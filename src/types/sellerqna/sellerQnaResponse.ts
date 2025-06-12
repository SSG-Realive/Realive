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
}

// QnA 상세 조회 응답 DTO
export interface SellerQnaDetailResponse extends SellerQnaResponse {
  sellerId: number;
  sellerName: string;
  sellerEmail: string;
}