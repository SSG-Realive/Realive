import apiClient from '@/lib/apiClient';
import { ProductSearchCondition } from '@/types/filter/productSearchCondition';
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
    const res = await apiClient.post('/seller/products/new', formData, {
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
 * 🔹 판매자 상품 목록 조회
 */
export async function getMyProducts(
  params?: ProductSearchCondition
): Promise<PageResponse<ProductListItem>> {
  const res = await apiClient.get('/seller/products', { params });
  return res.data;
}


/**
 * 상품 삭제 API
 */
export async function deleteProduct(id: number): Promise<void> {
    await apiClient.delete(`/seller/products/${id}`);
}