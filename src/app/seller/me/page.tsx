'use client';

import { useState, useEffect, ChangeEvent, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import SellerHeader from '@/components/seller/SellerHeader';
import {
  getProfile,
  updateProfile,
  SellerProfile,
  SellerUpdateRequest,
} from '@/service/seller/sellerService';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function SellerMePage() {
  const checking = useSellerAuthGuard();
  const router = useRouter();

  const [email, setEmail] = useState<string>('');
  const [name, setName] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState<boolean>(true); // 로딩 상태는 true로 시작

  // ▼▼▼ 이 useEffect를 추가합니다 ▼▼▼
  useEffect(() => {
    if (checking) {
      return; // 인증 확인 중이면 대기
    }
    const fetchProfile = async () => {
      try {
        const data = await getProfile();
        setEmail(data.email);
        setName(data.name);
        setPhone(data.phone);
      } catch (err: any) {
        console.error('프로필 정보 가져오기 실패', err);
        setError('프로필 정보를 불러오는데 실패했습니다.');
      } finally {
        // ✅ API 호출이 끝나면 로딩 상태를 false로 변경
        setLoading(false);
      }
    };
    fetchProfile();
  }, [checking]);
  // ▲▲▲ 여기까지 추가 ▲▲▲

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    // ... (프로필 수정 로직은 기존과 동일)
    e.preventDefault();
    setError(null);

    if (!name.trim() || !phone.trim()) {
      setError('이름과 전화번호는 필수 입력 사항입니다.');
      return;
    }
    const payload: SellerUpdateRequest = { name: name.trim(), phone: phone.trim() };
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

  // ✅ 로딩 조건문 순서를 dashboard 페이지와 동일하게 맞춥니다.
  if (checking || loading) {
    return (
        <>
          <SellerHeader />
          <SellerLayout>
            <div className="p-8">로딩 중...</div>
          </SellerLayout>
        </>
    );
  }

  return (
      <>
        <SellerHeader />
        <SellerLayout>
          <div style={{ maxWidth: 500, margin: '0 auto', padding: '2rem' }}>
            <h1>판매자 정보 수정</h1>
            <form onSubmit={handleSubmit}>
              {/* ... (나머지 JSX는 기존과 동일) ... */}
              <div style={{ marginBottom: '1rem' }}>
                <label htmlFor="email">이메일 (수정 불가)</label>
                <input id="email" type="email" value={email} disabled style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem', backgroundColor: '#f5f5f5' }}/>
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label htmlFor="name">이름</label>
                <input id="name" type="text" value={name} onChange={(e) => setName(e.target.value)} required style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}/>
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label htmlFor="phone">전화번호</label>
                <input id="phone" type="tel" value={phone} onChange={(e) => setPhone(e.target.value)} required style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}/>
              </div>
              <div style={{ marginBottom: '1rem' }}>
                <label htmlFor="password">새 비밀번호 (변경 시에만 입력)</label>
                <input id="password" type="password" value={password} onChange={(e) => setPassword(e.target.value)} placeholder="비밀번호를 변경하려면 입력하세요" style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}/>
              </div>
              {error && (<p style={{ color: 'red', marginBottom: '1rem' }}>{error}</p>)}
              <button type="submit" style={{ width: '100%', padding: '0.75rem', backgroundColor: '#333', color: '#fff', border: 'none', cursor: 'pointer' }}>수정하기</button>
            </form>
          </div>
        </SellerLayout>
      </>
  );
}