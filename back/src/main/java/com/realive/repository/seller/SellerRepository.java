package com.realive.repository.seller;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.realive.domain.seller.Seller;
import com.realive.repository.common.EmailLookupRepository;

public interface SellerRepository extends JpaRepository<Seller,Long>, EmailLookupRepository<Seller> {

    boolean existsByEmail(String email); //email 중복검사
    boolean existsByName(String name); //이름 중복검사
    Optional<Seller> findByEmailAndIsActiveTrue(String email); //로그인용(소프트삭제)
    Optional<Seller> findByEmail(String email);
    // 활성화된 판매자 수를 조회하는 메서드 추가
    long countByIsActiveTrue();
}