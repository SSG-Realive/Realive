package com.realive.repository.customer;

import java.lang.StackWalker.Option;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.customer.CustomerQna;

public interface CustomerQnaRepository extends JpaRepository<CustomerQna, Long> {
    
    // 내 문의 리스트 조회 
    List<CustomerQna> findByCustomerIdOrderByIdDesc(Long customerId);

    // 내 문의 상세 조회
    @Query("SELECT q FROM CustomerQna q WHERE q.id = :id AND q.customer.id = :customerId")
    Optional<CustomerQna> findByIdAndCustomerId(@Param("id") Long id, @Param("customerId") Long customerId);
    
    // 상품 문의 리스트 조회
    List<CustomerQna> findByProductIdOrderByIdDesc(Long productId);

}
