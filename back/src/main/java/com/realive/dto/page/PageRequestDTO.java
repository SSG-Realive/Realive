package com.realive.dto.page;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 페이지네이션 요청 DTO
 * - 컨트롤러에서 페이징, 정렬, 검색 키워드를 처리할 때 사용
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequestDTO {

    // 요청할 페이지 번호 (기본값: 1)
    private int page = 1;

    // 한 페이지당 데이터 수 (기본값: 10)
    private int size = 10;

    // 정렬 기준 컬럼명 (기본값: createdAt)
    private String sort = "createdAt";

    // 정렬 방향 (기본값: DESC)
    private String direction = "DESC";

    // 검색 키워드 (선택)
    private String keyword;

    /**
     * 내부적으로 0부터 시작하는 페이지 인덱스로 변환
     * 예: page가 1이면 index는 0
     */
    public int getPageIndex() {
        return (page <= 0) ? 0 : page - 1;
    }

    /**
     * Pageable 객체로 변환
     * - Spring Data JPA에서 페이징 쿼리 작성 시 사용
     * - 정렬 기준 및 방향 포함
     */
    public Pageable toPageable() {
        Sort.Direction dir = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.DESC);
        return PageRequest.of(getPageIndex(), size, Sort.by(dir, sort));
    }
}