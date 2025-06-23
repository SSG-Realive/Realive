// QnA 목록 조회 응답 DTO
export interface SellerQnaResponse {
  id: number;
  title: string;
  content: string;
  answer: string | null;
  isAnswered: boolean;
  createdAt: string;   // LocalDateTime → string 으로 받음
  updatedAt: string;
  answeredAt: string | null;
  isActive: boolean;
}

// QnA 상세 조회 응답 DTO
export interface SellerQnaDetailResponse extends SellerQnaResponse {
  sellerId: number;
  sellerName: string;
  sellerEmail: string;
}
//qna 목록 조회 응답 dto
export interface SellerQnaListResponse {
    content: SellerQnaResponse[];
    totalElements: number;
    size: number;
    totalPages: number;
    number: number; // 현재 페이지 번호 (0-based)
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
}