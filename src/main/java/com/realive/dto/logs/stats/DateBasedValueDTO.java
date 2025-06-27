package com.realive.dto.logs.stats;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class DateBasedValueDTO<T> { // T는 값의 타입 (Long, Double 등)
    private LocalDate date;
    private T value;
}
