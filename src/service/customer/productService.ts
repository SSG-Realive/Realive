import apiClient from '@/lib/apiClient';
import { FeaturedSellerWithProducts } from '@/types/product';
import { ProductListDTO } from '@/types/seller/product/product';
import { ProductDetail } from '@/types/seller/product/product';


 //🔍 [공개] 카테고리별 상품 목록 조회

export async function fetchPublicProducts(
    categoryId: number | null,
    page: number,
    size: number,
    keyword?: string // ✅ 검색어 추가
): Promise<ProductListDTO[]> {
    const params: Record<string, any> = {
        page,
        size,
    };
    if (categoryId !== null) params.categoryId = categoryId;
    if (keyword) params.keyword = keyword;
    params.type = 'T'; // ✅ 상품명 검색만 허용

    const res = await apiClient.get('/public/items', { params });
    return res.data.dtoList;
}


 //* 🔍 [공개] 상품 상세 조회

export async function fetchProductDetail(productId: number): Promise<ProductDetail> {
    const res = await apiClient.get(`/public/items/${productId}`);
    return res.data;
}

 // 관련 상품 추천

export async function fetchRelatedProducts(productId: number): Promise<ProductListDTO[]> {
    const res = await apiClient.get(`/public/items/${productId}/related`);
    return res.data;
}

export async function fetchPopularProducts(): Promise<ProductListDTO[]> {
    const res = await apiClient.get('/public/items/popular');
    return res.data;
}

 /* 추천 판매자+상품 목록을 서버에서 가져옵니다.
 */
export async function fetchFeaturedSellersWithProducts(
  candidateSize = 10,
  sellersPick = 1,
  productsPerSeller = 3,
  minReviews = 3
): Promise<FeaturedSellerWithProducts[]> {
  const response = await apiClient.get<FeaturedSellerWithProducts[]>(
    '/public/items/featured-sellers-with-products',
    {
      params: { candidateSize, sellersPick, productsPerSeller, minReviews },
    }
  );
  return response.data;
}