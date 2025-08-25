import LoginForm from '@/components/customer/login/LoginComponent';
import { Suspense } from 'react';

export default function LoginPage() {
    return (
        <div className="flex flex-col flex-grow items-center justify-start min-h-full px-4 pt-10 pb-0">
            <div className="w-full max-w-md">
                <Suspense fallback={<div>Loading...</div>}>
                    <LoginForm />
                </Suspense>

                <div className="mt-4 flex justify-between text-sm">
                    <a href="/customer/signup">회원가입</a>
                    <a href="/customer/member/findPassword">비밀번호 찾기</a>
                </div>
            </div>
        </div>
    );
}
