package com.realive.repository.customer;

import com.realive.domain.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    // JPA 기본 메서드 사용
}