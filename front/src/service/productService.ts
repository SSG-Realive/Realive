import apiClient from '@/lib/apiClient';
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
    const query = new URLSearchParams(searchParams).toString();
    const res = await apiClient.get(`/seller/products?${query}`);
    return res.data;
}

/**
 * 상품 삭제 API
 */
export async function deleteProduct(id: number): Promise<void> {
    await apiClient.delete(`/seller/products/${id}`);
}