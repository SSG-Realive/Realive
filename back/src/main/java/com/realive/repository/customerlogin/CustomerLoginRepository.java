package com.realive.repository.customerlogin;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.realive.domain.customer.Customer;

public interface CustomerLoginRepository extends JpaRepository<Customer, Long> {

    // 이메일로 고객 찾기
    @Query("SELECT c FROM Customer c WHERE c.email = :email AND c.signupMethod = com.realive.domain.customer.SignupMethod.USER  and c.isActive = true")
    Optional<Customer> findByEmail(@Param("email") String email);

    @Query("SELECT c FROM Customer c WHERE c.email = :email")
    Optional<Customer> findByEmailIncludingSocial(@Param("email") String email);


    
}
