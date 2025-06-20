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
