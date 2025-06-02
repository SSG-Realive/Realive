// src/app/seller/signup/page.tsx
'use client';

import { useState, ChangeEvent, FormEvent } from 'react';
import { useRouter } from 'next/navigation';
import { signup } from '../../../service/signupService';

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
    <div style={{ maxWidth: 500, margin: '0 auto', padding: '2rem' }}>
      <h1>판매자 회원가입</h1>
      <form onSubmit={handleSubmit} encType="multipart/form-data">
        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="email">이메일</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="password">비밀번호</label>
          <input
            id="password"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            minLength={6}
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="name">판매자 이름</label>
          <input
            id="name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            required
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="phone">전화번호</label>
          <input
            id="phone"
            type="text"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
            required
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="businessNumber">사업자등록번호</label>
          <input
            id="businessNumber"
            type="text"
            value={businessNumber}
            onChange={(e) => setBusinessNumber(e.target.value)}
            required
            style={{ width: '100%', padding: '0.5rem', marginTop: '0.25rem' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="businessLicense">사업자등록증 업로드</label>
          <input
            id="businessLicense"
            type="file"
            accept=".jpg,.jpeg,.png,.pdf"
            onChange={(e) => handleFileChange(e, setBusinessLicenseFile)}
            required
            style={{ marginTop: '0.25rem' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label htmlFor="bankAccountCopy">통장사본 업로드</label>
          <input
            id="bankAccountCopy"
            type="file"
            accept=".jpg,.jpeg,.png,.pdf"
            onChange={(e) => handleFileChange(e, setBankAccountFile)}
            required
            style={{ marginTop: '0.25rem' }}
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
          회원가입
        </button>
      </form>
    </div>
  );
};

export default SellerSignup;
