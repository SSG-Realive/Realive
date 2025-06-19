package com.realive.domain.common.enums;

public enum BidResultType {
    SUCCESS,            // 입찰 성공
    BELOW_HIGHEST,      // 최고가 미달/동일
    AUCTION_ENDED,      // 경매 종료 후 시도
    AUCTION_CANCELLED   // 경매 취소/중단 상태
}
