package com.realive.dto.page;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter 
@ToString
public class PageResponseDTO<E> {

    private int page, size, total, start, end;
    private boolean prev, next;
    private List<E> dtoList;

    @Builder(builderMethodName = "withAll")
    public PageResponseDTO(PageRequestDTO pageRequestDTO, List<E> dtoList, int total){
        this.page = pageRequestDTO.getPage();
        this.size = pageRequestDTO.getSize();
        this.total = total;
        this.dtoList = dtoList == null ? new ArrayList<>() : dtoList;

        // 페이지 범위 설정 ex) 12: 11~20
        this.end = (int)(Math.ceil(this.page / 10.0)) * 10; // Math.ceil: 올림
        this.start = this.end - 9; 

        // end가 총 페이지 수(last)를 넘지 않도록 하는 로직
        int last = (int)(Math.ceil((total / (double) size)));
        this.end = Math.min(end, last); // end가 last보다 크면 last로 설정

        // 이전과 다음
        this.prev = start > 1;
        this.next = total > end * size;
    }
}
