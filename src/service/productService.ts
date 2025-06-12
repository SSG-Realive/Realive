import apiClient from '@/lib/apiClient';
import { SellerCategoryDTO } from '@/types/category/sellerCategory';
import { PageResponse } from '@/types/page/pageResponse';
import { ProductDetail } from '@/types/product';

import { ProductListItem } from '@/types/productList';

// 판매자 상품 목록 조회 API
export async function fetchMyProducts(): Promise<ProductDetail[]> {
    const res = await apiClient.get('/seller/products');
    return res.data.content; // PageResponseDTO 기준
}

/**
 * 상품 등록 API
 */
export async function createProduct(formData: FormData): Promise<number> {
    const res = await apiClient.post('/seller/products', formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
    return res.data;
}

/**
 * 상품 수정 API
 */
export async function updateProduct(id: number, formData: FormData): Promise<void> {
    await apiClient.put(`/seller/products/${id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' },
    });
}

/**
 * 상품 단건 조회 API
 */
export async function getProductDetail(id: number): Promise<ProductDetail> {
    const res = await apiClient.get(`/seller/products/${id}`);
    return res.data;
}

/**
 * 상품 목록 조회 API (판매자)
 * @param searchParams - 페이지, 키워드 등 검색 조건
 */
export async function getMyProducts(searchParams: Record<string, any> = {}): Promise<PageResponse<ProductListItem>> {
    const query = buildSearchParams(searchParams); // ✅ 빈 값 빼고 쿼리스트링 구성
  console.log('→ 최종 요청 URL:', `/seller/products?${query}`); // 디버그 확인용

  const res = await apiClient.get(`/seller/products?${query}`);
  return res.data;
}

/**
 * 상품 삭제 API
 */
export async function deleteProduct(id: number): Promise<void> {
    await apiClient.delete(`/seller/products/${id}`);
}

//쿼리 빈값 제거
const buildSearchParams = (params: Record<string, any>): string => {
  const validParams = Object.entries(params)
    .filter(([_, v]) => v !== undefined && v !== null && v !== '') // 빈 값 제거
    .reduce((acc, [k, v]) => ({ ...acc, [k]: v }), {});

  return new URLSearchParams(validParams).toString();
};

//물건 등록시 카테고리 
export async function fetchCategories(): Promise<SellerCategoryDTO[]> {
    const res = await apiClient.get('/seller/categories');
    return res.data;
}