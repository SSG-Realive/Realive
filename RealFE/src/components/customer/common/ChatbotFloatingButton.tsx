'use client';

import { BsChatDotsFill } from 'react-icons/bs';
import { usePathname } from 'next/navigation';

export default function ChatbotFloatingButton() {
    const pathname = usePathname();

    const hiddenPaths = [
        '/autions',
        '/login',
        '/seller',
        '/admin',
        '/customer/signup',
        '/customer/member/login',
        '/customer/cart',
    ];

    const shouldHide = hiddenPaths.some(
        (path) => pathname === path || pathname.startsWith(`${path}/`)
    );

    if (shouldHide) return null;

    return (
        <button
            className="fixed bottom-6 left-6 z-[9999] w-12 h-12 flex items-center justify-center
             rounded-full bg-gray-200 text-gray-700 shadow-md hover:shadow-lg
             hover:bg-gray-300 transition-all duration-200"
            onClick={() => {
                // TODO: 챗봇 팝업 또는 이동
            }}
        >
            <BsChatDotsFill className="text-xl" />
        </button>
    );
}
