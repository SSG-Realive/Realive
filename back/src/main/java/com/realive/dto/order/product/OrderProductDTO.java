package com.realive.dto.order.product;

import lombok.Data;

//주문개별상품정보(결제)
//한 스토어의 상품단위
@Data
public class OrderProductDTO {

    private Long productId;//상품 id
    
    private Long productName;//상품 이름
    
    private String thumbnailUrl;//상품 썸네일 

    private int price;//개별 상품 금액
    
    private int quantity; //개별상품 수량

    private int totalPrice;//총 가격(수량*가격격)

}
