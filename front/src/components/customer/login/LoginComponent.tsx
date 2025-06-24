'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import { LoginResponse } from '@/types/customer/login/login';


export default function LoginForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const setAuth = useAuthStore((state) => state.setAuth);

  const [formData, setFormData] = useState({
    email: '',
    password: '',
  });
  const [error, setError] = useState('');

  /* 카카오 로그인 실패 메시지 */
  useEffect(() => {
    const errorParam = searchParams?.get('error');
    if (errorParam === 'kakao_login_failed') {
      setError('카카오 로그인에 실패했습니다. 다시 시도해주세요.');
    }
  }, [searchParams]);

  /* 인풋 변경 핸들러 */
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  /* 로그인 제출 */
  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError('');

    const payload = {
      email: formData.email.trim(),
      password: formData.password,
    };

    try {
      // 환경변수가 없으면 기본값 사용
      const apiBaseUrl = process.env.NEXT_PUBLIC_API_BASE_UR || 'http://localhost:8080/api';
      const apiUrl = `${apiBaseUrl}/public/auth/login`;
      
      console.log('로그인 API URL:', apiUrl);
      console.log('환경변수 NEXT_PUBLIC_API_ROOT_URL:', process.env.NEXT_PUBLIC_API_ROOT_URL);

      const res = await fetch(apiUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const data: LoginResponse = await res.json();

      if (!res.ok) {
        const message = (data as any).message ?? '로그인에 실패했습니다.';
        setError(message);
        return;
      }

      // 로그인 성공 후
      if (data.accessToken && data.email && data.name) {
        setAuth({
          id: data.id,
          accessToken: data.accessToken,
          refreshToken: data.refreshToken || null,
          email: data.email,
          userName: data.name,
          temporaryUser: false,
        });
        const redirectTo = searchParams?.get('redirectTo') || '/main';
        router.push(redirectTo);
      } else {
        setError('로그인 응답이 올바르지 않습니다.');
      }
    } catch (err) {
      console.error('Login error:', err);
      setError('로그인 처리 중 오류가 발생했습니다.');
    }
  };

  /* 카카오 로그인 */
  const handleKakaoLogin = () => {
    const state = crypto.randomUUID();
    const original = new URLSearchParams(window.location.search).get('redirectTo') || '/';
    sessionStorage.setItem('loginRedirectUrl', original);

    const redirectTo = encodeURIComponent(`${window.location.origin}/customer/oauth/callback`);
    window.location.href =
      `${process.env.NEXT_PUBLIC_API_ROOT_URL}/oauth2/authorization/kakao?state=${state}&redirectTo=${redirectTo}`;
  };

  /* ---------- UI ---------- */
  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label htmlFor="email" className="block text-sm font-medium text-gray-700">이메일</label>
        <input
          id="email"
          name="email"
          type="email"
          value={formData.email}
          onChange={handleChange}
          required
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          placeholder="이메일을 입력하세요"
        />
      </div>
      <div>
        <label htmlFor="password" className="block text-sm font-medium text-gray-700">비밀번호</label>
        <input
          id="password"
          name="password"
          type="password"
          value={formData.password}
          onChange={handleChange}
          required
          className="mt-1 block w-full rounded-md border border-gray-300 px-3 py-2"
          placeholder="비밀번호를 입력하세요"
        />
      </div>
      {error && <p className="text-sm text-center text-red-500">{error}</p>}
      <button
        type="submit"
        className="w-full rounded bg-blue-600 py-2 text-white hover:bg-blue-700"
      >
        로그인
      </button>

      <div className="relative my-4">
        <div className="absolute inset-0 flex items-center">
          <div className="w-full border-t border-gray-300"></div>
        </div>
        <div className="relative flex justify-center text-sm">
          <span className="bg-white px-2 text-gray-500">또는</span>
        </div>
      </div>

      <button
        type="button"
        onClick={handleKakaoLogin}
        className="flex w-full items-center justify-center gap-2 rounded bg-[#FEE500] py-2 px-4 text-[#000000] hover:bg-[#FDD835]"
        aria-label="카카오로 로그인"
      >
        <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
          <path
            fillRule="evenodd"
            clipRule="evenodd"
            d="M12 3C6.477 3 2 6.463 2 10.702c0 2.682 1.76 5.035 4.4 6.4-.19.882-.74 3.22-.85 3.72-.13.55.2.53.37.39.14-.11 2.2-1.51 2.2-1.51 1.17.17 2.38.26 3.58.26 5.523 0 10-3.463 10-7.702S17.523 3 12 3z"
            fill="#000000"
          />
        </svg>
        카카오로 로그인
      </button>
    </form>
  );
}
