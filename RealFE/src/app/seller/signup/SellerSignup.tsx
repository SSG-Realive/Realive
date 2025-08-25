// src/app/seller/signup/page.tsx
'use client';

import { useState, ChangeEvent, FormEvent, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { signup } from '../../../service/seller/signupService';

const SellerSignup: React.FC = () => {
  const [email, setEmail] = useState<string>('');
  const [password, setPassword] = useState<string>('');
  const [name, setName] = useState<string>('');
  const [phone, setPhone] = useState<string>('');
  const [businessNumber, setBusinessNumber] = useState<string>('');
  const [businessLicenseFile, setBusinessLicenseFile] = useState<File | null>(null);
  const [bankAccountFile, setBankAccountFile] = useState<File | null>(null);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    document.body.classList.add('seller-signup');
    return () => {
      document.body.classList.remove('seller-signup');
    };
  }, []);

  const handleFileChange = (
    e: ChangeEvent<HTMLInputElement>,
    setter: React.Dispatch<React.SetStateAction<File | null>>
  ) => {
    if (e.target.files && e.target.files.length > 0) {
      setter(e.target.files[0]);
    } else {
      setter(null);
    }
  };

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setError(null);

    if (!businessLicenseFile || !bankAccountFile) {
      setError('사업자등록증 및 통장사본 파일을 모두 업로드해주세요.');
      return;
    }

    try {
      const signupDto = {
        email,
        password,
        name,
        phone,
        businessNumber,
      };

      const formData = new FormData();
      // DTO를 JSON 문자열로 묶어서 "dto" 파트로 전송
      formData.append(
        'dto',
        new Blob([JSON.stringify(signupDto)], { type: 'application/json' })
      );
      // 파일 파트 이름은 controller에서 @RequestPart MultipartFile businessLicense, bankAccountCopy 로 받으므로 그대로 사용
      formData.append('businessLicense', businessLicenseFile);
      formData.append('bankAccountCopy', bankAccountFile);

      // signupService의 signup 메소드를 사용
      await signup(formData);

      // 가입 성공 시 로그인 페이지로 이동
      router.push('/seller/login');
    } catch (e: any) {
      console.error('회원가입 실패:', e);
      if (e.response && e.response.data && e.response.data.message) {
        setError(e.response.data.message);
      } else {
        setError('회원가입 중 오류가 발생했습니다.');
      }
    }
  };

  return (
    <div className="min-h-screen w-full flex items-center justify-center bg-[#f3f4f6] px-4 py-8">
      <div className="w-full max-w-md md:max-w-lg lg:max-w-xl mx-auto">
        <div className="bg-white rounded-lg shadow-md p-6 md:p-8 border-2 border-[#d1d5db]">
          <h1 className="text-2xl font-bold text-center mb-6 text-[#374151]">판매자 회원가입</h1>
          <form onSubmit={handleSubmit} encType="multipart/form-data" className="space-y-4">
            <div>
              <label htmlFor="email" className="block text-sm font-medium text-[#374151] mb-1">
                이메일
              </label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full px-3 py-2 border border-[#d1d5db] rounded-md focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:border-[#6b7280] bg-white text-[#374151] placeholder-[#6b7280]"
                placeholder="이메일을 입력하세요"
              />
            </div>
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-[#374151] mb-1">
                비밀번호
              </label>
              <input
                id="password"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                minLength={6}
                className="w-full px-3 py-2 border border-[#d1d5db] rounded-md focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:border-[#6b7280] bg-white text-[#374151] placeholder-[#6b7280]"
                placeholder="비밀번호를 입력하세요 (최소 6자)"
              />
            </div>
            <div>
              <label htmlFor="name" className="block text-sm font-medium text-[#374151] mb-1">
                판매자 이름
              </label>
              <input
                id="name"
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                className="w-full px-3 py-2 border border-[#d1d5db] rounded-md focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:border-[#6b7280] bg-white text-[#374151] placeholder-[#6b7280]"
                placeholder="판매자 이름을 입력하세요"
              />
            </div>
            <div>
              <label htmlFor="phone" className="block text-sm font-medium text-[#374151] mb-1">
                전화번호
              </label>
              <input
                id="phone"
                type="text"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                required
                className="w-full px-3 py-2 border border-[#d1d5db] rounded-md focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:border-[#6b7280] bg-white text-[#374151] placeholder-[#6b7280]"
                placeholder="전화번호를 입력하세요"
              />
            </div>
            <div>
              <label htmlFor="businessNumber" className="block text-sm font-medium text-[#374151] mb-1">
                사업자등록번호
              </label>
              <input
                id="businessNumber"
                type="text"
                value={businessNumber}
                onChange={(e) => setBusinessNumber(e.target.value)}
                required
                className="w-full px-3 py-2 border border-[#d1d5db] rounded-md focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:border-[#6b7280] bg-white text-[#374151] placeholder-[#6b7280]"
                placeholder="사업자등록번호를 입력하세요"
              />
            </div>
            <div>
              <label htmlFor="businessLicense" className="block text-sm font-medium text-[#374151] mb-1">
                사업자등록증 업로드
              </label>
              <input
                id="businessLicense"
                type="file"
                accept=".jpg,.jpeg,.png,.pdf"
                onChange={(e) => handleFileChange(e, setBusinessLicenseFile)}
                required
                className="w-full px-3 py-2 border border-[#d1d5db] rounded-md focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:border-[#6b7280] bg-white text-[#374151] file:mr-4 file:py-1 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-[#6b7280] file:text-white hover:file:bg-[#374151]"
              />
            </div>
            <div>
              <label htmlFor="bankAccountCopy" className="block text-sm font-medium text-[#374151] mb-1">
                통장사본 업로드
              </label>
              <input
                id="bankAccountCopy"
                type="file"
                accept=".jpg,.jpeg,.png,.pdf"
                onChange={(e) => handleFileChange(e, setBankAccountFile)}
                required
                className="w-full px-3 py-2 border border-[#d1d5db] rounded-md focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:border-[#6b7280] bg-white text-[#374151] file:mr-4 file:py-1 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-[#6b7280] file:text-white hover:file:bg-[#374151]"
              />
            </div>
            {error && (
              <p className="text-[#dc2626] text-sm bg-[#fef2f2] p-3 rounded-md border-2 border-[#fecaca]">
                {error}
              </p>
            )}
            <button
              type="submit"
              className="w-full bg-[#6b7280] text-white py-3 px-4 rounded-md font-medium hover:bg-[#374151] focus:outline-none focus:ring-2 focus:ring-[#6b7280] focus:ring-offset-2 transition-colors"
            >
              회원가입
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default SellerSignup;