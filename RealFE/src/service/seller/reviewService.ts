import { sellerApi } from '@/lib/apiClient';
import { SellerReviewListResponse, SellerReviewStatistics, ReviewFilterOptions } from '@/types/seller/review';

// 판매자의 리뷰 목록 조회
export const getSellerReviews = async (
  page: number = 0,
  size: number = 10,
  filters?: ReviewFilterOptions
): Promise<SellerReviewListResponse> => {
  const params: any = { page, size };
  
  if (filters?.productName) {
    params.productName = filters.productName;
  }
  if (filters?.rating) {
    params.rating = filters.rating;
  }
  if (filters?.sortBy) {
    params.sortBy = filters.sortBy;
  }

  console.log('🔍 리뷰 API 호출:', { url: '/seller/reviews', params });
  const response = await sellerApi.get('/seller/reviews', { params });
  console.log('✅ 리뷰 API 응답:', response.data);
  
  return response.data;
};

// 판매자의 리뷰 통계 조회
export const getSellerReviewStatistics = async (): Promise<SellerReviewStatistics> => {
  console.log('🔍 리뷰 통계 API 호출:', { url: '/seller/reviews/statistics' });
  const response = await sellerApi.get('/seller/reviews/statistics');
  console.log('✅ 리뷰 통계 API 응답:', response.data);
  return response.data;
}; 