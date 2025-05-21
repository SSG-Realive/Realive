package com.realive.repository.seller;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realive.domain.common.enums.SellerFileType;
import com.realive.domain.seller.SellerDocument;

public interface SellerDocumentRepository extends JpaRepository<SellerDocument, Long> {
    
    boolean existsBySellerIdAndFileType(Long sellerId, SellerFileType fileType);

}
