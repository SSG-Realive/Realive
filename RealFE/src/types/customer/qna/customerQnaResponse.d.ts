// src/types/customer/qna/customerQnaResponse.d.ts

// 백엔드 CustomerQnaListResponseDto와 일치하도록 정의 (content와 answer 포함)
export interface CustomerQnaResponse {
    id: number;
    title: string;
    content: string;         // 백엔드 DTO에 추가된 content 필드
    answer: string | null;   // 백엔드 DTO에 추가된 answer 필드
    createdAt: string;       // LocalDateTime은 string으로 받음
    isAnswered: boolean;

    // 백엔드 DTO에 추가한 필드가 있다면 여기에 추가합니다.
    // customerName?: string;
    // productName?: string;
    // productId?: number;
}

// QnA 목록 전체 응답 (백엔드가 List<DTO>를 반환하므로, content 배열에 해당 리스트를 담는 형태로 가공)
export interface CustomerQnaListResponse {
    content: CustomerQnaResponse[];
    totalElements: number;
    size: number;
    totalPages: number;
    number: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
}