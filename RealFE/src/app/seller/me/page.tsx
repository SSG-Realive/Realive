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
import { useGlobalDialog } from '@/app/context/dialogContext';

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
  const {show} = useGlobalDialog();
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
      payload.newPassword = password.trim();
    }
    try {
      console.log('수정 요청 데이터:', payload); // 디버깅용
      await updateProfile(payload);
      console.log('수정 요청 완료'); // 디버깅용
      
      // 최신 프로필 정보 다시 가져와서 카드 업데이트
      const updatedData = await getProfile();
      console.log('최신 데이터:', updatedData); // 디버깅용
      setEmail(updatedData.email);
      setName(updatedData.name);
      setPhone(updatedData.phone);
      
      await show('프로필이 성공적으로 수정되었습니다.');
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
          <div className="flex-1 w-full h-full px-4 py-8">
            <h1 className="text-xl md:text-2xl font-bold mb-6 text-[#374151]">마이페이지</h1>

            {/* 상단 정보 카드 */}
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
              <section className="bg-[#f3f4f6] rounded-xl shadow-xl border-2 border-[#d1d5db] flex flex-col justify-center items-center p-6 min-h-[140px] transition-all">
                <User className="w-10 h-10 text-[#6b7280]" />
                <div>
                  <h2 className="text-[#374151] text-sm font-semibold mb-1">판매자명</h2>
                  <p className="text-2xl font-extrabold text-[#374151]">{name}</p>
                </div>
              </section>
              <section className="bg-[#f3f4f6] rounded-xl shadow-xl border-2 border-[#d1d5db] flex flex-col justify-center items-center p-6 min-h-[140px] transition-all">
                <Mail className="w-10 h-10 text-[#6b7280]" />
                <div>
                  <h2 className="text-[#374151] text-sm font-semibold mb-1">이메일</h2>
                  <p className="text-2xl font-extrabold text-[#374151]">{email}</p>
                </div>
              </section>
              <section className="bg-[#f3f4f6] rounded-xl shadow-xl border-2 border-[#d1d5db] flex flex-col justify-center items-center p-6 min-h-[140px] transition-all">
                <Phone className="w-10 h-10 text-[#6b7280]" />
                <div>
                  <h2 className="text-[#374151] text-sm font-semibold mb-1">연락처</h2>
                  <p className="text-2xl font-extrabold text-[#374151]">{phone}</p>
                </div>
              </section>
            </div>

            {/* 정보 수정 폼 */}
            <div className="max-w-2xl mx-auto">
              <div className="bg-[#f3f4f6] rounded-xl shadow border border-[#d1d5db] p-8">
                <h2 className="text-lg font-bold mb-6 text-[#374151] flex items-center gap-2">
                  <Edit3 className="w-5 h-5 text-[#6b7280]" /> 정보 수정
                </h2>
                <form onSubmit={handleSubmit} className="space-y-4">
                  <div>
                    <label htmlFor="email" className="block text-sm font-medium text-[#374151] mb-2 flex items-center gap-1">
                      <Mail className="w-4 h-4 text-[#6b7280]" /> 이메일 (수정 불가)
                    </label>
                    <input 
                      id="email" 
                      type="email" 
                      value={email} 
                      disabled 
                      className="w-full px-3 py-2 border border-[#d1d5db] rounded-md bg-[#f3f4f6] text-[#374151] cursor-not-allowed"
                    />
                  </div>
                  <div>
                    <label htmlFor="name" className="block text-sm font-medium text-[#374151] mb-2 flex items-center gap-1">
                      <User className="w-4 h-4 text-[#6b7280]" /> 이름
                    </label>
                    <input 
                      id="name" 
                      type="text" 
                      value={name} 
                      onChange={(e) => setName(e.target.value)} 
                      required 
                      className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#4fd1c7] focus:border-[#4fd1c7] bg-[#f3f4f6] text-[#374151]"
                      placeholder="이름을 입력하세요"
                    />
                  </div>
                  <div>
                    <label htmlFor="phone" className="block text-sm font-medium text-[#374151] mb-2 flex items-center gap-1">
                      <Phone className="w-4 h-4 text-[#6b7280]" /> 전화번호
                    </label>
                    <input 
                      id="phone" 
                      type="tel" 
                      value={phone} 
                      onChange={(e) => setPhone(e.target.value)} 
                      required 
                      className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#4fd1c7] focus:border-[#4fd1c7] bg-[#f3f4f6] text-[#374151]"
                      placeholder="전화번호를 입력하세요"
                    />
                  </div>
                  <div>
                    <label htmlFor="password" className="block text-sm font-medium text-[#374151] mb-2 flex items-center gap-1">
                      <Key className="w-4 h-4 text-[#6b7280]" /> 새 비밀번호 (변경 시에만 입력)
                    </label>
                    <input 
                      id="password" 
                      type="password" 
                      value={password} 
                      onChange={(e) => setPassword(e.target.value)} 
                      placeholder="비밀번호를 변경하려면 입력하세요" 
                      className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#4fd1c7] focus:border-[#4fd1c7] bg-[#f3f4f6] text-[#374151]"
                    />
                  </div>
                  {error && (
                    <div className="bg-red-50 border border-red-200 rounded-md p-3">
                      <p className="text-red-600 text-sm">{error}</p>
                    </div>
                  )}
                  <button 
                    type="submit" 
                    className="w-full bg-[#d1d5db] hover:bg-[#bfc5cb] text-[#374151] py-3 px-4 rounded-lg font-medium transition-colors focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:ring-offset-2 flex items-center justify-center gap-2"
                  >
                    <Edit3 className="w-4 h-4 text-[#6b7280]" /> 정보 수정
                  </button>
                </form>
              </div>
            </div>
          </div>
        </SellerLayout>
      </>
  );
}