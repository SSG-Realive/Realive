'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';

export default function OAuthCallbackPage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const setTokens = useAuthStore((state) => state.setTokens);
  const setAuth = useAuthStore((state) => state.setAuth); 

  useEffect(() => {
    console.log('=== 콜백 페이지 실행 ===');
    console.log('현재 URL:', window.location.href);

    const accessToken = searchParams.get('token');
    const refreshToken = searchParams.get('refreshToken');
    const email = searchParams.get('email');
    const userName = searchParams.get('userName');
    const isTemporaryUser = searchParams.get('temporaryUser') === 'true';

    console.log('받은 파라미터들:', {
      accessToken: accessToken?.substring(0, 20) + '...',
      refreshToken: refreshToken?.substring(0, 20) + '...',
      email,
      userName,
      isTemporaryUser,
    });

    const originalUrl = sessionStorage.getItem('loginRedirectUrl');
    console.log('sessionStorage에서 가져온 원래 URL:', originalUrl);

    if (accessToken && email) {
      // 👇 인증 토큰만 먼저 저장
      setTokens(accessToken, refreshToken);

      // 👇 임시 유저인 경우: 소셜 회원가입 페이지로 이동
      if (isTemporaryUser) {
        // 👇 소셜 회원가입에 필요한 정보 전역 상태에 저장
        setAuth({
          id: -1, // 아직 모를 경우 -1 처리
          accessToken,
          refreshToken: refreshToken || null,
          email,
          userName: userName || '', // null 방지
          temporaryUser: true,
        });
        console.log('임시 회원 → 추가 정보 입력 페이지로 이동');
        router.push('/customer/socialsignup');
      } else {
        console.log('정상 회원 → 리다이렉트할 URL:', originalUrl || '/');
        router.push(originalUrl || '/');
      }
    } else {
      console.log('토큰 또는 이메일이 없음, 로그인 페이지로 이동');
      router.push('/customer/member/login?error=invalid_callback');
    }
  }, [searchParams, setTokens, setAuth, router]);

  return <div>로그인 처리 중입니다...</div>;
}
