package com.realive.dto.page;


import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {
    
    private int page = 1;
    private int size = 10;
    private String sort = "createdAt";
    private String direction = "DESC";
    private String keyword;

    public int getPageIndex(){
        return (page <= 0) ? 0 : page -1;
    }

    public Pageable toPageable(){
        Sort.Direction dir = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.DESC);
        return PageRequest.of(getPageIndex(), size, Sort.by(dir, sort));
    }




}
