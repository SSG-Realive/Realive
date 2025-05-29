//package com.realive.serviceimpl.admin.approval;
//
//import com.realive.domain.seller.Seller;
//import com.realive.dto.admin.approval.PendingSellerDTO;
//import com.realive.dto.seller.SellerResponseDTO;
//import com.realive.repository.admin.approval.ApprovalRepository;
//import com.realive.repository.seller.SellerRepository;
//import com.realive.service.admin.approval.SellerApprovalService;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.NoSuchElementException;
//import java.util.Optional;
//import java.util.stream.Collectors;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class SellerApprovalServiceImpl implements SellerApprovalService {
//
//    private final ApprovalRepository approvalRepository;
//    private final SellerRepository sellerRepository;
//
//
//    @Override
//    @Transactional(readOnly = true)
//    public List<PendingSellerDTO> getPendingApprovalSellers() {
//        log.info("승인 대기 중인 판매자 목록 조회 요청");
//        List<Seller> pendingSellers = approvalRepository.findByIsApprovedFalseAndApprovedAtIsNull();
//        return pendingSellers.stream()
//                .map(this::convertToPendingSellerDTO)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    @Transactional
//    public SellerResponseDTO processSellerDecision(Long sellerId, boolean approveAction, Integer approvingAdminId) {
//        log.info("판매자 처리 요청 - SellerId: {}, Action (Approve?): {}, AdminId: {}", sellerId, approveAction, approvingAdminId);
//
//        Optional<Seller> sellerOptional = approvalRepository.findById(sellerId);
//        Seller seller = sellerOptional.orElseThrow(() -> {
//            log.warn("processSellerDecision - 존재하지 않는 판매자 ID 시도: {}", sellerId);
//            return new NoSuchElementException("ID가 " + sellerId + "인 판매자를 찾을 수 없습니다.");
//        });
//
//        // "한 번 승인하면 끝" 규칙 및 approvedAt의 updatable=false 제약 반영
//        if (seller.getApprovedAt() != null) {
//            // 이미 approvedAt이 설정되어 있다면, 어떤 결정이든 이미 내려진 상태.
//            // "한 번 승인하면 끝"이므로 더 이상 상태 변경 불가.
//            log.warn("processSellerDecision - 이미 처리된 판매자(ID: {})입니다. (approvedAt: {}). 상태 변경 불가.",
//                    sellerId, seller.getApprovedAt());
//            // 현재 상태를 그대로 반환하거나, 예외를 발생시킬 수 있습니다.
//            // 여기서는 예외를 발생시켜 클라이언트에게 명확히 알립니다.
//            throw new IllegalStateException("이미 승인/거부 처리가 완료된 판매자(ID: " + sellerId + ")입니다. 상태를 변경할 수 없습니다.");
//        }
//
//        // approvedAt이 null인 경우 (최초 처리)
//        if (approveAction) { // 승인 처리
//            seller.setApproved(true);
//            seller.setApprovedAt(LocalDateTime.now()); // 승인 시에만 approvedAt 설정
//            seller.setActive(true);
//            log.info("판매자 승인 처리됨 - SellerId: {}", sellerId);
//        } else { // 거부 처리
//            seller.setApproved(false);
//            // 거부 시에는 approvedAt을 설정하지 않음 (null 유지)
//            // (선택적) isActive=false 처리: Seller 엔티티에 isActive 필드와 setter가 있으므로 사용 가능
//            if (seller.isActive()) { // 현재 활성 상태일 때만 비활성화
//                seller.setActive(false);
//                log.info("판매자 거부와 함께 비활성화 처리됨 - SellerId: {}", sellerId);
//            } else {
//                log.info("판매자 거부 처리됨 (이미 비활성 상태) - SellerId: {}", sellerId);
//            }
//        }
//
//        Seller savedSeller = approvalRepository.save(seller);
//        log.info("판매자 처리 완료 - SellerId: {}, 최종 상태 (isApproved): {}, 최종 상태 (isActive): {}, approvedAt: {}",
//                savedSeller.getId(), savedSeller.isApproved(), savedSeller.isActive(), savedSeller.getApprovedAt());
//
//        return convertToSellerResponseDTO(savedSeller);
//    }
//
//    private PendingSellerDTO convertToPendingSellerDTO(Seller seller) {
//        if (seller == null) return null;
//        return PendingSellerDTO.builder()
//                .id(seller.getId())
//                .name(seller.getName())
//                .email(seller.getEmail())
//                .businessNumber(seller.getBusinessNumber())
//                .build();
//    }
//
//    private SellerResponseDTO convertToSellerResponseDTO(Seller seller) {
//        if (seller == null) return null;
//        return SellerResponseDTO.builder()
//                .email(seller.getEmail())
//                .name(seller.getName())
//                .phone(seller.getPhone())
//                .isApproved(seller.isApproved())
//                .businessNumber(seller.getBusinessNumber())
//                // Seller 엔티티에 hasBankAccountCopy 필드가 없으므로 DTO에서는 false 또는 생략
//                .build();
//    }
//
//    // 승인된 업체 리스트
//    @Override
//    public List<SellerResponseDTO> getApprovedSellers() {
//        return approvalRepository.findByIsApprovedTrueAndIsActiveTrue().stream()
//                .map(seller -> SellerResponseDTO.builder()
//                        .email(seller.getEmail())
//                        .name(seller.getName())
//                        .phone(seller.getPhone())
//                        .isApproved(seller.isApproved())
//                        .businessNumber(seller.getBusinessNumber())
//                        .build())
//                .toList();
//    }
//
//}
