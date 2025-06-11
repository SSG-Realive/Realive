import axios from 'axios';

const apiClient = axios.create({
  baseURL: process.env.NEXT_PUBLIC_API_BASE_URL, // "http://localhost:8080/api"
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // 쿠키 기반 인증을 함께 보내려면 true
});

// 요청 인터셉터 예시: 토큰이 있다면 자동으로 헤더에 넣기
apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken');
   console.log('→ Interceptor: token=', token);
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
     console.log('→ Interceptor: Authorization 헤더 설정됨:', config.headers.Authorization);
  }
  return config;
});



export default apiClient;
