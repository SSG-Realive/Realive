'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';

import GenderSelector from './GenderSelector';

import {
  Card,
  CardAction,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import Link from 'next/link';

import { GenderWithUnselected, MemberJoinDTO } from '@/types/customer/signup';

export default function RegisterForm() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const redirectTo = searchParams.get('redirectTo') || '/';

  const setAuth = useAuthStore(state => state.setAuth);

  const [formData, setFormData] = useState<{
    email: string;
    password: string;
    name: string;
    phone: string;
    address: string;
    birth: string;
    gender: GenderWithUnselected;
  }>({
    email: '',
    password: '',
    name: '',
    phone: '',
    address: '',
    birth: '',
    gender: 'UNSELECTED',
  });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.gender === 'UNSELECTED') {
      alert('성별을 선택해주세요.');
      return;
    }

    try {
      const payload: MemberJoinDTO = {
        ...formData,
        gender: formData.gender as 'M' | 'F',
      };

      const res = await fetch(`${process.env.NEXT_PUBLIC_API_ROOT_URL}/api/public/auth/join`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      const data = await res.json();

      if (!res.ok || !data.success) {
        alert(data.message || '회원가입 실패');
        return;
      }

      alert('회원가입 성공!');

      if (data.accessToken && data.email && data.name) {
        setAuth({
          id: data.id,
          accessToken: data.accessToken,        // 'token' → 'accessToken' 변경
          refreshToken: null,                    // 새 필드, 없으면 null로 처리
          email: data.email,
          userName: data.name,
          temporaryUser: false,
        });
    }
      router.push(redirectTo);
    } catch (err) {
      console.error('회원가입 오류:', err);
      alert('회원가입 처리 중 오류가 발생했습니다.');
    }
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-md mx-auto mt-10">
      <Card>
        <CardHeader>
          <CardTitle>신규 회원가입</CardTitle>
          <CardAction>
            <Button asChild variant="outline">
              <Link href="/customer/member/login">Login</Link>
            </Button>
          </CardAction>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col gap-6">
            {/* 이메일 */}
            <div className="grid gap-2">
              <Label htmlFor="email">이메일</Label>
              <Input id="email" name="email" type="email" value={formData.email} onChange={handleChange} required />
            </div>
            {/* 비밀번호 */}
            <div className="grid gap-2">
              <Label htmlFor="password">비밀번호</Label>
              <Input id="password" name="password" type="password" value={formData.password} onChange={handleChange} required />
            </div>
            {/* 이름 */}
            <div className="grid gap-2">
              <Label htmlFor="name">이름</Label>
              <Input id="name" name="name" value={formData.name} onChange={handleChange} required />
            </div>
            {/* 전화번호 */}
            <div className="grid gap-2">
              <Label htmlFor="phone">전화번호</Label>
              <Input id="phone" name="phone" value={formData.phone || ""} onChange={handleChange} />
            </div>
            {/* 주소 */}
            <div className="grid gap-2">
              <Label htmlFor="address">주소</Label>
              <Input id="address" name="address" value={formData.address || ""} onChange={handleChange} />
            </div>
            {/* 생년월일 */}
            <div className="grid gap-2">
              <Label htmlFor="birth">생년월일</Label>
              <Input id="birth" name="birth" type="date" value={formData.birth || ""} onChange={handleChange} />
            </div>

            <GenderSelector gender={formData.gender} onChange={(value) => setFormData(prev => ({ ...prev, gender: value }))} />
          </div>
        </CardContent>
        <CardFooter>
          <Button type="submit" className="w-full mt-4">회원가입</Button>
        </CardFooter>
      </Card>
    </form>
  );
}
