// src/lib/apiFactory.ts (새 파일)

import axios, { type AxiosInstance } from 'axios';

// 인자로 받을 스토어의 최소 타입을 정의합니다.
// token을 가져오고 logout을 실행할 수 있는 스토어여야 합니다.
interface AuthStore {
   getState: () => {
    accessToken: string | null;   // ✅ 필드명 변경
    refreshToken: string | null;  //   (리프레시 토큰은 인터셉터 안 쓸 수도 있지만 함께 둡니다)
    logout: () => void;
  };
}

// Axios 인스턴스를 생성하는 '팩토리' 함수입니다.
export function createApiClient(store: AuthStore): AxiosInstance {
  const api = axios.create({
    baseURL: process.env.NEXT_PUBLIC_API_BASE_URL,
    headers: { 'Content-Type': 'application/json' },
  });

  // 요청 인터셉터: 요청을 보내기 전에 토큰을 헤더에 추가합니다.
  api.interceptors.request.use(
    (config) => {
      const { accessToken } = store.getState();   // ✅
      if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`;
      }
      return config;
    },
    (error) => Promise.reject(error)
  );

  // 응답 인터셉터: 401 Unauthorized 에러 발생 시 자동으로 로그아웃 처리합니다.
  api.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // 토큰이 만료되었거나 유효하지 않으므로 스토어에서 로그아웃 처리합니다.
        store.getState().logout();
        // 필요 시 로그인 페이지로 리디렉션 할 수 있습니다.
        // window.location.href = '/login';
      }
      return Promise.reject(error);
    }
  );

  return api;
}