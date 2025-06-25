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
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

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
          <div className="hidden">
            <SellerHeader toggleSidebar={toggleSidebar} />
          </div>
          <SellerLayout>
            <div className="p-4 sm:p-8">로딩 중...</div>
          </SellerLayout>
        </>
    );
  }

  return (
      <>
        <div className="hidden">
          <SellerHeader toggleSidebar={toggleSidebar} />
        </div>
        <SellerLayout>
          <div className="max-w-2xl mx-auto p-4 sm:p-6">
            <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
              <h1 className="text-xl sm:text-2xl font-bold mb-6 text-gray-900">판매자 정보 수정</h1>
              
              <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                    이메일 (수정 불가)
                  </label>
                  <input 
                    id="email" 
                    type="email" 
                    value={email} 
                    disabled 
                    className="w-full px-3 py-2 border border-gray-300 rounded-md bg-gray-50 text-gray-500 cursor-not-allowed"
                  />
                </div>
                
                <div>
                  <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                    이름
                  </label>
                  <input 
                    id="name" 
                    type="text" 
                    value={name} 
                    onChange={(e) => setName(e.target.value)} 
                    required 
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="이름을 입력하세요"
                  />
                </div>
                
                <div>
                  <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
                    전화번호
                  </label>
                  <input 
                    id="phone" 
                    type="tel" 
                    value={phone} 
                    onChange={(e) => setPhone(e.target.value)} 
                    required 
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                    placeholder="전화번호를 입력하세요"
                  />
                </div>
                
                <div>
                  <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                    새 비밀번호 (변경 시에만 입력)
                  </label>
                  <input 
                    id="password" 
                    type="password" 
                    value={password} 
                    onChange={(e) => setPassword(e.target.value)} 
                    placeholder="비밀번호를 변경하려면 입력하세요" 
                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                  />
                </div>
                
                {error && (
                  <div className="bg-red-50 border border-red-200 rounded-md p-3">
                    <p className="text-red-600 text-sm">{error}</p>
                  </div>
                )}
                
                <button 
                  type="submit" 
                  className="w-full bg-gray-800 hover:bg-gray-900 text-white py-3 px-4 rounded-lg font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
                >
                  수정하기
                </button>
              </form>
            </div>
          </div>
        </SellerLayout>
      </>
  );
}