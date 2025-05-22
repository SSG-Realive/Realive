package com.realive.domain.customer;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "customer")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
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

    public Customer(String email, String password) {
        this.email = email;
        this.password = password;
    }

    //디폴트 0
    @Column(name = "penalty_score")
    private Integer penaltyScore = 0;
 
    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    //일반/소셜 구분
    @Column(name = "signup_method")
    private SignupMethod signupMethod;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime created;

    @LastModifiedDate
    @Column(name ="updated_at" )
    protected LocalDateTime updated;

    
}
