package com.realive.repository.seller;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realive.domain.common.enums.SellerFileType;
import com.realive.domain.seller.SellerDocument;

/**
 * SellerDocumentRepository
 * - 판매자 서류(SellerDocument) 관련 JPA Repository 인터페이스
 * - 기본적인 CRUD 기능 제공
 * - 판매자의 특정 파일 유형 존재 여부 확인 메서드 포함
 */
public interface SellerDocumentRepository extends JpaRepository<SellerDocument, Long> {

    /**
     * 특정 판매자의 특정 파일 유형이 존재하는지 확인
     *
     * @param sellerId  판매자 ID
     * @param fileType  파일 유형 (예: 사업자등록증, 통장사본 등)
     * @return          해당 조합의 데이터 존재 여부
     */
    boolean existsBySellerIdAndFileType(Long sellerId, SellerFileType fileType);
}