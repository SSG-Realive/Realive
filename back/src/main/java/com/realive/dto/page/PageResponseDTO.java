package com.realive.dto.page;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * PageResponseDTO<T>
 * - 페이징 처리된 결과를 응답용으로 포장하는 DTO
 * - 페이지 정보, 전체 개수, 현재 페이지의 데이터 목록 등을 포함함
 *
 * @param <T> 응답 데이터의 타입 (ex: ProductDTO, SellerDTO 등)
 */
@Getter
@ToString
public class PageResponseDTO<T> {

    private int page;               // 현재 페이지 번호
    private int size;               // 한 페이지당 항목 수
    private int total;              // 전체 항목 수
    private int start;              // 페이지네이션의 시작 번호 (ex: 1, 11, 21...)
    private int end;                // 이지네이션의 끝 번호 (ex: 10, 20, 30...)
    private boolean prev;           // 이전 페이지 그룹 존재 여부
    private boolean next;           // 다음 페이지 그룹 존재 여부
    private List<T> dtoList;        // 현재 페이지에 해당하는 데이터 목록

    /**
     * 생성자 (Builder 사용)
     * - 페이징 요청 정보와 결과 리스트, 전체 개수를 받아 계산 및 초기화
     *
     * @param pageRequestDTO 페이징 요청 정보
     * @param dtoList        현재 페이지의 데이터 목록
     * @param total          전체 데이터 개수
     */
    @Builder(builderMethodName = "withAll")
    private PageResponseDTO(PageRequestDTO pageRequestDTO, List<T> dtoList, int total) {
        this.page = pageRequestDTO.getPage();
        this.size = pageRequestDTO.getSize();
        this.total = total;
        this.dtoList = (dtoList != null) ? dtoList : new ArrayList<>();

        // 한 블록당 10개 페이지를 보여줄 때의 끝 페이지 계산
        this.end = (int) (Math.ceil(this.page / 10.0)) * 10;
        this.start = this.end - 9;

        // 전체 페이지 수 계산
        int last = (int) Math.ceil((double) total / size);

        // 끝 페이지 번호가 실제 페이지 수보다 크면 보정
        if (this.end > last) {
            this.end = last;
        }

        // 이전/다음 페이지 그룹 존재 여부
        this.prev = this.start > 1;
        this.next = end < last;
    }
}