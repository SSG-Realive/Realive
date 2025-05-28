//package com.realive.dto.order;
//
//import lombok.Data;
//import org.springframework.format.annotation.DateTimeFormat;
//
//import java.time.LocalDate;
//
///**
// * 판매자 주문 목록 조회 시 사용할 검색 조건 DTO
// */
//@Data
//public class OrderSearchCondition {
//
//    private String productName;     // 상품명 검색 키워드
//    private String status;          // 주문 상태 필터 (예: 결제완료)
//
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//    private LocalDate startDate;    // 주문일 시작일
//
//    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
//    private LocalDate endDate;      // 주문일 종료일
//
//    private String sort = "createdAt";   // 정렬 기준 필드 (기본: 주문일)
//    private String direction = "desc";   // 정렬 방향 (기본: 최신순)
//}