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

    // [추가] 모든 고객 Q&A를 페이징하여 가져오기 (관리자용)
    Page<CustomerQna> findAll(Pageable pageable);

    // [추가] 답변되지 않은 고객 Q&A를 페이징하여 가져오기 (관리자용)
    Page<CustomerQna> findByIsAnsweredFalse(Pageable pageable);

}
