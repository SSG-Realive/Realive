'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';

import { useAuthStore } from '@/store/customer/authStore';
import { fetchMyProfile } from '@/service/customer/customerService';
import SearchBar from './SearchBar';
import { requestLogout } from '@/service/customer/logoutService';
import CategoryDropdown from './CategoryDropdown';

interface NavbarProps {
    onSearch?: (keyword: string) => void;
    onCategorySelect?: (id: number) => void;
}

export default function Navbar({ onSearch, onCategorySelect }: NavbarProps) {
    const router = useRouter();
    const pathname = usePathname();
    const searchParams = useSearchParams();
    const { logout: clearAuthState } = useAuthStore();

    const { isAuthenticated, logout, userName, setUserName } = useAuthStore();
    const [mounted, setMounted] = useState(false);

    useEffect(() => setMounted(true), []);

    if (
        pathname === '/login' ||
        pathname === '/customer/member/login' ||
        pathname === '/seller/login'
    ) {
        return null;
    }

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
            alert('안전하게 로그아웃 되었습니다.');
        } catch (error) {
            console.error('서버 로그아웃 요청 실패:', error);
            alert('로그아웃 처리 중 오류가 발생했지만, 클라이언트에서는 로그아웃됩니다.');
        } finally {
            clearAuthState();
            router.push('/main');
        }
    };

    return (
        <nav className="w-full sticky top-0 z-50 bg-white border-b border-gray-200 shadow-sm">
            <div className="w-full px-4 py-3 space-y-3">
                {/* ✅ PC 화면 */}
                <div className="hidden md:grid grid-cols-[auto_1fr_auto] items-center w-full">
                    <Link href="/main" className="flex-shrink-0">
                        <Image
                            src="/images/logo.png"
                            alt="Realive 로고"
                            width={120}
                            height={30}
                            className="object-contain"
                            priority
                        />
                    </Link>

                    <div className="flex justify-center">
                        <div className="w-full max-w-[900px]">
                            <SearchBar onSearch={onSearch} />
                        </div>
                    </div>

                    {mounted && (
                        <div className="flex items-center justify-end space-x-3 text-xs text-gray-600">
                            {isAuthenticated() ? (
                                <>
                                    {userName && <span className="hidden sm:inline">{userName}님</span>}
                                    <Link href="/customer/mypage" className="hover:text-gray-800">마이페이지</Link>
                                    <Link href="/customer/cart" className="hover:text-gray-800">장바구니</Link>
                                    <button onClick={handleLogout} className="hover:text-red-500">로그아웃</button>
                                </>
                            ) : (
                                <Link
                                    href={`/login?redirectTo=${encodeURIComponent(
                                        pathname + (searchParams?.toString() ? `?${searchParams}` : '')
                                    )}`}
                                    className="hover:text-blue-500"
                                >
                                    로그인
                                </Link>
                            )}
                        </div>
                    )}
                </div>

                {/* ✅ PC 카테고리 드롭다운 */}
                <div className="hidden md:block">
                    <CategoryDropdown onCategorySelect={onCategorySelect} />
                </div>

                {/* ✅ 모바일 상단 */}
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
                                    <Link href="/customer/mypage" className="hover:text-gray-800">마이페이지</Link>
                                    <Link href="/customer/cart" className="hover:text-gray-800">장바구니</Link>
                                    <button onClick={handleLogout} className="hover:text-red-500">로그아웃</button>
                                </>
                            ) : (
                                <Link
                                    href={`/login?redirectTo=${encodeURIComponent(
                                        pathname + (searchParams?.toString() ? `?${searchParams}` : '')
                                    )}`}
                                    className="hover:text-blue-500"
                                >
                                    로그인
                                </Link>
                            )}
                        </div>
                    )}
                </div>

                {/* ✅ 모바일 검색창 */}
                <div className="md:hidden">
                    <SearchBar onSearch={onSearch} />
                </div>

                {/* ✅ 모바일 카테고리 드롭다운 */}
                <div className="block md:hidden">
                    <CategoryDropdown onCategorySelect={onCategorySelect} />
                </div>
            </div>
        </nav>
    );
}
