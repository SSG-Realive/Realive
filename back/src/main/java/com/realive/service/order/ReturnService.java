package com.realive.service.order;

import com.realive.domain.common.enums.ReturnReason;
import com.realive.domain.common.enums.ReturnStatus;
import com.realive.domain.order.Order;
import com.realive.domain.order.ReturnRequest;
import com.realive.domain.product.Product;
import com.realive.dto.order.ReturnProcessRequestDTO;
import com.realive.dto.order.ReturnRequestDTO;
import com.realive.dto.order.ReturnResponseDTO;
import com.realive.repository.order.OrderRepository;
import com.realive.repository.order.ReturnRequestRepository;
import com.realive.repository.product.ProductRepository;
import com.realive.repository.productview.ProductViewRepository;
import com.realive.util.ImageUploader;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductViewRepository productViewRepository;
    private final ImageUploader imageUploader;

    @Transactional
    public ReturnResponseDTO createReturnRequest(Long customerId, ReturnRequestDTO requestDTO) {
        Order order = orderRepository.findById(requestDTO.getOrderId())
                .orElseThrow(() -> new EntityNotFoundException("Order not found with ID: " + requestDTO.getOrderId()));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new IllegalArgumentException("Unauthorized: This order does not belong to the customer.");
        }

        Product product = productRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Product not found with ID: " + requestDTO.getProductId()));

        List<String> imageUrls = null;
        if (requestDTO.getEvidenceImages() != null && !requestDTO.getEvidenceImages().isEmpty()) {
            imageUrls = requestDTO.getEvidenceImages().stream()
                    .map(image -> imageUploader.uploadImage(image, "evidence_"))
                    .collect(Collectors.toList());
        }

        int calculatedReturnShippingFee = calculateReturnShippingFee(requestDTO.getReturnReason());

        ReturnRequest returnRequest = ReturnRequest.builder()
                .order(order)
                .product(product)
                .quantity(requestDTO.getQuantity())
                .returnReason(requestDTO.getReturnReason())
                .detailedReason(requestDTO.getDetailedReason())
                .imageUrls(imageUrls != null ? imageUrls : new ArrayList<>())
                .status(ReturnStatus.PENDING)
                .returnShippingFee(calculatedReturnShippingFee)
                .build();

        ReturnRequest savedReturnRequest = returnRequestRepository.save(returnRequest);

        String productName = productViewRepository.findProductDetailById(product.getId())
                .map(p -> p.getName())
                .orElse("알 수 없는 상품");

        return ReturnResponseDTO.from(savedReturnRequest, productName);
    }

    @Transactional(readOnly = true)
    public ReturnResponseDTO getReturnRequest(Long customerId, Long returnRequestId) {
        ReturnRequest returnRequest = returnRequestRepository.findByIdAndOrder_Customer_Id(returnRequestId, customerId)
                .orElseThrow(() -> new EntityNotFoundException("Return request not found or unauthorized with ID: " + returnRequestId));

        String productName = productViewRepository.findProductDetailById(returnRequest.getProduct().getId())
                .map(p -> p.getName())
                .orElse("알 수 없는 상품");

        return ReturnResponseDTO.from(returnRequest, productName);
    }

    @Transactional(readOnly = true)
    public List<ReturnResponseDTO> getReturnRequestsByCustomer(Long customerId) {
        List<ReturnRequest> returnRequests = returnRequestRepository.findByOrder_Customer_Id(customerId);

        List<Long> productIds = returnRequests.stream()
                .map(rr -> rr.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        Map<Long, String> productNamesMap = productIds.stream()
                .map(productViewRepository::findProductDetailById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(
                        p -> p.getId(),
                        p -> p.getName()
                ));

        return returnRequests.stream()
                .map(rr -> ReturnResponseDTO.from(rr, productNamesMap.getOrDefault(rr.getProduct().getId(), "알 수 없는 상품")))
                .collect(Collectors.toList());
    }

    @Transactional
    public ReturnResponseDTO processReturnRequest(Long returnRequestId, ReturnProcessRequestDTO requestDTO) {
        ReturnRequest returnRequest = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new EntityNotFoundException("Return request not found with ID: " + returnRequestId));

        returnRequest.updateStatus(requestDTO.getNewStatus());
        returnRequest.setAdminMemo(requestDTO.getAdminMemo());

        int finalRefundAmount = requestDTO.getRefundAmount() != null ? requestDTO.getRefundAmount() :
                calculateRefundAmount(returnRequest);

        returnRequest.setRefundAmount(finalRefundAmount);

        if (requestDTO.getNewStatus() == ReturnStatus.REFUND_PROCESSING || requestDTO.getNewStatus() == ReturnStatus.COMPLETED) {
            log.info("Initiating refund for return request {}. Amount: {}", returnRequestId, finalRefundAmount);
        }

        ReturnRequest updatedReturnRequest = returnRequestRepository.save(returnRequest);

        String productName = productViewRepository.findProductDetailById(updatedReturnRequest.getProduct().getId())
                .map(p -> p.getName())
                .orElse("알 수 없는 상품");

        return ReturnResponseDTO.from(updatedReturnRequest, productName);
    }

    // TODO: 현재 반품 비용 5000원으로 고정인데 협의 후 변경 필요
    private int calculateReturnShippingFee(ReturnReason reason) {
        if (reason.isCustomerFault()) {
            return 5000;
        } else {
            return 0;
        }
    }

    // 반품시 반환받는 금액 계산 툴
    private int calculateRefundAmount(ReturnRequest returnRequest) {
        int productActualPrice = returnRequest.getProduct().getPrice() * returnRequest.getQuantity();
        return productActualPrice - returnRequest.getReturnShippingFee();
    }
}