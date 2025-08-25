'use client';

import { useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';

import { useAuthStore } from '@/store/customer/authStore';
import GenderSelector from './GenderSelector';
import AddressInput from '@/components/customer/join/AddressInput';

import {
  Card, CardHeader, CardTitle, CardAction,
  CardContent, CardFooter,
} from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Button } from '@/components/ui/button';

import type { GenderWithUnselected, MemberJoinDTO } from '@/types/customer/signup';

interface Props {
  /** 모달 등에서 닫기용 콜백 – 페이지 단독 사용 시 생략 가능 */
  onSuccess?: () => void;
}

export default function RegisterForm({ onSuccess }: Props) {
  /* ---------------- 기본 라우팅 ---------------- */
  const router      = useRouter();
  const redirectTo  = useSearchParams().get('redirectTo') || '/';

  /* ---------------- 전역 스토어 ---------------- */
  const setAuth = useAuthStore(s => s.setAuth);

  /* ---------------- 로컬 상태 ---------------- */
  const [formData, setFormData] = useState({
    email: '',
    password: '',
    name: '',
    phone: '',
    address: '',
    birth: '',
    gender: 'UNSELECTED' as GenderWithUnselected,
    verificationCode: '',
  });

  const [sending, setSending] = useState(false); // 인증 코드 발송
  const [saving , setSaving ] = useState(false); // 회원가입

  const handleChange = (
    e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>,
  ) => {
    const { name, value } = e.target;
    setFormData(p => ({ ...p, [name]: value }));
  };

  /* ------------ 이메일 인증 코드 발송 ------------ */
  const handleSendCode = async () => {
    if (!formData.email) return alert('이메일을 입력해주세요.');
    try {
      setSending(true);
      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_ROOT_URL}/api/public/auth/send-verification`,
        {
          method : 'POST',
          headers: { 'Content-Type': 'application/json' },
          body   : JSON.stringify({ email: formData.email.trim() }),
        },
      );

      const data = await res.json().catch(() => ({}));

      if (!res.ok || data.success === false) {
        alert(data.message || '코드 발송 실패');
        return;
      }
      alert('인증 코드가 이메일로 발송되었습니다.\n5분 안에 입력해주세요.');
    } catch (err) {
      console.error('코드 발송 오류:', err);
      alert('코드 발송 중 오류가 발생했습니다.');
    } finally {
      setSending(false);
    }
  };

  /* ---------------- 회원가입 ---------------- */
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (formData.gender === 'UNSELECTED') {
      alert('성별을 선택해주세요.');
      return;
    }

    try {
      setSaving(true);

      const payload: MemberJoinDTO = {
        ...formData,
        gender: formData.gender as 'M' | 'F',
      };

      const res = await fetch(
        `${process.env.NEXT_PUBLIC_API_ROOT_URL}/api/public/auth/signup`,
        {
          method : 'POST',
          headers: { 'Content-Type': 'application/json' },
          body   : JSON.stringify(payload),
        },
      );

      const data = await res.json().catch(() => ({}));

      if (!res.ok || data.success === false) {
        alert(data.message || '회원가입 실패');
        return;
      }

      alert('회원가입 성공!');

      // 백엔드가 토큰을 내려줄 경우 자동 로그인
      if (data.accessToken && data.email && data.name) {
        setAuth({
          id           : data.id ?? 0,
          accessToken  : data.accessToken,
          refreshToken : null,
          email        : data.email,
          userName     : data.name,
          temporaryUser: false,
        });
      }

      // 외부에서 콜백을 넘겼으면 우선 실행
      if (onSuccess) {
        onSuccess();
      } else {
        router.replace(redirectTo);
      }
    } catch (err) {
      console.error('회원가입 오류:', err);
      alert('회원가입 처리 중 오류가 발생했습니다.');
    } finally {
      setSaving(false);
    }
  };

  /* ---------------- UI ---------------- */
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
            {/* 이메일 + 코드 발송 */}
            <div className="grid gap-2">
              <Label htmlFor="email">이메일</Label>
              <div className="flex gap-2">
                <Input
                  className="flex-1"
                  id="email"
                  name="email"
                  type="email"
                  value={formData.email}
                  onChange={handleChange}
                  required
                />
                <Button
                  type="button"
                  variant="outline"
                  disabled={!formData.email || sending}
                  onClick={handleSendCode}
                >
                  {sending ? '발송 중…' : '코드 발송'}
                </Button>
              </div>
            </div>

            {/* 인증 코드 */}
            <div className="grid gap-2">
              <Label htmlFor="verificationCode">인증 코드</Label>
              <Input
                id="verificationCode"
                name="verificationCode"
                placeholder="6자리"
                maxLength={6}
                value={formData.verificationCode}
                onChange={handleChange}
                required
              />
            </div>

            {/* 비밀번호 */}
            <div className="grid gap-2">
              <Label htmlFor="password">비밀번호</Label>
              <Input
                id="password"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>

            {/* 이름 */}
            <div className="grid gap-2">
              <Label htmlFor="name">이름</Label>
              <Input
                id="name"
                name="name"
                value={formData.name}
                onChange={handleChange}
                required
              />
            </div>

            {/* 전화번호 */}
            <div className="grid gap-2">
              <Label htmlFor="phone">전화번호</Label>
              <Input
                id="phone"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
              />
            </div>

            {/* 주소 – AddressInput 컴포넌트 */}
            <div className="grid gap-2">
              <Label htmlFor="address">주소</Label>
              <AddressInput
                onAddressChange={(full) =>
                  setFormData(p => ({ ...p, address: full }))
                }
              />
            </div>

            {/* 생년월일 */}
            <div className="grid gap-2">
              <Label htmlFor="birth">생년월일</Label>
              <Input
                id="birth"
                name="birth"
                type="date"
                value={formData.birth}
                onChange={handleChange}
              />
            </div>

            {/* 성별 선택 */}
            <GenderSelector
              gender={formData.gender}
              onChange={g => setFormData(p => ({ ...p, gender: g }))}
            />
          </div>
        </CardContent>

        <CardFooter>
          <Button type="submit" className="w-full mt-4" disabled={saving}>
            {saving ? '가입 중…' : '회원가입'}
          </Button>
        </CardFooter>
      </Card>
    </form>
  );
}
