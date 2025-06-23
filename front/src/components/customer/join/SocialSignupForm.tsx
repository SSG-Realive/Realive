'use client';

import React from 'react';
import { useSocialSignupForm } from '@/hooks/useSocialSignupForm';
import GenderSelector from './GenderSelector';

import {
  Card,
  CardAction,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import Link from 'next/link';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';

interface Props {
  email: string;
  token: string;
  onSuccess: (user: { id: number; userName: string }) => void;
}

export default function SocialSignupForm({ email, token, onSuccess }: Props) {
  const {
    form: { userName, phone, address, birth, gender },
    setUserName,
    setPhone,
    setAddress,
    setBirth,
    setGender,
    loading,
    setLoading,
    isValid,
  } = useSocialSignupForm(email);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    if (!isValid()) {
      alert('모든 필드를 입력해주세요.');
      setLoading(false);
      return;
    }

    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_ROOT_URL}/api/customer/update-info`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          email,
          password: 'aaaa1111',
          name: userName,
          phone,
          address,
          birth,
          gender,
        }),
      });

      if (!res.ok) throw new Error('회원가입 실패');

      const data = await res.json();  // 여기 꼭 추가

      onSuccess({ id: data.id, userName });
    } catch (err) {
      console.error(err);
      alert('회원가입에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="max-w-md mx-auto mt-10">
      <Card>
        <CardHeader>
          <CardTitle>소셜 회원가입</CardTitle>
          <CardAction>
            <Button asChild variant="outline">
              <Link href="/customer/member/login">Login</Link>
            </Button>
          </CardAction>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col gap-6">
            <div className="grid gap-2">
              <Label htmlFor="email">이메일</Label>
              <Input
                id="email"
                name="email"
                type="email"
                value={email}
                readOnly
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="userName">닉네임</Label>
              <Input
                id="userName"
                name="userName"
                value={userName}
                onChange={(e) => setUserName(e.target.value)}
                required
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="phone">전화번호</Label>
              <Input
                id="phone"
                name="phone"
                value={phone}
                onChange={(e) => setPhone(e.target.value)}
                placeholder="010-1234-5678"
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="address">주소</Label>
              <Input
                id="address"
                name="address"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
              />
            </div>
            <div className="grid gap-2">
              <Label htmlFor="birth">생년월일</Label>
              <Input
                id="birth"
                name="birth"
                type="date"
                value={birth}
                onChange={(e) => setBirth(e.target.value)}
              />
            </div>

            <GenderSelector
              gender={gender}
              onChange={setGender}  // gender와 setGender는 useSocialSignupForm 훅에서 바로 받음
            />
          </div>
        </CardContent>
        <CardFooter>
          <Button type="submit" className="w-full mt-4" disabled={loading}>
            {loading ? '가입 중...' : '회원가입'}
          </Button>
        </CardFooter>
      </Card>
    </form>
  );

}
