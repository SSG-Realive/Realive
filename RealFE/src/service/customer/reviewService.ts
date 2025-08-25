import apiClient from '@/lib/apiClient';
import { ReviewCreateRequestDTO, ReviewResponseDTO } from '@/types/customer/review/review';

// 리뷰 = 판매자리뷰 

// 판매자별 리뷰 리스트 조회
export async function fetchReviewsBySeller(sellerId: number): Promise<ReviewResponseDTO[]> {
    const res = await apiClient.get(`/public/items/reviews/seller/${sellerId}`);
    return res.data.reviews ?? [];
}

// 내 리뷰 리스트 조회
export async function fetchMyReviews(): Promise<ReviewResponseDTO[]> {
    const res = await apiClient.get('/customer/reviews/my');
    return res.data.content ?? [];
}

// 리뷰 상세조회
export async function fetchReviewDetail(reviewId: number): Promise<ReviewResponseDTO> {
    const res = await apiClient.get(`/customer/reviews/${reviewId}`);
    return res.data;
}

// 리뷰 작성
export async function createReview(data: ReviewCreateRequestDTO): Promise<ReviewResponseDTO> {
    const res = await apiClient.post('/customer/reviews', data);
    return res.data;
}

// 리뷰 삭제
export async function deleteReview(reviewId: number): Promise<void> {
    await apiClient.delete(`/customer/reviews/${reviewId}`);
}

// 리뷰 수정
export async function updateReview(
    reviewId: number,
    data: { content: string; rating: number; imageUrls: string[] }
): Promise<void> {
    await apiClient.put(`/customer/reviews/${reviewId}`, data);
}