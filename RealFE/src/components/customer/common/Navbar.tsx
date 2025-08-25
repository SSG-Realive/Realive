"use client";

import { useState, useEffect } from "react";
import { useRouter, useSearchParams, usePathname } from "next/navigation";
import Link from "next/link";
import Image from "next/image";

import { useAuthStore } from "@/store/customer/authStore";
import { useCartStore } from "@/store/customer/useCartStore";
import { fetchCartList } from "@/service/customer/cartService";
import { fetchMyProfile } from "@/service/customer/customerService";
import { requestLogout } from "@/service/customer/logoutService";
import SearchBar from "./SearchBar";
import CategoryDropdown from "./CategoryDropdown";
import {
    UserCircle,
    ShoppingCart,
    LogOut,
    Heart,
    LogIn,
    Menu,
    X,
} from "lucide-react";
import useDialog from "@/hooks/useDialog";
import GlobalDialog from "@/components/ui/GlobalDialog";

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
    const { show, open, message, handleClose } = useDialog();
    const [showCategory, setShowCategory] = useState(false);

    const isLoginPage =
        pathname.startsWith("/customer/member/login") ||
        pathname.startsWith("/login") ||
        pathname.startsWith("/customer/member/findPassword") ||
        pathname.startsWith("/customer/socialsignup") ||
        pathname.startsWith("/customer/signup");

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
                .catch((e) => console.error("회원 이름 조회 실패:", e));
        }
    }, [mounted, isAuthenticated, userName, setUserName]);

    const handleLogout = async () => {
        try {
            await requestLogout();
            show("로그아웃 되었습니다.");
        } catch (error) {
            console.error("Logout request failed:", error);
            show("Logout error occurred. Client will logout anyway.");
        } finally {
            clearAuthState();
            router.push("/main");
        }
    };

    const hideNavbarPaths = ["/seller", "/admin"];
    if (hideNavbarPaths.some((path) => pathname.startsWith(path))) return null;

    return (
        <>
            <GlobalDialog open={open} message={message} onClose={handleClose} />

            <nav className="sticky top-0 z-[9999] w-full bg-white/90 backdrop-blur border-b border-gray-200 shadow-sm">
                <div className="w-full px-4 py-3 space-y-2">
                    {/* ✅ PC 헤더 */}
                    <div className="hidden md:flex justify-between items-center gap-4">
                        {/* 왼쪽: 카테고리 토글 + 로고 */}
                        <div className="flex items-center gap-2 shrink-0">
                            <button
                                onClick={() => setShowCategory((prev) => !prev)}
                                className="p-2 rounded-md hover:bg-gray-100"
                                aria-label="카테고리 메뉴"
                            >
                                {showCategory ? <X size={20} /> : <Menu size={20} />}
                            </button>
                            <Link href="/main" className="flex items-center">
                                <Image
                                    src="/images/logo.png"
                                    alt="Realive 로고"
                                    width={60}
                                    height={30}
                                    className="object-contain"
                                    priority
                                />
                            </Link>
                        </div>

                        {/* 중앙: 검색창 */}
                        {!isLoginPage && (
                            <div className="flex-1 max-w-2xl">
                                <SearchBar onSearch={onSearch} />
                            </div>
                        )}

                        {/* 오른쪽: 사용자 메뉴 */}
                        <div className="flex items-center gap-3 text-gray-600 shrink-0">
                            {mounted && isAuthenticated() ? (
                                <>
                                    <Link href="/customer/mypage" title="My Page">
                                        <UserCircle size={20} />
                                    </Link>
                                    <Link href="/customer/mypage/wishlist" title="찜한 상품">
                                        <Heart size={20} className="text-gray-500" />
                                    </Link>
                                    <Link href="/customer/cart" className="relative" title="Cart">
                                        <ShoppingCart size={20} />
                                        {itemCount > 0 && (
                                            <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                        {itemCount}
                      </span>
                                        )}
                                    </Link>
                                    <button
                                        onClick={handleLogout}
                                        title="로그아웃"
                                        className="hover:text-red-500"
                                    >
                                        <LogOut size={20} />
                                    </button>
                                </>
                            ) : (
                                <Link
                                    href={`/login?redirectTo=${encodeURIComponent(
                                        pathname + (searchParams?.toString() ? `?${searchParams}` : "")
                                    )}`}
                                    title="Login"
                                    className="hover:text-blue-500"
                                >
                                    <LogIn size={20} />
                                </Link>
                            )}
                        </div>
                    </div>

                    {/* ✅ 모바일 헤더 */}
                    <div className="md:hidden flex items-center justify-between">
                        <div className="flex items-center gap-2">
                            <button
                                onClick={() => setShowCategory((prev) => !prev)}
                                className="p-2 rounded-md border border-gray-300 hover:bg-gray-100"
                                aria-label="카테고리 메뉴"
                            >
                                {showCategory ? <X size={20} /> : <Menu size={20} />}
                            </button>
                            <Link href="/main" className="flex items-center">
                                <Image
                                    src="/images/logo.png"
                                    alt="Realive 로고"
                                    width={60}
                                    height={30}
                                    className="object-contain"
                                    priority
                                />
                            </Link>
                        </div>
                        <div className="flex items-center gap-3 text-gray-600">
                            {mounted && isAuthenticated() ? (
                                <>
                                    <Link href="/customer/mypage" title="My Page">
                                        <UserCircle size={20} />
                                    </Link>
                                    <Link href="/customer/mypage/wishlist" title="찜한 상품">
                                        <Heart size={20} className="text-gray-500" />
                                    </Link>
                                    <Link href="/customer/cart" className="relative" title="Cart">
                                        <ShoppingCart size={20} />
                                        {itemCount > 0 && (
                                            <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs rounded-full w-5 h-5 flex items-center justify-center">
                        {itemCount}
                      </span>
                                        )}
                                    </Link>
                                    <button
                                        onClick={handleLogout}
                                        title="로그아웃"
                                        className="hover:text-red-500"
                                    >
                                        <LogOut size={20} />
                                    </button>
                                </>
                            ) : (
                                <Link
                                    href={`/login?redirectTo=${encodeURIComponent(
                                        pathname + (searchParams?.toString() ? `?${searchParams}` : "")
                                    )}`}
                                    title="Login"
                                    className="hover:text-blue-500"
                                >
                                    <LogIn size={20} />
                                </Link>
                            )}
                        </div>
                    </div>

                    {/* ✅ 모바일 검색창 */}
                    {!isLoginPage && (
                        <div className="md:hidden mt-2">
                            <SearchBar onSearch={onSearch} />
                        </div>
                    )}

                    {/* ✅ 카테고리 드롭다운 */}
                    {showCategory && (
                        <div className="mt-2">
                            <CategoryDropdown
                                onCategorySelect={(id) => {
                                    onCategorySelect?.(id);
                                    setShowCategory(false);
                                }}
                            />
                        </div>
                    )}
                </div>
            </nav>
        </>
    );
}
