// src/app/components/Header.tsx
'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { logout } from '@/service/sellerService';

export default function Header() {
  const router = useRouter();

  const handleLogout = async () => {
    try {
      // 1) 서비스의 logout() 호출 → refreshToken 쿠키 삭제
      await logout();

      // 2) 로컬 스토리지의 accessToken 삭제
      localStorage.removeItem('accessToken');

      // 3) 로그인 페이지로 리디렉트
      router.push('/seller/login');
    } catch (err) {
      console.error('로그아웃 실패', err);
      // 예외 상황에도 로컬 토큰은 삭제하고 로그인 페이지로 이동
      localStorage.removeItem('accessToken');
      router.push('/seller/login');
    }
  };

  return (
    <header
      style={{
        display: 'flex',
        justifyContent: 'space-between',
        alignItems: 'center',
        padding: '1rem 2rem',
        borderBottom: '1px solid #ddd',
        marginBottom: '2rem',
      }}
    >
      <Link
        href="/seller/dashboard"
        style={{ fontSize: '1.25rem', fontWeight: 'bold', textDecoration: 'none', color: '#333' }}
      >
        Realive Seller
      </Link>
      <nav>
        <button
          onClick={handleLogout}
          style={{
            background: 'none',
            border: 'none',
            color: '#333',
            fontSize: '1rem',
            cursor: 'pointer',
          }}
        >
          로그아웃
        </button>
      </nav>
    </header>
  );
}
