import { useAuthStore } from '@/store/customer/authStore';
import { useAdminAuthStore } from '@/store/admin/useAdminAuthStore';
import axios from 'axios';

// [customer] Zustand로 로그인 상태를 관리

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_ROOT_URL,
});

// 토큰이 필요없는 public API 경로들
const publicPaths = [
  '/public/auth/login',
  '/public/auth/join',
  '/public/auth/login',
  '/public/auth/join',
  '/seller/login',
  '/admin/login'
];

api.interceptors.request.use((config) => {
  // public API 경로인 경우 토큰을 포함하지 않음
  const isPublicPath = publicPaths.some(path => config.url?.includes(path));
  if (!isPublicPath) {
    const token = useAuthStore.getState().accessToken;
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
    }
    return Promise.reject(error);
  }
);

// 관리자용 adminApi 인스턴스 추가
const adminApi = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
});

adminApi.interceptors.request.use((config) => {
  // useAdminAuthStore에서 토큰 가져오기
  const token = useAdminAuthStore.getState().accessToken;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

adminApi.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('AdminApi Error:', error.response?.status, error.response?.data);
    if (error.response?.status === 401 || error.response?.status === 403) {
      console.log('관리자 인증 만료, 로그아웃 처리');
      useAdminAuthStore.getState().logout();
      if (typeof window !== 'undefined') {
        window.location.href = '/admin/login';
      }
    }
    return Promise.reject(error);
  }
);

export { adminApi };

export default api; 