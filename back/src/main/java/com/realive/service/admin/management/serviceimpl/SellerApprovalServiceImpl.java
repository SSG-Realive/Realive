//package com.realive.service.admin.management.serviceimpl;
//
//import com.realive.domain.seller.Seller;
//import com.realive.dto.admin.approval.ApproveSellerRequestDTO;
//import com.realive.dto.admin.approval.ApproveSellerResponseDTO;
//import com.realive.repository.seller.SellerRepository;
//import com.realive.service.admin.management.service.SellerApprovalService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.NoSuchElementException;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SellerApprovalServiceImpl implements SellerApprovalService {
//
//    private final SellerRepository sellerRepository;
//
//    @Override
//    @Transactional
//    public ApproveSellerResponseDTO processSellerApproval(ApproveSellerRequestDTO requestDto, Integer approvingAdminId) {
//        Seller seller = sellerRepository.findById(requestDto.getSellerId().longValue())
//                .orElseThrow(() -> new NoSuchElementException("판매자 없음 ID: " + requestDto.getSellerId()));
//
//        String message;
//        boolean successProcessing = false;
//        String finalStatus = seller.isApproved() ? "APPROVED" : "PENDING";
//
//        if ("APPROVE".equalsIgnoreCase(requestDto.getStatus())) {
//            if (!seller.isApproved()) {
//                seller.setApproved(true);
//                seller.setApprovedAt(LocalDateTime.now());
//                sellerRepository.save(seller);
//                message = "판매자 승인 완료.";
//                successProcessing = true;
//                finalStatus = "APPROVED";
//            } else {
//                message = "이미 승인된 판매자입니다.";
//            }
//        } else if ("REJECT".equalsIgnoreCase(requestDto.getStatus())) {
//            if (seller.isApproved()) {
//                message = "이미 승인된 판매자는 현재 로직에서 거부 처리할 수 없습니다.";
//            } else {
//                message = "판매자 승인 거부 처리됨. 사유: " + requestDto.getReason();
//                successProcessing = true; // '처리'는 된 것으로 간주
//                finalStatus = "REJECTED"; // 명시적인 REJECTED 상태가 있다면
//            }
//        } else {
//            message = "알 수 없는 승인 상태값: " + requestDto.getStatus();
//        }
//        log.info("판매자 승인 처리 결과 - SellerId: {}, AdminId: {}, Message: {}", requestDto.getSellerId(), approvingAdminId, message);
//
//        return ApproveSellerResponseDTO.builder()
//                .success(successProcessing)
//                .sellerId(requestDto.getSellerId())
//                .newStatus(finalStatus)
//                .message(message)
//                .processedAt(LocalDateTime.now())
//                .build();
//    }
//}
