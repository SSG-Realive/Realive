package com.realive.dto.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * PageRequestDTO
 * - 클라이언트에서 요청한 페이징 및 정렬 정보를 담는 DTO
 * - Spring Data의 Pageable로 변환 가능
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {

    private int page = 1;               // 요청한 페이지 번호 (1부터 시작, 내부 로직에서는 0-based로 변환됨)
    private int size = 10;              // 페이지당 데이터 개수 (기본값: 10)
    private String sort = "createdAt";  // 정렬 기준 필드명 (기본값: createdAt)
    private String direction = "DESC";  // 정렬 방향 (ASC 또는 DESC, 기본값: DESC)
    private String keyword;             // 검색 키워드 (선택적으로 사용 가능)

    /**
     * 0-based 페이지 인덱스를 반환
     * 클라이언트는 보통 1-based로 요청하므로, 내부적으로는 0으로 변환
     */
    public int getPageIndex() {
        return (page <= 0) ? 0 : page - 1;
    }

    /**
     * PageRequest (Pageable 구현체)로 변환
     * - 정렬 방향에 따라 Sort 객체 생성
     * - getPageIndex()를 통해 0-based 페이지 번호 사용
     */
    public Pageable toPageable() {
        Sort.Direction dir = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.DESC);
        return PageRequest.of(getPageIndex(), size, Sort.by(dir, sort));
    }
}