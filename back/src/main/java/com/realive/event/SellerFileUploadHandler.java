package com.realive.event;

import com.realive.domain.common.enums.SellerFileType;
import com.realive.domain.seller.Seller;
import com.realive.domain.seller.SellerDocument;
import com.realive.repository.seller.SellerDocumentRepository;
import com.realive.service.common.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SellerFileUploadHandler {

    private final FileUploadService fileUploadService;
    private final SellerDocumentRepository sellerDocumentRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SellerFileUploadEvent event) {
        
        Seller seller = event.getSeller();

        try{
            String licenseUrl = fileUploadService.upload(event.getLicensFile(), "사업자등록증" , seller.getId());
            String bankUrl = fileUploadService.upload(event.getBankFile(), "통장사본" , seller.getId());
            
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

            sellerDocumentRepository.saveAll(List.of(licensDoc, bankDoc));
        } catch (Exception e) {

            fileUploadService.deleteIfExists(seller.getId(),"사업자등록증" );
            fileUploadService.deleteIfExists(seller.getId(),"통장사본" );
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.");

        }
    }
    
}
