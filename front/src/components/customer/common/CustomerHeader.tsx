'use client';

import Link from 'next/link';

export default function CustomerHeader() {
    return (
        <header className="bg-white border-b border-gray-200">
            <div className="max-w-screen-xl mx-auto px-4 py-3 flex justify-between items-center">
                {/* 로고 */}
                <Link href="/" className="text-lg font-bold text-gray-800 hover:text-green-600">
                    realize
                </Link>

                {/* 메뉴 */}
                <nav className="flex items-center gap-6 text-sm text-gray-600">
                    <Link href="/login" className="hover:text-green-600 transition">로그인</Link>
                    <Link href="/signup" className="hover:text-green-600 transition">회원가입</Link>
                    <Link href="/mypage" className="hover:text-green-600 transition">마이페이지</Link>
                    <Link href="/cart" className="hover:text-green-600 transition">장바구니</Link>
                </nav>
            </div>
        </header>
    );
}
