'use client';

import RegisterForm from "@/components/customer/join/RegisterForm";
import { useSearchParams, useRouter } from 'next/navigation';

export default function RegisterPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const redirectTo = searchParams.get('redirectTo') || '/main';

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-6 rounded shadow-md w-full max-w-lg">
        <h1 className="text-2xl font-bold mb-4">회원가입</h1>
        <RegisterForm
          onSuccess={() => {
            console.log('RegisterPage: onSuccess 호출됨');
            // 회원가입 성공 후 로그인 페이지로 이동 (redirectTo 파라미터 포함)
            setTimeout(() => {
              const loginUrl = `/customer/member/login?redirectTo=${encodeURIComponent(redirectTo)}`;
              console.log('RegisterPage: 로그인 페이지로 이동', loginUrl);
              window.location.href = loginUrl;
            }, 200);
          }}
        />
      </div>
    </main>
  );
}