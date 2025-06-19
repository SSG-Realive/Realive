'use client';

import { useState, useEffect } from 'react';
import { useRouter, useSearchParams, usePathname } from 'next/navigation';
import Link from 'next/link';

import { useAuthStore } from '@/store/customer/authStore';
import { fetchMyProfile } from '@/service/customer/customerService';
import SearchBar from './SearchBar';

interface NavbarProps {
  onSearch?: (keyword: string) => void;
}

export default function Navbar({ onSearch }: NavbarProps) {
  const router       = useRouter();
  const pathname     = usePathname();
  const searchParams = useSearchParams();

  /* 스토어 */
  const { isAuthenticated, logout, userName, setUserName } = useAuthStore();
  const [mounted, setMounted] = useState(false);

  /* CSR 하이드레이션 여부 */
  useEffect(() => setMounted(true), []);

  /* 로그인 관련 페이지에서는 네비게이션 숨김 */
  if (pathname === '/login' || pathname === '/customer/member/login' || pathname === '/seller/login') {
    return null;
  }

  /* 로그인 상태면 한 번만 프로필 이름 가져오기 */
  useEffect(() => {
    if (mounted && isAuthenticated() && !userName) {
      fetchMyProfile()
        .then((data) => setUserName(data.name))
        .catch((e) => console.error('회원 이름 조회 실패:', e));
    }
  }, [mounted, isAuthenticated, userName, setUserName]);

  /* 로그아웃 */
  const handleLogout = () => {
    logout();
    router.push('/main');
  };

  return (
    <nav className="w-full bg-white shadow-md">
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          {/* 로고 */}
          <Link href="/main" className="flex-shrink-0 text-xl font-bold text-gray-800">
            Realive
          </Link>

          {/* 검색창 */}
          <div className="flex flex-1 justify-center px-4">
            <SearchBar onSearch={onSearch} />
          </div>

          {/* 우측 메뉴 */}
          {mounted && (
            <div className="flex items-center space-x-4">
              {isAuthenticated() ? (
                // ✅ 이 부분을 제가 실수로 생략했습니다. 다시 복원했습니다.
                <>
                  {userName && (
                    <span className="hidden whitespace-nowrap text-gray-700 sm:inline">
                      {userName}님
                    </span>
                  )}
                  <Link href="/customer/mypage" className="text-gray-600 hover:text-gray-900">
                    마이페이지
                  </Link>
                  <Link href="/customer/cart" className="text-gray-600 hover:text-gray-900">
                    장바구니
                  </Link>
                  <button
                    onClick={handleLogout}
                    className="rounded-md bg-red-600 px-4 py-2 text-white hover:bg-red-700"
                  >
                    로그아웃
                  </button>
                </>
              ) : (
                // ✅ 로그인 버튼의 href만 수정한 부분입니다.
                <Link
                  href={`/login?redirectTo=${encodeURIComponent(
                    pathname + (searchParams?.toString() ? `?${searchParams}` : '')
                  )}`}
                  className="rounded-md bg-blue-600 px-4 py-2 text-white hover:bg-blue-700"
                >
                  로그인
                </Link>
              )}
            </div>
          )}
        </div>
      </div>
    </nav>
  );
}