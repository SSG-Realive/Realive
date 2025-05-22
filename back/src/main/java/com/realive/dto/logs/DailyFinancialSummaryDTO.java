package com.realive.dto.logs;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter

public class DailyFinancialSummaryDTO {
    
    //어느 날짜의 통계인지
    private final LocalDate date;

    //해당일 총 판매액
    private final Long totalSalesAmount;

    //해당일 총 판매 건수//
    private final Long totalSalesCount;

    //해당일에 정산 처리된 총 금액//
    private final Long totalPayoutAmountOnDate;

    //해당일 총 발생 수수료
    private final Long totalCommissionAmount;

    /// ///
    private final LocalDateTime recordedAt;

    @Builder
    public DailyFinancialSummaryDTO(LocalDate date, Long totalSalesAmount, Long totalSalesCount,
                                    Long totalPayoutAmountOnDate, Long totalCommissionAmount,
                                    LocalDateTime recordedAt) {
        this.date = date;
        this.totalSalesAmount = totalSalesAmount;
        this.totalSalesCount = totalSalesCount;
        this.totalPayoutAmountOnDate = totalPayoutAmountOnDate;
        this.totalCommissionAmount = totalCommissionAmount;
        this.recordedAt = recordedAt;
    }



}
