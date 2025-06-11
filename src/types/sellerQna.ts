export interface SellerQnaResponse {
    id: number;
    title: string;        // 제목
    content: string;      // 본문
    answer: string | null;
    isAnswered: boolean;
    createdAt: string;
    updatedAt: string;
    answeredAt: string | null;
}

export interface SellerQnaDetailResponse {
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

export interface CreateQnaRequest {
    title: string;
    content: string;
}

export interface UpdateQnaRequest {
    title: string;
    content: string;
}