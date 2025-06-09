export interface SellerQnaResponseDTO {
  id: number;
  title: string;
  content: string;
  answer: string | null;
  isAnswered: boolean;
  createdAt: string;
  updatedAt: string;
  answeredAt: string | null;
}

export interface SellerQnaDetailResponseDTO {
  id: number;
  title: string;
  content: string;
  answer: string | null;
  isAnswered: boolean;
  createdAt: string;
  updatedAt: string;
  answeredAt: string | null;
  sellerId: number;
  sellerName: string;
  sellerEmail: string;
}

export interface SellerQnaRequestDTO {
  title: string;
  content: string;
}

export interface SellerQnaUpdateRequestDTO {
  title: string;
  content: string;
}

export interface SellerQnaAnswerRequestDTO {
  answer: string;
}
