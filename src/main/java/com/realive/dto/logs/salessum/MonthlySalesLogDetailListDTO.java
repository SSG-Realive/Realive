package com.realive.dto.logs.salessum;

import com.realive.dto.logs.SalesLogDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.YearMonth;
import java.util.List;

// 월별 상세 판매내역 리스트 DTO
@Getter
@AllArgsConstructor
@Builder
public class MonthlySalesLogDetailListDTO {
    private final YearMonth month;
    private final List<SalesLogDTO> salesLogs;
}