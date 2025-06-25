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
      setError(err.response?.data?.message ?? '로그인 중 오류가 발생했습니다.');
    }
  };

  return (
    <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-[#a89f91] flex items-center justify-center px-4">
      <div className="w-full max-w-md mx-auto">
        <div className="bg-[#e9dec7] rounded-lg shadow-md p-6 md:p-8 border border-[#bfa06a]">
          <h1 className="text-2xl font-bold text-center mb-6 text-[#5b4636]">판매자 로그인</h1>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-[#5b4636] mb-1">
                이메일
              </label>
              <input 
                id="email" 
                type="email" 
                value={email} 
                onChange={(e) => setEmail(e.target.value)} 
                required 
                className="w-full px-3 py-2 border border-[#bfa06a] rounded-md focus:outline-none focus:ring-2 focus:ring-[#bfa06a] bg-[#e9dec7] text-[#5b4636] placeholder-[#bfa06a]"
                placeholder="이메일을 입력하세요"
              />
            </div>
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-[#5b4636] mb-1">
                비밀번호
              </label>
              <input 
                id="password" 
                type="password" 
                value={password} 
                onChange={(e) => setPassword(e.target.value)} 
                required 
                className="w-full px-3 py-2 border border-[#bfa06a] rounded-md focus:outline-none focus:ring-2 focus:ring-[#bfa06a] bg-[#e9dec7] text-[#5b4636] placeholder-[#bfa06a]"
                placeholder="비밀번호를 입력하세요"
              />
            </div>
            {error && (
              <p className="text-[#b94a48] text-sm bg-[#fbeee0] p-3 rounded-md border border-[#bfa06a]">
                {error}
              </p>
            )}
            <button 
              type="submit" 
              className="w-full bg-[#bfa06a] text-[#4b3a2f] py-3 px-4 rounded-md font-medium hover:bg-[#5b4636] hover:text-[#e9dec7] focus:outline-none focus:ring-2 focus:ring-[#bfa06a] focus:ring-offset-2 transition-colors"
            >
              로그인
            </button>
            <Link
              href="/seller/signup"
              className="block w-full mt-4 py-3 px-4 rounded-md border-2 border-[#bfa06a] bg-[#e9dec7] text-[#5b4636] font-medium text-center hover:bg-[#bfa06a] hover:text-[#e9dec7] focus:outline-none focus:ring-2 focus:ring-[#bfa06a] focus:ring-offset-2 transition-colors"
            >
              회원가입
            </Link>
          </form>
        </div>
      </div>
    </div>
  );
}