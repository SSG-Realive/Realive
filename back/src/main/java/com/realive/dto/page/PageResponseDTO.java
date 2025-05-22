package com.realive.dto.page;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class PageResponseDTO<T> {

    private int page;
    private int size;
    private int total;

    private int start;
    private int end;

    private boolean prev;
    private boolean next;

    private List<T> dtoList;

    @Builder(builderMethodName = "withAll")
    private PageResponseDTO(PageRequestDTO pageRequestDTO, List<T>dtoList, int total){
        this.page = pageRequestDTO.getPage();
        this.size = pageRequestDTO.getSize();
        this.total = total;
        this.dtoList = (dtoList != null) ? dtoList : new ArrayList<>();

        this.end = (int)(Math.ceil(this.page/ 10.0)) * 10;
        this.start = this.end - 9;


        int last = (int) Math.ceil((double) total / size);
        
        if (this.end > last) {
            this.end = last;
            
        }

        this.prev = this.start > 1 ;
        this.next = end < last;

    }
    
}
