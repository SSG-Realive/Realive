import LoginForm from '@/components/customer/auth/LoginComponent';
import { Suspense } from 'react';



export default function LoginPage() {
  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="w-full max-w-md p-6 bg-white rounded-lg shadow">
        <h1 className="text-2xl font-bold text-center mb-6">로그인</h1>
        <Suspense fallback={<div>Loading...</div>}>
          <LoginForm />
        </Suspense>
        <div className="mt-4 flex justify-between text-sm">
          <a href="/customer/member/register" className="text-blue-600 hover:underline">
            회원가입
          </a>
          <a href="/customer/member/find-password" className="text-blue-600 hover:underline">
            비밀번호 찾기
          </a>
        </div>
      </div>
    </div>
  );
}