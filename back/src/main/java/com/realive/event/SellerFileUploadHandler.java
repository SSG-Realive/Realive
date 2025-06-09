package com.realive.event;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.realive.domain.common.enums.SellerFileType;
import com.realive.domain.seller.Seller;
import com.realive.domain.seller.SellerDocument;
import com.realive.repository.seller.SellerDocumentRepository;
import com.realive.service.common.FileUploadService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SellerFileUploadHandler {

    private final FileUploadService fileUploadService;
    private final SellerDocumentRepository sellerDocumentRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SellerFileUploadEvent event) {
        
        Seller seller = event.getSeller();

        //이벤트 수신 로그
        log.debug("▶ SellerFileUploadEvent 수신: sellerId={}, licenseFile={}, bankFile={}",
                seller.getId(),
                event.getLicensFile().getOriginalFilename(),
                event.getBankFile().getOriginalFilename()
        );

        String licenseUrl;
        String bankUrl;

        try{
            log.debug("▶ 파일 업로드 시작: sellerId={}", seller.getId());
            licenseUrl = fileUploadService.upload(event.getLicensFile(), "사업자등록증" , seller.getId());
            log.debug("▶ 사업자등록증 업로드 완료: url={}", licenseUrl);

            bankUrl = fileUploadService.upload(event.getBankFile(), "통장사본" , seller.getId());
            log.debug("▶ 통장사본 업로드 완료: url={}", bankUrl);
            
            } catch (Exception e) {
            // 업로드 실패 시 보상 로직 (이미 업로드된 파일 있으면 삭제)
            log.error("▶ 파일 업로드 중 예외: sellerId={}, message={}", seller.getId(), e.getMessage(), e);
            fileUploadService.deleteIfExists(seller.getId(), "사업자등록증");
            fileUploadService.deleteIfExists(seller.getId(), "통장사본");
            return; // 리스너 종료 (DB에는 Seller만 남음)
        }
        
        SellerDocument licensDoc =SellerDocument.builder()
                    .seller(seller)
                    .fileType(SellerFileType.사업자등록증)
                    .fileUrl(licenseUrl)
                    .isVerified(false)  
                    .build();
            
        SellerDocument bankDoc =SellerDocument.builder()
                    .seller(seller)
                    .fileType(SellerFileType.통장사본)
                    .fileUrl(bankUrl)
                    .isVerified(false)
                    .build();

        try {
            
            sellerDocumentRepository.saveAll(List.of(licensDoc, bankDoc));
            log.debug("▶ SellerDocument 저장 완료: sellerId={}", seller.getId());

        } catch (Exception e) {

            log.error("▶ SellerDocument 저장 실패: sellerId={}, message={}", seller.getId(), e.getMessage(), e);
            fileUploadService.deleteIfExists(seller.getId(),"사업자등록증" );
            fileUploadService.deleteIfExists(seller.getId(),"통장사본" );
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.");

        }
    }
    
}
