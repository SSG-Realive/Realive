package com.realive.service.customer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.domain.customer.Customer;
import com.realive.domain.customer.CustomerQna;
import com.realive.domain.product.Product;
import com.realive.domain.seller.Seller;
import com.realive.dto.customer.customerqna.CustomerQnaDetailDTO;
import com.realive.dto.customer.customerqna.CustomerQnaListDTO;
import com.realive.dto.customer.customerqna.CustomerQnaRequestDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.repository.customer.CustomerQnaRepository;
import com.realive.repository.customer.CustomerRepository;
import com.realive.repository.customer.productview.ProductListRepository;
import com.realive.repository.customer.productview.ProductViewRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

// [Customer] Q&A Service

@Service
@RequiredArgsConstructor
@Transactional
public class CustomerQnaService {

    private final ProductViewRepository ProductViewRepository;
    private final CustomerQnaRepository customerQnaRepository;
    private final CustomerRepository customerRepository;
    private final ProductListRepository productListRepository;

    // 판매 상품 1:1 문의하기 + 상단에 상품 요약
    public Map<String, Object> createQnaWithProductSummary(CustomerQnaRequestDTO requestDTO) {
        
        Product product = ProductViewRepository.findById(requestDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. ID=" + requestDTO.getProductId()));

        Customer customer = customerRepository.findActiveUserById(requestDTO.getCustomerId())
                .orElseThrow(() -> new EntityNotFoundException("고객을 찾을 수 없습니다. ID=" + requestDTO.getCustomerId()));

        Seller seller = product.getSeller();

        CustomerQna qna = CustomerQna.builder()
                .product(product)
                .seller(seller)
                .customer(customer)
                .title(requestDTO.getTitle())
                .content(requestDTO.getContent())
                .isAnswered(false)
                .build();

        CustomerQna saved = customerQnaRepository.save(qna);

        // 상품 요약 정보 가져오기
        ProductListDTO productSummary = productListRepository
                .getWishlistedProducts(List.of(product.getId()))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("상품 요약 정보를 찾을 수 없습니다."));

        return Map.of(
            "qnaId", saved.getId(),
            "product", productSummary
        );
    }

    // 내 문의 목록 조회
    public List<Map<String, Object>> listQnaWithProductSummary(Long customerId) {
       
        List<CustomerQna> qnaList = customerQnaRepository.findByCustomerIdOrderByIdDesc(customerId);

        List<Long> productIds = qnaList.stream()
                .map(q -> q.getProduct().getId())
                .distinct()
                .collect(Collectors.toList());

        List<ProductListDTO> productSummaries = productListRepository.getWishlistedProducts(productIds);
        Map<Long, ProductListDTO> productMap = productSummaries.stream()
                .collect(Collectors.toMap(ProductListDTO::getId, dto -> dto));

        List<Map<String, Object>> resultList = qnaList.stream().map(qna -> {
            Map<String, Object> result = new HashMap<>();

            CustomerQnaListDTO qnaDto = CustomerQnaListDTO.builder()
                    .id(qna.getId())
                    .title(qna.getTitle())
                    .createdAt(qna.getCreatedAt())
                    .isAnswered(qna.getIsAnswered())
                    .build();

            result.put("qna", qnaDto);
            result.put("productSummary", productMap.getOrDefault(qna.getProduct().getId(), null));

            return result;
        }).collect(Collectors.toList());

        return resultList;
    }

    // 문의 상세조회(문의+답변)
    public Map<String, Object> detailQnaWithProductSummary(Long id, Long customerId) {

        CustomerQna qna = customerQnaRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new EntityNotFoundException("문의 정보를 찾을 수 없습니다. ID=" + id));
        
        Product product = qna.getProduct();
        
        ProductListDTO productSummary = productListRepository
                .getWishlistedProducts(List.of(product.getId()))
                .stream()
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("상품 요약 정보를 찾을 수 없습니다."));

        CustomerQnaDetailDTO qnaDetail = CustomerQnaDetailDTO.builder()
            .id(qna.getId())
            .title(qna.getTitle())
            .content(qna.getContent())
            .answer(qna.getAnswer())
            .isAnswered(qna.getIsAnswered())
            .createdAt(qna.getCreatedAt())
            .updatedAt(qna.getUpdatedAt())
            .answeredAt(qna.getAnsweredAt()) // nullable 가능
            .build();

        Map<String, Object> result = new HashMap<>();
        result.put("qna", qnaDetail);
        result.put("productSummary", productSummary);

        return result;
    }

    // 상품 문의 목록 조회(상품 상세조회 페이지)
    public List<CustomerQnaListDTO> listProductQnaWith(Long productId) {

        List<CustomerQna> qnaList = customerQnaRepository.findByProductIdOrderByIdDesc(productId);

        return qnaList.stream()
                .map(qna -> CustomerQnaListDTO.builder()
                        .id(qna.getId())
                        .title(qna.getTitle())
                        .createdAt(qna.getCreatedAt())
                        .isAnswered(qna.getIsAnswered())
                        .build())
                .collect(Collectors.toList());
    }

}
