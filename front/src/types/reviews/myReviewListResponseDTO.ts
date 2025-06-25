
import { ReviewResponseDTO } from "@/types/reviews/reviewResponseDTO";

export interface MyReviewListResponse {
    content: ReviewResponseDTO[]; // 리뷰 목록 (실제 데이터)
    totalElements: number;        // 전체 아이템 개수 (totalCount 대신 사용)
    totalPages: number;           // 전체 페이지 수
    number: number;               // 현재 페이지 번호 (0부터 시작)
    size: number;                 // 페이지당 아이템 개수
    first: boolean;               // 첫 페이지인지 여부
    last: boolean;                // 마지막 페이지인지 여부
    empty: boolean;               // 내용이 비어있는지 여부
    // 기타 Spring Page 필드 (pageable, sort, numberOfElements 등)는 필요에 따라 추가
}