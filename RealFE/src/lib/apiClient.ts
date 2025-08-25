// src/lib/apiClient.ts (기존 파일을 이 내용으로 교체)

import { useAdminAuthStore } from '@/store/admin/useAdminAuthStore';
import { createApiClient } from './apiFactory';
import { useAuthStore } from '@/store/customer/authStore';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore';

// Customer용 API 클라이언트
export const customerApi = createApiClient(useAuthStore);

// Seller용 API 클라이언트
export const sellerApi = createApiClient(useSellerAuthStore);

export const adminApi = createApiClient(useAdminAuthStore);

// 기본적으로는 customerApi를 내보내거나,
// 혹은 사용하는 곳에서 명시적으로 customerApi, sellerApi를 import해서 사용합니다.
export default customerApi;

