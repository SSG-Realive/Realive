import apiClient from '@/lib/apiClient';

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  email: string;
  name: string;
}

// 로그인 요청
export async function login(email: string, password: string): Promise<LoginResponse> {
  const response = await apiClient.post<LoginResponse>('/seller/login', { email, password });
  return response.data;
}
//로그아웃 요청
export async function logout(): Promise<void> {
  // 1) 백엔드 /seller/logout 호출 → refreshToken 쿠키 만료
  await apiClient.post('/seller/logout');
}

// 프로필 조회
export interface SellerProfile {
  id: number;
  email: string;
  name: string;
  phone: string;
}
export async function getProfile(): Promise<SellerProfile> {
  const response = await apiClient.get<SellerProfile>('/seller/me');
  return response.data;
}

// 프로필 수정
export interface SellerUpdateRequest {
  name: string;
  phone: string;
  password?: string;
}
// 백엔드가 PUT 으로 받으니 여기서도 PUT으로 바꿔야 합니다.
export async function updateProfile(data: SellerUpdateRequest): Promise<void> {
  await apiClient.put('/seller/me', data);
}
//대시보드
export interface SellerDashboardResponse{
  totalProducts : number;
  todayProducts : number;
  totalQna : number;
  unansweredQna : number;
  activeOrders : number;
}
export async function getDashboard() : Promise<SellerDashboardResponse> {
  const response = await apiClient.get('/seller/dashboard');
  return response.data;
}


export interface ProductResponse {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  width: number;
  depth: number;
  height: number;
  status: string;
  isActive: boolean;
  imageThumbnailUrl?: string;
  videoThumbnailUrl?: string;
  categoryName?: string;
  sellerName?: string;
}

export interface ProductListItem {
  id: number;
  name: string;
  price: number;
  status: string;
  isActive: boolean;
  imageThumbnailUrl?: string;
  parentCategoryName?: string;
  categoryName?: string;
  sellerName?: string;
}

export interface ProductSearchCondition {
  page?: number;
  size?: number;
  keyword?: string;
  minPrice?: number;
  maxPrice?: number;
  categoryId?: number;
  status?: string;
  isActive?: boolean;
}

/**
 * 🔹 상품 등록
 */
export async function createProduct(formData: FormData): Promise<number> {
  const res = await apiClient.post('/api/seller/products', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return res.data;
}

/**
 * 🔹 상품 수정
 */
export async function updateProduct(id: number, formData: FormData): Promise<void> {
  await apiClient.put(`/api/seller/products/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
}

/**
 * 🔹 상품 삭제
 */
export async function deleteProduct(id: number): Promise<void> {
  await apiClient.delete(`/api/seller/products/${id}`);
}

/**
 * 🔹 상품 단건 상세 조회
 */
export async function getProductDetail(id: number): Promise<ProductResponse> {
  const res = await apiClient.get(`/api/seller/products/${id}`);
  return res.data;
}

/**
 * 🔹 판매자 상품 목록 조회
 */
export async function getMyProducts(params?: ProductSearchCondition): Promise<{
  dtoList: ProductListItem[];
  total: number;
}> {
  const res = await apiClient.get('/api/seller/products', {
    params,
  });
  return res.data;
}

/**
 * 🔹 관리자 전체 상품 목록 조회
 */
export async function getAllProductsForAdmin(params?: ProductSearchCondition): Promise<{
  dtoList: ProductListItem[];
  total: number;
}> {
  const res = await apiClient.get('/api/seller/products/admin/products', {
    params,
  });
  return res.data;
}
