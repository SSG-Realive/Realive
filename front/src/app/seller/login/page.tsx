// src/app/seller/login/page.tsx

'use client';

import { useState, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { login } from '@/service/seller/sellerService'; // 👈 위에서 확인한 서비스 함수
import { LoginResponse } from '@/types/seller/login/loginResponse';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore'; // 👈 [2단계]에서 만든 스토어

export default function SellerLoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  // ✅ 스토어에서 `setToken` 액션만 가져옵니다.
  // 이렇게 하면 token 상태가 바뀌어도 이 컴포넌트는 리렌더링되지 않아 효율적입니다.
  const setTokens = useSellerAuthStore((s) => s.setTokens);

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
    // ... JSX ...
    // 로그인 폼 UI는 기존과 동일하게 사용하시면 됩니다.
    // 예시:
    <div style={{ maxWidth: 400, margin: '0 auto', padding: '2rem' }}>
      <h1>판매자 로그인</h1>
      <form onSubmit={handleSubmit}>
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="email">이메일</label>
          <input id="email" type="email" value={email} onChange={(e) => setEmail(e.target.value)} required style={{ width: '100%' }} />
        </div>
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="password">비밀번호</label>
          <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} required style={{ width: '100%' }} />
        </div>
        {error && <p style={{ color: 'red' }}>{error}</p>}
        <button type="submit" style={{ width: '100%' }}>로그인</button>
      </form>
    </div>
  );
}