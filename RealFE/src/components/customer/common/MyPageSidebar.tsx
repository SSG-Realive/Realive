// src/components/common/MyPageSidebar.tsx
'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import {
    FiList,
    FiHeart,
    FiShoppingCart,
    FiStar,
    FiUser,
    FiCheckCircle,
} from 'react-icons/fi';

export default function MyPageSidebar() {
    const pathname = usePathname();

    const navItems = [
        { name: '주문내역', path: '/customer/mypage/orders', icon: <FiList /> },
        { name: '찜 목록', path: '/customer/mypage/wishlist', icon: <FiHeart /> },
        { name: '장바구니', path: '/customer/cart', icon: <FiShoppingCart /> },
        { name: '리뷰', path: '/customer/mypage/reviews', icon: <FiStar /> },
        { name: '개인정보', path: '/customer/mypage/edit', icon: <FiUser /> },
        { name: '낙찰한 경매', path: '/customer/member/auctions/won', icon: <FiCheckCircle /> },
    ];

    return (
        <aside className="hidden md:block w-56 pr-6 pt-6 border-r border-gray-200 bg-white">
        <nav className="flex flex-col gap-2">
                {navItems.map((item) => {
                    const isActive = pathname === item.path;
                    return (
                        <Link
                            key={item.path}
                            href={item.path}
                            className={`flex items-center gap-3 px-4 py-2 rounded-lg text-sm font-medium transition-all duration-150
                ${isActive ? 'bg-black text-white' : 'text-gray-700 hover:bg-gray-100'}
              `}
                        >
                            <span className="text-lg">{item.icon}</span>
                            {item.name}
                        </Link>
                    );
                })}
            </nav>
        </aside>
    );
}
