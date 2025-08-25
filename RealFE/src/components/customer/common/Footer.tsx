'use client';

import { usePathname } from 'next/navigation';

export default function Footer() {
    const pathname = usePathname();

    const hiddenPaths = [
        '/autions',
        '/seller',
        '/admin',
        '/customer/cart',
    ];

    const shouldHide = hiddenPaths.some(
        (path) => pathname === path || pathname.startsWith(`${path}/`)
    );

    if (shouldHide) return null;

    return (
        <footer className="w-full bg-gray-100 py-8 mt-10 text-center text-sm text-gray-600">
            <p className="mb-1 font-light">© 2025 Realive</p>
            <p>중고 가구 거래 플랫폼 | 개인정보처리방침 | 이용약관</p>
        </footer>
    );
}
