package com.realive.event;

import com.realive.domain.common.enums.SellerFileType;
import com.realive.domain.seller.Seller;
import com.realive.domain.seller.SellerDocument;
import com.realive.repository.seller.SellerDocumentRepository;
import com.realive.service.common.S3Uploader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SellerFileUploadHandler {

    private final S3Uploader s3Uploader;
    private final SellerDocumentRepository sellerDocumentRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(SellerFileUploadEvent event) {

        Seller seller = event.getSeller();

        try {
            String licenseUrl = s3Uploader.upload(event.getLicensFile(), "seller/license/" + seller.getId());
            String bankUrl = s3Uploader.upload(event.getBankFile(), "seller/bank/" + seller.getId());

            SellerDocument licensDoc = SellerDocument.builder()
                    .seller(seller)
                    .fileType(SellerFileType.사업자등록증)
                    .fileUrl(licenseUrl)
                    .isVerified(false)
                    .build();

            SellerDocument bankDoc = SellerDocument.builder()
                    .seller(seller)
                    .fileType(SellerFileType.통장사본)
                    .fileUrl(bankUrl)
                    .isVerified(false)
                    .build();

            sellerDocumentRepository.saveAll(List.of(licensDoc, bankDoc));
        } catch (Exception e) {

            throw new RuntimeException("S3 파일 업로드 중 오류가 발생했습니다.", e);
        }
    }


}
