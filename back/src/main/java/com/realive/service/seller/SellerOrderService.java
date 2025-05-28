package com.realive.service.seller;

import com.realive.dto.order.OrderListDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SellerOrderService {

    /**
     * 판매자 이메일로 본인의 주문 목록 조회
     * @param email 판매자 이메일
     * @param pageable 페이징 정보
     * @return 주문 목록 (DTO 변환 포함)
     */
    Page<OrderListDTO> getOrdersBySeller(String email, Pageable pageable);
}
