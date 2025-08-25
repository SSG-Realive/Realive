// src/app/seller/login/page.tsx

'use client';

import { useState, FormEvent, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { login } from '@/service/seller/sellerService'; // 👈 위에서 확인한 서비스 함수
import { LoginResponse } from '@/types/seller/login/loginResponse';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore'; // 👈 [2단계]에서 만든 스토어
import Link from 'next/link';

export default function SellerLoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  // ✅ 스토어에서 `setToken` 액션만 가져옵니다.
  // 이렇게 하면 token 상태가 바뀌어도 이 컴포넌트는 리렌더링되지 않아 효율적입니다.
  const setTokens = useSellerAuthStore((s) => s.setTokens);

  useEffect(() => {
    document.body.classList.add('seller-login');
    return () => {
      document.body.classList.remove('seller-login');
    };
  }, []);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);
     

     try {
      const res: LoginResponse = await login(email, password);

      if (res.accessToken && res.refreshToken) {
        setTokens(res.accessToken, res.refreshToken);  // 여기만 변경
        router.push('/seller/dashboard');
      } else {
        setError('로그인 응답이 올바르지 않습니다.');
      }
    } catch (err: any) {
      console.error('로그인 오류:', err);
      
      // 더 구체적인 오류 메시지 처리
      if (err.response) {
        const status = err.response.status;
        const message = err.response.data?.message;
        
        if (status === 401 || status === 500) {
          // 인증 실패 시 통일된 메시지 (보안상 구체적인 오류 정보 노출 방지)
          setError('아이디 또는 비밀번호를 확인해주세요.');
        } else if (status === 400) {
          setError(message || '입력 정보를 확인해주세요.');
        } else {
          setError(message || '로그인 중 오류가 발생했습니다.');
        }
      } else if (err.request) {
        setError('서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요.');
      } else {
        setError('로그인 중 오류가 발생했습니다.');
      }
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-[#f3f4f6] px-4">
      <div className="w-full max-w-md md:max-w-lg lg:max-w-xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-6 md:p-8 border-2 border-[#d1d5db]">
          <h1 className="text-2xl font-bold text-center mb-6 text-[#374151]">판매자 로그인</h1>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-[#374151] mb-2">
                이메일
              </label>
              <input 
                type="email"
                id="email" 
                name="email"
                value={email} 
                onChange={(e) => setEmail(e.target.value)} 
                required 
                className="w-full px-3 py-2 border-2 border-[#d1d5db] rounded-md focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:border-[#6b7280] bg-white text-[#374151] placeholder-[#6b7280]"
                placeholder="이메일을 입력하세요"
              />
            </div>
            
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-[#374151] mb-2">
                비밀번호
              </label>
              <input 
                type="password"
                id="password" 
                name="password"
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                required 
                className="w-full px-3 py-2 border-2 border-[#d1d5db] rounded-md focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:border-[#6b7280] bg-white text-[#374151] placeholder-[#6b7280]"
                placeholder="비밀번호를 입력하세요"
              />
            </div>
            
            {error && (
              <p className="text-[#dc2626] text-sm bg-[#fef2f2] p-3 rounded-md border-2 border-[#fecaca]">
                {error}
              </p>
            )}
            
            <button 
              type="submit" 
              className="w-full bg-[#6b7280] text-white py-2 px-4 rounded-md hover:bg-[#374151] focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:ring-offset-2 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              로그인
            </button>
          </form>
          
          <div className="mt-6 text-center">
            <p className="text-sm text-[#6b7280]">
              아직 계정이 없으신가요?{' '}
              <Link href="/seller/signup" className="text-[#374151] hover:text-[#6b7280] font-medium">
                판매자 회원가입
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}