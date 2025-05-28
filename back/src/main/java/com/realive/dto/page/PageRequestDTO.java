package com.realive.dto.page;


import lombok.*;

@Getter
@Setter
@ToString
public class PageRequestDTO {


    private int page = 1;        // 현재 페이지 번호
    private int size = 10;       // 한 페이지 당 게시글 수
    private String type;         // 검색 조건 타입 (예: "T", "TC" 등)
    private String keyword;      // 검색어




    //기준위치
    public int getOffset() {
        return (page - 1) * size;
    }
    public int getLimit() {
        return this.size;
    }

}

