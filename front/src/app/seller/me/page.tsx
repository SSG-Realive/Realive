// src/app/seller/me/page.tsx 또는 src/app/seller/dashboard/page.tsx 등에 적용

'use client';

import { useState, useEffect, ChangeEvent, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import Header from '@/components/Header';      // ↙ Header 컴포넌트를 import
import {
  getProfile,
  updateProfile,
  SellerProfile,
  SellerUpdateRequest,
} from '@/service/sellerService';
import SellerLayout from '@/components/layouts/SellerLayout';

export default function SellerMePage() {
  const router = useRouter();

  const [email, setEmail] = useState<string>('');
  const [	name, setName] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [password, setPassword] = useState<string>(''); // 빈 값이면 비밀번호 변경 안 함
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true);

  useEffect(() => {
    const fetchProfile = async () => {
      const token = localStorage.getItem('accessToken');

      if (!token)  return;
      
      try {
        const data: SellerProfile = await getProfile();
        setEmail(data.email);
        setName(data.name);
        setPhone(data.phone);
      } catch (e) {
        console.error('프로필 조회 실패', e);
        router.push('/seller/login');
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [router]);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    if (!name.trim() || !phone.trim()) {
      setError('이름과 전화번호는 필수 입력 사항입니다.');
      return;
    }

    const payload: SellerUpdateRequest = {
      name: name.trim(),
      phone: phone.trim(),
    };
    if (password.trim().length > 0) {
      payload.password = password.trim();
    }

    try {
      await updateProfile(payload);
      alert('프로필이 성공적으로 수정되었습니다.');
      router.push('/seller/dashboard');
    } catch (err: any) {
      console.error('프로필 수정 실패', err);
      if (err.response?.status === 401) {
        router.push('/seller/login');
        return;
      }
      if (err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('프로필 수정 중 오류가 발생했습니다.');
      }
    }
  };

  if (loading) {
    return <div>로딩 중...</div>;
  }

  return (
    <>
      {/* ↙ 여기서 Header를 먼저 렌더링 */}
      <Header />
      <SellerLayout>
      {/* 기존 로그인 폼처럼 생긴 부분을 그대로 둔 예시 */}
      <div style={{ maxWidth: 500, margin: '0 auto', padding: '2rem' }}>
        <h1>판매자 정보 수정</h1>
        <form onSubmit={handleSubmit}>
          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="email">이메일 (수정 불가)</label>
            <input
              id="email"
              type="email"
              value={email}
              disabled
              style={{
                width: '100%',
                padding: '0.5rem',
                marginTop: '0.25rem',
                backgroundColor: '#f5f5f5',
              }}
            />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="name">이름</label>
            <input
              id="name"
              type="text"
              value={name}
              onChange={(e: ChangeEvent<HTMLInputElement>) => setName(e.target.value)}
              required
              style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
            />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="phone">전화번호</label>
            <input
              id="phone"
              type="tel"
              value={phone}
              onChange={(e: ChangeEvent<HTMLInputElement>) => setPhone(e.target.value)}
              required
              style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
            />
          </div>

          <div style={{ marginBottom: '1rem' }}>
            <label htmlFor="password">새 비밀번호 (변경 시에만 입력)</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(e: ChangeEvent<HTMLInputElement>) => setPassword(e.target.value)}
              placeholder="비밀번호를 변경하려면 입력하세요"
              style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
            />
          </div>

          {error && (
            <p style={{ color: 'red', marginBottom: '1rem' }}>{error}</p>
          )}

          <button
            type="submit"
            style={{
              width: '100%',
              padding: '0.75rem',
              backgroundColor: '#333',
              color: '#fff',
              border: 'none',
              cursor: 'pointer',
            }}
          >
            수정하기
          </button>
        </form>
      </div>
      </SellerLayout>
    </>
  );
}
