// src/app/components/Header.tsx
'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { getProfile, logout } from '@/service/sellerService';
import { useEffect, useState } from 'react';

export default function Header() {
  const router = useRouter();
  const [name, setName] = useState<string>('');

  useEffect(() =>{
    const fetchName = async () => {
      try {
        const profile = await getProfile();
        setName(profile.name);

      } catch (err){
        console.error('프로필 정보 가져오기 실패', err);
      }
      }
      fetchName();
  }, []);

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
        Realive 
      </Link>


      <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
        {name && <span style={{ fontSize: '1rem', color: '#333' }}>{name}님</span>}
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
      </div>
    </header>
  );
}
