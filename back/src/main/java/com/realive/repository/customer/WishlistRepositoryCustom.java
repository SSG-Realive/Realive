package com.realive.repository.customer; // 이 리포지토리가 속할 패키지 (예: realive.repository.wishlist)

import com.realive.dto.wishlist.WishlistMostResponseDTO;
import java.util.List;

// Querydsl을 사용하기 위해 만든 Repository
public interface WishlistRepositoryCustom {

    // 특정 개수(limit)만큼의 가장 많이 찜된 상품 리스트를 조회하는 메서드
    // 페이징 처리는 Service 단계
    List<WishlistMostResponseDTO> findTopNWishlistedProducts(int limit);
}