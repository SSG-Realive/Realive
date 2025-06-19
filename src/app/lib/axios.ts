import { useAuthStore } from '@/store/customer/authStore';
import axios from 'axios';

// [customer] Zustand로 로그인 상태를 관리

const api = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_ROOT_URL,
});

// 토큰이 필요없는 public API 경로들
const publicPaths = [
  '/public/auth/login',
  '/public/auth/join',
  '/api/public/auth/login',
  '/api/public/auth/join',
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
  baseURL: process.env.NEXT_PUBLIC_API_ROOT_URL,
});
adminApi.interceptors.request.use((config) => {
  const token = typeof window !== 'undefined' ? localStorage.getItem('adminToken') : null;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export { adminApi };

export default api; 