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
import { User, Mail, Phone, Key, Edit3 } from 'lucide-react';

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
        setLoading(false);
      }
    };
    fetchProfile();
  }, [checking]);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
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
          <div className="flex-1 w-full h-full px-4 py-8 bg-[#a89f91]">
            <h1 className="text-xl md:text-2xl font-bold mb-6 text-[#5b4636]">마이페이지</h1>

            {/* 상단 정보 카드 */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8">
              <section className="bg-[#e9dec7] p-6 rounded-xl shadow border border-[#bfa06a] flex items-center gap-4">
                <Mail className="w-8 h-8 text-[#bfa06a]" />
                <div>
                  <h2 className="text-[#5b4636] text-sm font-semibold mb-1">이메일</h2>
                  <p className="text-base font-bold text-[#5b4636]">{email}</p>
                </div>
              </section>
              <section className="bg-[#e9dec7] p-6 rounded-xl shadow border border-[#bfa06a] flex items-center gap-4">
                <User className="w-8 h-8 text-[#bfa06a]" />
                <div>
                  <h2 className="text-[#5b4636] text-sm font-semibold mb-1">이름</h2>
                  <p className="text-base font-bold text-[#5b4636]">{name}</p>
                </div>
              </section>
              <section className="bg-[#e9dec7] p-6 rounded-xl shadow border border-[#bfa06a] flex items-center gap-4">
                <Phone className="w-8 h-8 text-[#bfa06a]" />
                <div>
                  <h2 className="text-[#5b4636] text-sm font-semibold mb-1">전화번호</h2>
                  <p className="text-base font-bold text-[#5b4636]">{phone}</p>
                </div>
              </section>
            </div>

            {/* 정보 수정 폼 */}
            <div className="max-w-2xl mx-auto">
              <div className="bg-[#e9dec7] rounded-xl shadow border border-[#bfa06a] p-8">
                <h2 className="text-lg font-bold mb-6 text-[#5b4636] flex items-center gap-2">
                  <Edit3 className="w-5 h-5 text-[#bfa06a]" /> 정보 수정
                </h2>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <label htmlFor="email" className="block text-sm font-medium text-[#5b4636] mb-2 flex items-center gap-1">
                      <Mail className="w-4 h-4 text-[#bfa06a]" /> 이메일 (수정 불가)
                    </label>
                    <input 
                      id="email" 
                      type="email" 
                      value={email} 
                      disabled 
                      className="w-full px-3 py-2 border border-[#bfa06a] rounded-md bg-[#e9dec7] text-[#bfa06a] cursor-not-allowed"
                    />
                  </div>
                  <div>
                    <label htmlFor="name" className="block text-sm font-medium text-[#5b4636] mb-2 flex items-center gap-1">
                      <User className="w-4 h-4 text-[#bfa06a]" /> 이름
                    </label>
                    <input 
                      id="name" 
                      type="text" 
                      value={name} 
                      onChange={(e) => setName(e.target.value)} 
                      required 
                      className="w-full px-3 py-2 border border-[#bfa06a] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#bfa06a] focus:border-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
                      placeholder="이름을 입력하세요"
                    />
                  </div>
                  <div>
                    <label htmlFor="phone" className="block text-sm font-medium text-[#5b4636] mb-2 flex items-center gap-1">
                      <Phone className="w-4 h-4 text-[#bfa06a]" /> 전화번호
                    </label>
                    <input 
                      id="phone" 
                      type="tel" 
                      value={phone} 
                      onChange={(e) => setPhone(e.target.value)} 
                      required 
                      className="w-full px-3 py-2 border border-[#bfa06a] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#bfa06a] focus:border-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
                      placeholder="전화번호를 입력하세요"
                    />
                  </div>
                  <div>
                    <label htmlFor="password" className="block text-sm font-medium text-[#5b4636] mb-2 flex items-center gap-1">
                      <Key className="w-4 h-4 text-[#bfa06a]" /> 새 비밀번호 (변경 시에만 입력)
                    </label>
                    <input 
                      id="password" 
                      type="password" 
                      value={password} 
                      onChange={(e) => setPassword(e.target.value)} 
                      placeholder="비밀번호를 변경하려면 입력하세요" 
                      className="w-full px-3 py-2 border border-[#bfa06a] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#bfa06a] focus:border-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
                    />
                  </div>
                  {error && (
                    <div className="bg-red-50 border border-red-200 rounded-md p-3">
                      <p className="text-red-600 text-sm">{error}</p>
                    </div>
                  )}
                  <button 
                    type="submit" 
                    className="w-full bg-[#bfa06a] hover:bg-[#5b4636] text-[#4b3a2f] py-3 px-4 rounded-lg font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-[#bfa06a] focus:ring-offset-2 flex items-center justify-center gap-2"
                  >
                    <Edit3 className="w-4 h-4" /> 정보 수정
                  </button>
                </form>
              </div>
            </div>
          </div>
        </SellerLayout>
      </>
  );
}