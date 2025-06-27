package com.realive.dto.logs.stats;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.YearMonth;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MonthBasedValueDTO<T> {
    private YearMonth yearMonth;
    private T value;
}
