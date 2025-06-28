package com.realive.repository.customer;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.customer.CustomerQna;

// [Customer] Q&A Repository

public interface CustomerQnaRepository extends JpaRepository<CustomerQna, Long> {

    // 고객ID로 내 문의 리스트 가져오기
    List<CustomerQna> findByCustomerIdOrderByIdDesc(Long customerId);

    // QnA ID, 고객ID로 내 문의 상세 가져오기
    @Query("SELECT q FROM CustomerQna q WHERE q.id = :id AND q.customer.id = :customerId")
    Optional<CustomerQna> findByIdAndCustomerId(@Param("id") Long id, @Param("customerId") Long customerId);

    // 상품ID로 상품 문의 리스트 가져오기
    List<CustomerQna> findByProductIdOrderByIdDesc(Long productId);

    // 수정: 판매자ID로 해당 판매자의 상품에 대한 고객 문의 리스트 가져오기
    List<CustomerQna> findBySellerIdOrderByIdDesc(Long sellerId);

    // 수정: QnA ID, 판매자ID로 문의 상세 가져오기 (판매자 권한 확인용)
    @Query("SELECT q FROM CustomerQna q WHERE q.id = :id AND q.seller.id = :sellerId")
    Optional<CustomerQna> findByIdAndSellerId(@Param("id") Long id, @Param("sellerId") Long sellerId);

    // 추가: 판매자ID로 해당 판매자의 고객 문의를 페이징하여 가져오기
    Page<CustomerQna> findBySellerId(Pageable pageable, Long sellerId);

    // 추가: 판매자ID로 해당 판매자의 답변되지 않은 고객 문의를 페이징하여 가져오기
    Page<CustomerQna> findBySellerIdAndIsAnsweredFalse(Pageable pageable, Long sellerId);
}