package com.realive.domain.review;

import com.realive.domain.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_reports")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewReport extends BaseTimeEntity {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Integer id;

@Column(name = "seller_review_id")
private Integer sellerReviewId;

@Column(name = "reporter_id")
private Integer reporterId;

@Column(length = 1000)
private String reason;
}