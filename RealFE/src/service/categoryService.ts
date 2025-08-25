// src/service/categoryService.ts
import apiClient from '@/lib/apiClient';
import { Category } from '@/types/common/category';

export async function fetchAllCategories(): Promise<Category[]> {
    const res = await apiClient.get('/seller/categories'); // 기존 API 그대로 사용
    return res.data; // API가 List<CategoryDTO> 그대로 내려주므로
}

export async function fetchSubCategories(parentId: number): Promise<Category[]> {
    const res = await apiClient.get(`/categories?parentId=${parentId}`);
    return res.data.categories; // or res.data depending on API response
}