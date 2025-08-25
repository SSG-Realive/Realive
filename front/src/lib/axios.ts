// import { useAuthStore } from '@/store/customer/authStore';
// import axios, { AxiosHeaders } from 'axios';

// const api = axios.create({
//   baseURL: process.env.NEXT_PUBLIC_API_ROOT_URL,
// });

// // 토큰이 필요 없는 public API 경로들 (루트 경로 기준으로 변경 가능)
// const publicPaths = [
//   '/api/public/auth/login',
//   '/api/public/auth/join'
// ];

// api.interceptors.request.use((config) => {
//   const url = config.url || '';

//   const isPublicPath = publicPaths.some(path => url.startsWith(path));

//   if (!isPublicPath) {
//     const accessToken = useAuthStore.getState().accessToken;
//     if (accessToken) {
//       if (config.headers) {
//         if (typeof (config.headers as AxiosHeaders).set === 'function') {
//           // AxiosHeaders 인스턴스일 경우
//           (config.headers as AxiosHeaders).set('Authorization', `Bearer ${accessToken}`);
//         } else {
//           // 일반 객체 타입일 경우
//           (config.headers as Record<string, string>)['Authorization'] = `Bearer ${accessToken}`;
//         }
//       } else {
//         // headers가 없으면 AxiosHeaders 객체로 새로 생성
//         config.headers = new AxiosHeaders({ Authorization: `Bearer ${accessToken}` });
//       }
//     }
//   }

//   return config;
// });


// api.interceptors.response.use(
//   (response) => response,
//   (error) => {
//     if (error.response?.status === 401) {
//       useAuthStore.getState().logout();
//     }
//     return Promise.reject(error);
//   }
// );

// export default api;
