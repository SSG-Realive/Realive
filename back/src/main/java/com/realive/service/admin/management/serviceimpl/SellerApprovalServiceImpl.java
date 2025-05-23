package com.realive.service.admin.management.serviceimpl;

import com.realive.domain.common.enums.SellerApprovalStatusByAdmin;
import com.realive.domain.seller.Seller;
import com.realive.dto.admin.approval.ApproveSellerRequestDTO;
import com.realive.dto.admin.approval.ApproveSellerResponseDTO;
import com.realive.repository.seller.SellerRepository;
import com.realive.service.admin.management.service.SellerApprovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SellerApprovalServiceImpl implements SellerApprovalService {

    private final SellerRepository sellerRepository;

    @Override
    @Transactional
    public ApproveSellerResponseDTO processSellerApproval(ApproveSellerRequestDTO requestDto, Integer approvingAdminId) {
        if (requestDto == null || requestDto.getSellerId() == null || requestDto.getStatus() == null) {
            log.warn("processSellerApproval: 필수 요청 파라미터가 누락되었습니다.");
            return ApproveSellerResponseDTO.builder()
                    .success(false)
                    .message("필수 요청 파라미터가 누락되었습니다.")
                    .processedAt(LocalDateTime.now())
                    .build();
        }

        Optional<Seller> sellerOptional = sellerRepository.findById(requestDto.getSellerId().longValue());
        Seller seller = sellerOptional.orElseThrow(() -> new NoSuchElementException("판매자 없음 ID: " + requestDto.getSellerId()));

        String message = "알 수 없는 요청입니다.";
        boolean successProcessing = false;
        String finalStatus = seller.isApproved() ? SellerApprovalStatusByAdmin.APPROVED.name() : SellerApprovalStatusByAdmin.PENDING_REVIEW.name();

        try {
            SellerApprovalStatusByAdmin requestedStatus = SellerApprovalStatusByAdmin.valueOf(requestDto.getStatus().toUpperCase());

            switch (requestedStatus) {
                case APPROVED:
                    if (!seller.isApproved()) {
                        seller.setApproved(true);
                        seller.setApprovedAt(LocalDateTime.now());
                        message = "판매자 승인 완료.";
                        finalStatus = SellerApprovalStatusByAdmin.APPROVED.name();
                        successProcessing = true;
                    } else {
                        message = "이미 승인된 판매자입니다.";
                    }
                    break;
                case REJECTED:
                    if (seller.isApproved()) {
                        message = "이미 승인된 판매자는 현재 로직에서 거부 처리할 수 없습니다.";
                    } else {
                        seller.setApproved(false);
                        message = "판매자 승인 거부 처리됨. 사유: " + (requestDto.getReason() != null ? requestDto.getReason() : "사유 없음");
                        finalStatus = SellerApprovalStatusByAdmin.REJECTED.name();
                        successProcessing = true;
                    }
                    break;
                case ON_HOLD:
                    seller.setApproved(false);
                    message = "판매자 승인 보류 처리됨. 사유: " + (requestDto.getReason() != null ? requestDto.getReason() : "사유 없음");
                    finalStatus = SellerApprovalStatusByAdmin.ON_HOLD.name();
                    successProcessing = true;
                    break;
                case PENDING_REVIEW:
                    seller.setApproved(false);
                    message = "판매자 검토 대기 상태로 변경됨.";
                    finalStatus = SellerApprovalStatusByAdmin.PENDING_REVIEW.name();
                    successProcessing = true;
                    break;
                default:
                    message = "처리할 수 없는 승인 상태값입니다: " + requestDto.getStatus();
                    break;
            }

            if (successProcessing) {
                sellerRepository.save(seller);
            }
        } catch (IllegalArgumentException ex) {
            message = "유효하지 않은 승인 상태값입니다: " + requestDto.getStatus();
            log.warn("유효하지 않은 SellerApprovalStatusByAdmin 값: {}", requestDto.getStatus(), ex);
        } catch (Exception ex) {
            message = "판매자 승인 처리 중 오류가 발생했습니다.";
            log.error("판매자 승인 처리 오류 - SellerId: {}, AdminId: {}", requestDto.getSellerId(), approvingAdminId, ex);
            successProcessing = false; // 오류 발생 시 처리 실패로 간주
        }

        log.info("판매자 승인 처리 결과 - SellerId: {}, AdminId: {}, Message: {}", requestDto.getSellerId(), approvingAdminId, message);

        return ApproveSellerResponseDTO.builder()
                .success(successProcessing)
                .sellerId(requestDto.getSellerId())
                .newStatus(finalStatus)
                .message(message)
                .processedAt(LocalDateTime.now())
                .build();
    }
}
