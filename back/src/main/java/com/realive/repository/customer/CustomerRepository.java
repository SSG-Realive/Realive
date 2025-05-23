// CustomerRepository.java (검색 메소드 추가된 버전)
package com.realive.repository.customer;

import com.realive.domain.customer.Customer;
import org.springframework.data.domain.Page;     // 추가
import org.springframework.data.domain.Pageable;  // 추가
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 이름 또는 이메일에 키워드가 포함된 고객 검색 (페이징 지원, 대소문자 무시)
    Page<Customer> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(String nameKeyword, String emailKeyword, Pageable pageable);
}
