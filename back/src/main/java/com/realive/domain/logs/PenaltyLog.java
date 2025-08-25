package com.realive.domain.logs;

import com.realive.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "penalty_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PenaltyLog extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "customer_id")
    private Long customerId;

    private String reason;

    private Integer points;

    private String description;

}