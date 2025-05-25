package com.realive.repository.customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.customer.Customer;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // 고객 id로 고객 찾기
    // 회원: isActive = true, signupMethod = USER일 것. 
    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.signupMethod = com.realive.domain.customer.SignupMethod.USER AND c.isActive = true")
    Optional<Customer> findActiveUserById(@Param("id")Long id);
    
}
