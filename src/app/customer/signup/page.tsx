'use client';

import RegisterForm from "@/components/customer/join/RegisterForm";
import { useSearchParams, useRouter } from 'next/navigation';

export default function RegisterPage() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const redirectTo = searchParams.get('redirectTo') || '/'; // 기본값은 홈

  return (
    <main className="min-h-screen flex items-center justify-center bg-gray-100">
      <div className="bg-white p-6 rounded shadow-md w-full max-w-lg">
        <h1 className="text-2xl font-bold mb-4">회원가입</h1>
        <RegisterForm
          onSuccess={() => {
            console.log('RegisterPage: onSuccess 호출됨');
            router.push(redirectTo);
          }}
        />
      </div>
    </main>
  );
}
