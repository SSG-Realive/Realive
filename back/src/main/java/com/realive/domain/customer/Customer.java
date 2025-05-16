package com.realive.domain.customer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customer")
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id ;

    private String email;
    private String password;
    private String name;
    private String phone;
    private String address;

    //디폴트 false
    @Column(name = "is_verified")
    private Boolean isVerified = false;
    
    //디폴트 true
    //탈퇴처리 enum 0,1 ?? 
    @Column(name = "is_active")
    private Boolean isActive = true;

    //디폴트 0
    @Column(name = "penalty_score")
    private Integer penaltyScore = 0;
 
    private LocalDate birth;

    private Gender gender;

    @Column(name = "signup_method")
    private SignupMethod signupMethod;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime created;

    @LastModifiedDate
    @Column(name ="updated_at" )
    protected LocalDateTime updated;

    
}
