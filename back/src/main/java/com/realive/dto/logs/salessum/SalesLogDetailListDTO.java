package com.realive.dto.logs.salessum;

import com.realive.dto.logs.SalesLogDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

// 일자별 상세 판매내역 리스트 DTO
@Getter
@AllArgsConstructor
@Builder
public class SalesLogDetailListDTO {
    private final LocalDate date;
    private final List<SalesLogDTO> salesLogs;
}