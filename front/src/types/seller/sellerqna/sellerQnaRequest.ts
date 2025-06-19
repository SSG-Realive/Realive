// QnA 등록 요청 DTO
export interface SellerCreateQnaRequest {
  title: string;
  content: string;
}

// QnA 수정 요청 DTO
export interface SellerQnaUpdateRequest {
  title: string;
  content: string;
}

// QnA 답변 등록 요청 DTO
export interface SellerQnaAnswerRequest {
  answer: string;
}