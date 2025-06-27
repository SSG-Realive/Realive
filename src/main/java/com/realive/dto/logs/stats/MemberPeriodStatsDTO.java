package com.realive.dto.logs.stats;


import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MemberPeriodStatsDTO {
    private MemberSummaryStatsDTO summary;
    private List<DateBasedValueDTO<Long>> dailyNewUserTrend;    // 일별 신규 가입자 추이
    private List<DateBasedValueDTO<Long>> dailyActiveUserTrend; // 일별 활동 사용자 추이
    private List<MonthBasedValueDTO<Long>> monthlyNewUserTrend;  // 월별 신규 가입자 추이
    private List<MonthBasedValueDTO<Long>> monthlyActiveUserTrend; // 월별 활동 사용자 추이
}
