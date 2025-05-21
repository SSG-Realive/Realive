package com.realive.dto.page;

import java.util.ArrayList;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 페이지 응답 DTO
 * - 페이징 처리된 결과를 클라이언트에게 전달하기 위한 제네릭 DTO
 * - 페이징 정보 + 실제 결과 리스트(dtoList)를 함께 전달
 */
@Getter
@ToString
public class PageResponseDTO<T> {

    // 현재 페이지 번호
    private int page;

    // 한 페이지당 보여줄 데이터 개수
    private int size;

    // 전체 데이터 개수
    private int total;

    // 페이지네이션 시작 번호
    private int start;

    // 페이지네이션 끝 번호
    private int end;

    // 이전 페이지 블럭 존재 여부
    private boolean prev;

    // 다음 페이지 블럭 존재 여부
    private boolean next;

    // 결과 DTO 리스트 (예: 게시글 목록, 상품 목록 등)
    private List<T> dtoList;

    /**
     * 생성자 - Builder 패턴 사용
     * @param pageRequestDTO 요청 DTO (page, size 정보 포함)
     * @param dtoList 실제 데이터 리스트
     * @param total 전체 데이터 수
     */
    @Builder(builderMethodName = "withAll")
    private PageResponseDTO(PageRequestDTO pageRequestDTO, List<T> dtoList, int total) {
        this.page = pageRequestDTO.getPage();
        this.size = pageRequestDTO.getSize();
        this.total = total;

        // 데이터 리스트가 null인 경우 빈 리스트로 초기화
        this.dtoList = (dtoList != null) ? dtoList : new ArrayList<>();

        // 화면에 보여줄 마지막 페이지 번호 계산 (10개 단위 블럭)
        this.end = (int)(Math.ceil(this.page / 10.0)) * 10;

        // 시작 페이지 번호 계산
        this.start = this.end - 9;

        // 전체 마지막 페이지 번호 계산
        int last = (int) Math.ceil((double) total / size);

        // end가 실제 마지막 페이지를 초과하지 않도록 조정
        if (this.end > last) {
            this.end = last;
        }

        // 이전 페이지 블럭 존재 여부
        this.prev = this.start > 1;

        // 다음 페이지 블럭 존재 여부
        this.next = end < last;
    }
}