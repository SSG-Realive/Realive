// 고객 QnA 목록 조회 응답 DTO
export interface CustomerQnaResponse {
  id: number;
  title: string;
  content: string;
  answer: string | null;
  isAnswered: boolean;
  createdAt: string;
  updatedAt: string;
  answeredAt: string | null;
  customerName: string;
  productName: string;
  productId: number;
  answered: boolean | string;
}

// 고객 QnA 상세 조회 응답 DTO
export interface CustomerQnaDetailResponse extends CustomerQnaResponse {
  answered: boolean | string;
}

// 고객 QnA 목록 조회 응답 DTO (페이징)
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