'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';

import { useAuthStore } from '@/store/customer/authStore';
import { useCartStore } from '@/store/customer/useCartStore';
import { fetchCartList } from '@/service/customer/cartService';
import { fetchMyProfile } from '@/service/customer/customerService';
import { requestLogout } from '@/service/customer/logoutService';

import SearchBar from './SearchBar';
import CategoryDropdown from './CategoryDropdown';
import { UserCircle, ShoppingCart, LogOut } from 'lucide-react';
import { FaRegHeart } from 'react-icons/fa';

interface NavbarProps {
    onSearch?: (keyword: string) => void;
    onCategorySelect?: (id: number) => void;
}

export default function Navbar({ onSearch, onCategorySelect }: NavbarProps) {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();
    const { logout: clearAuthState } = useAuthStore();

    const { isAuthenticated, userName, setUserName } = useAuthStore();
    const { itemCount } = useCartStore();
    const [mounted, setMounted] = useState(false);

    useEffect(() => setMounted(true), []);

    useEffect(() => {
        if (mounted && isAuthenticated()) {
            fetchCartList();
        }
    }, [mounted, isAuthenticated]);

    useEffect(() => {
        if (mounted && isAuthenticated() && !userName) {
            fetchMyProfile()
                .then((data) => setUserName(data.name))
                .catch((e) => console.error('회원 이름 조회 실패:', e));
        }
    }, [mounted, isAuthenticated, userName, setUserName]);

    const handleLogout = async () => {
        try {
            await requestLogout();
            alert('You have been safely logged out.');
        } catch (error) {
            console.error('Logout request failed:', error);
            alert('Logout error occurred. Client will logout anyway.');
        } finally {
            clearAuthState();
            router.push('/main');
        }
    };

    if (
        pathname === '/login' ||
        pathname === '/customer/member/login' ||
        pathname === '/seller/login'
    ) {
        return null;
    }

    return (
        <nav className="w-full sticky top-0 z-50 bg-white/85 backdrop-blur-sm">
            <div className="w-full px-4 py-3 space-y-4">
                {/* ✅ PC 헤더 */}
                <div className="hidden md:grid grid-cols-[auto_1fr_auto] items-center w-full">
                    <Link href="/main" className="flex-shrink-0">
                        <Image
                            src="/images/logo.png"
                            alt="Realive 로고"
                            width={120}
                            height={30}
                            className="object-contain drop-shadow-sm"
                        />
                    </Link>

                    <div className="flex justify-center">
                        <div className="w-full max-w-[900px] px-2">
                            <SearchBar onSearch={onSearch} />
                        </div>
                    </div>

                    {mounted && (
                        <div className="flex items-center justify-end space-x-4 text-sm text-gray-700">
                            {isAuthenticated() ? (
                                <>
                                    <Link href="/customer/mypage" className="hover:text-gray-800" title="My Page">
                                        <UserCircle size={20} />
                                    </Link>

                                    <Link href="/customer/mypage/wishlist" className="hover:text-gray-800" title="찜한 상품">
                                        <FaRegHeart className="w-5 h-5 text-gray-500" />
                                    </Link>

                                    <Link href="/customer/cart" className="relative hover:text-gray-800" title="Cart">
                                        <ShoppingCart size={20} />
                                        {itemCount > 0 && (
                                            <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                        {itemCount}
                      </span>
                                        )}
                                    </Link>

                                    {/* ✅ PC: 텍스트 버튼만 */}
                                    <button
                                        onClick={handleLogout}
                                        className="hidden md:block text-xs hover:text-red-500"
                                    >
                                        LOGOUT
                                    </button>

                                    {/* ✅ 모바일: 아이콘만 */}
                                    <button
                                        onClick={handleLogout}
                                        title="로그아웃"
                                        className="block md:hidden hover:text-red-500 min-w-[24px]"
                                    >
                                        <LogOut size={20} />
                                    </button>
                                </>
                            ) : (
                                <Link
                                    href={`/login?redirectTo=${encodeURIComponent(
                                        pathname + (searchParams?.toString() ? `?${searchParams}` : '')
                                    )}`}
                                    className="hover:text-blue-500 text-xs"
                                >
                                    LOGIN
                                </Link>
                            )}
                        </div>
                    )}
                </div>

                {/* ✅ PC 카테고리 */}
                {pathname.startsWith('/main') && (
                    <div className="hidden md:block mt-4">
                        <CategoryDropdown onCategorySelect={onCategorySelect} />
                    </div>
                )}

                {/* ✅ 모바일 헤더 */}
                <div className="flex items-center justify-between md:hidden">
                    <Link href="/main" className="flex items-center">
                        <Image
                            src="/images/logo.png"
                            alt="Realive 로고"
                            width={120}
                            height={30}
                            className="object-contain"
                            priority
                        />
                    </Link>

                    {mounted && (
                        <div className="flex items-center space-x-3 text-sm text-gray-600">
                            {isAuthenticated() ? (
                                <>
                                    <Link href="/customer/mypage" title="My Page">
                                        <UserCircle size={20} />
                                    </Link>

                                    <Link href="/customer/mypage/wishlist" title="찜한 상품">
                                        <FaRegHeart className="w-5 h-5 text-gray-500" />
                                    </Link>

                                    <Link href="/customer/cart" className="relative" title="Cart">
                                        <ShoppingCart size={20} />
                                        {itemCount > 0 && (
                                            <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                        {itemCount}
                      </span>
                                        )}
                                    </Link>

                                    {/* ✅ 모바일: 아이콘만 */}
                                    <button
                                        onClick={handleLogout}
                                        title="로그아웃"
                                        className="block md:hidden hover:text-red-500 min-w-[24px]"
                                    >
                                        <LogOut size={20} />
                                    </button>
                                </>
                            ) : (
                                <Link
                                    href={`/login?redirectTo=${encodeURIComponent(
                                        pathname + (searchParams?.toString() ? `?${searchParams}` : '')
                                    )}`}
                                    className="hover:text-blue-500 text-xs"
                                >
                                    LOGIN
                                </Link>
                            )}
                        </div>
                    )}
                </div>

                {/* ✅ 모바일 검색창 */}
                <div className="md:hidden">
                    <SearchBar onSearch={onSearch} />
                </div>

                {/* ✅ 모바일 카테고리 */}
                {pathname.startsWith('/main') && (
                    <div className="block md:hidden mt-4">
                        <CategoryDropdown onCategorySelect={onCategorySelect} />
                    </div>
                )}
            </div>
        </nav>
    );
}
