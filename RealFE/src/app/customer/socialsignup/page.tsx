'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import SocialSignupForm from '@/components/customer/join/SocialSignupForm';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function SocialSignupPage() {
  const router = useRouter();
  const {
    email,
    accessToken,
    refreshToken,
    isTemporaryUser,
    setAuth,
  } = useAuthStore();
  const {show} = useGlobalDialog();
  useEffect(() => {
    if (!email || !accessToken || !isTemporaryUser) {
      router.replace('/');
    }
  }, [email, accessToken, isTemporaryUser, router]);

  if (!email || !accessToken || !isTemporaryUser) return null;

  return (
    <div>
      <SocialSignupForm
        email={email}
        token={accessToken}
        onSuccess={(user) => {
          setAuth({
            id: user.id,
            accessToken: accessToken,
            refreshToken: refreshToken || null,
            email: email,
            userName: user.userName,
            temporaryUser: false,
          });
          show('회원가입이 완료되었습니다!');
          router.push('/main');
        }}
      />
    </div>
  );
}
