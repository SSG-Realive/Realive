'use client';

import { useState } from 'react';

import InputField from './InputField';
import GenderSelector from './GenderSelector';
import { MemberJoinDTO } from '@/types/customer/signup/signup';

export default function RegisterForm() {
  const [formData, setFormData] = useState<MemberJoinDTO>({
    email: '',
    password: '',
    name: '',
    phone: '',
    address: '',
    birth: '',
    gender: 'MALE'
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    try {
      const res = await fetch('/api/customer/signup', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });

      const data = await res.json();
      if (res.ok) {
        alert('회원가입 성공!');
        // 이동 or 로그인 처리
      } else {
        alert(data.message || '회원가입 실패');
      }
    } catch (err) {
      console.error(err);
      alert('에러 발생');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-md mx-auto mt-10">
      <InputField label="이메일" name="email" value={formData.email} onChange={handleChange} />
      <InputField label="비밀번호" name="password" type="password" value={formData.password} onChange={handleChange} />
      <InputField label="이름" name="name" value={formData.name} onChange={handleChange} />
      <InputField label="전화번호" name="phone" value={formData.phone || ''} onChange={handleChange} />
      <InputField label="주소" name="address" value={formData.address || ''} onChange={handleChange} />
      <InputField label="생년월일" name="birth" type="date" value={formData.birth || ''} onChange={handleChange} />
      <GenderSelector gender={formData.gender} onChange={handleChange} />
      
      <button type="submit" className="w-full bg-blue-500 text-white py-2 rounded mt-4">회원가입</button>
    </form>
  );
}
