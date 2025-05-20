package com.realive.dto.log;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyFinancialSummaryDTO {
    
    //어느 날짜의 통계인지
    private LocalDate date;

    //해당일 총 판매액
    private Long totalSalesAmount;

    //해당일 총 판매 건수
    private Long totalSalesCount;

    //해당일에 정산 처리된 총 금액
    private Long totalPayoutAmountOnDate;

    //해당일 총 발생 수수료
    private Long totalCommissionAmount;

}
