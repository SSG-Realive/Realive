import apiClient from '@/lib/apiClient';
import { ReviewResponseDTO } from '@/types/customer/review/review';

export async function fetchReviewsBySeller(sellerId: number): Promise<ReviewResponseDTO[]> {
    const res = await apiClient.get(`/reviews/seller/${sellerId}`);
    return res.data.reviews ?? [];
}

export async function fetchMyReviews(): Promise<ReviewResponseDTO[]> {
    const res = await apiClient.get('/reviews/my');
    return res.data.content ?? [];
}

export async function fetchReviewDetail(reviewId: number): Promise<ReviewResponseDTO> {
    const res = await apiClient.get(`/reviews/${reviewId}`);
    return res.data;
}

export async function getReviewDetail(reviewId: number): Promise<ReviewResponseDTO> {
    const res = await apiClient.get(`/reviews/${reviewId}`);
    return res.data;
}

export async function deleteReview(reviewId: number): Promise<void> {
    await apiClient.delete(`/reviews/${reviewId}`);
}

export async function updateReview(
    reviewId: number,
    data: { content: string; rating: number }
): Promise<void> {
    await apiClient.put(`/reviews/${reviewId}`, data);
}