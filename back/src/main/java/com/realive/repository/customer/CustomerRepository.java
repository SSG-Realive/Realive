package com.realive.repository.customer;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.realive.domain.customer.Customer;

// [Customer] 고객 Repository

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    // 고객ID로 고객 찾기
    // 회원: isActive = true, signupMethod = USER일 것. 
    @Query("SELECT c FROM Customer c WHERE c.id = :id AND c.signupMethod = com.realive.domain.customer.SignupMethod.USER AND c.isActive = true")
    Optional<Customer> findActiveUserById(@Param("id")Long id);

    // 이메일로 고객 찾기
    @Query("SELECT c FROM Customer c WHERE c.email = :email AND c.signupMethod = com.realive.domain.customer.SignupMethod.USER  and c.isActive = true")
    Optional<Customer> findByEmail(@Param("email") String email);

    // 이메일로 고객 찾기 (임시회원 포함)
    @Query("SELECT c FROM Customer c WHERE c.email = :email")
    Optional<Customer> findByEmailIncludingSocial(@Param("email") String email);
    
}