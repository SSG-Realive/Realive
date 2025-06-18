package com.realive.repository.review;

import com.realive.domain.customer.Customer;
import com.realive.domain.order.Order;
import com.realive.domain.product.Product;
import com.realive.domain.seller.SellerReview;
import jakarta.persistence.criteria.*; // Criteria API import
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils; // StringUtils.hasText 사용

public class SellerReviewSpecification {

    // 상품명(productName)으로 필터링하는 Specification
    public static Specification<SellerReview> productNameContains(String productName) {
        return (Root<SellerReview> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (!StringUtils.hasText(productName)) { // productName이 null이거나 비어있으면 조건 없음
                return null;
            }
            // SellerReview -> Order -> Product -> name 경로로 Join
            Join<SellerReview, Order> orderJoin = root.join("order", JoinType.INNER);
            Join<Order, Product> productJoin = orderJoin.join("product", JoinType.INNER);
            // productName을 포함하는(like) 조건 생성
            return criteriaBuilder.like(criteriaBuilder.lower(productJoin.get("name")), "%" + productName.toLowerCase() + "%");
        };
    }

    // 고객명(customerName)으로 필터링하는 Specification
    public static Specification<SellerReview> customerNameContains(String customerName) {
        return (Root<SellerReview> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (!StringUtils.hasText(customerName)) { // customerName이 null이거나 비어있으면 조건 없음
                return null;
            }
            // SellerReview -> Customer -> name 경로로 Join
            Join<SellerReview, Customer> customerJoin = root.join("customer", JoinType.INNER);
            // customerName을 포함하는(like) 조건 생성
            return criteriaBuilder.like(criteriaBuilder.lower(customerJoin.get("name")), "%" + customerName.toLowerCase() + "%");
        };
    }

    // 만약 다른 필터 조건 (예: 판매자명)이 필요하다면 여기에 추가적으로 정의합니다.

    public static Specification<SellerReview> sellerNameContains(String sellerName) {
        return (Root<SellerReview> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) -> {
            if (!StringUtils.hasText(sellerName)) {
                return null;
            }
            Join<SellerReview, com.realive.domain.seller.Seller> sellerJoin = root.join("seller", JoinType.INNER);
            return criteriaBuilder.like(criteriaBuilder.lower(sellerJoin.get("name")), "%" + sellerName.toLowerCase() + "%");
        };
    }

}
