package com.realive.util;

import org.springframework.stereotype.Component;

public interface TickSizeCalculator {
    /**
     * 시작가에 따른 입찰 단위를 계산합니다.
     * @param startPrice 경매 시작가
     * @return 입찰 단위
     */
    int calculateTickSize(int startPrice);

    /**
     * 현재가와 시작가를 기반으로 최소 입찰가를 계산합니다.
     * @param currentPrice 현재가
     * @param startPrice 시작가
     * @return 최소 입찰가
     */
    int calculateMinBidPrice(int currentPrice, int startPrice);
}

@Component
class DefaultTickSizeCalculator implements TickSizeCalculator {
    // 가구 가격대별 입찰 단위 상수
    private static final int LOW_PRICE_THRESHOLD = 10_000;      // 1만원
    private static final int MID_PRICE_THRESHOLD = 100_000;     // 10만원
    private static final int HIGH_PRICE_THRESHOLD = 1_000_000;  // 100만원
    
    private static final int LOW_PRICE_TICK = 100;      // 100원
    private static final int MID_PRICE_TICK = 1_000;    // 1,000원
    private static final int HIGH_PRICE_TICK = 10_000;  // 1만원
    private static final int PREMIUM_TICK = 100_000;    // 10만원

    @Override
    public int calculateTickSize(int startPrice) {
        if (startPrice < LOW_PRICE_THRESHOLD) {
            return LOW_PRICE_TICK;
        } else if (startPrice < MID_PRICE_THRESHOLD) {
            return MID_PRICE_TICK;
        } else if (startPrice < HIGH_PRICE_THRESHOLD) {
            return HIGH_PRICE_TICK;
        } else {
            return PREMIUM_TICK;
        }
    }

    @Override
    public int calculateMinBidPrice(int currentPrice, int startPrice) {
        int tickSize = calculateTickSize(startPrice);
        return currentPrice + tickSize;
    }
} 