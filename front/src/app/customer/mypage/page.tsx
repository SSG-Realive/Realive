'use client';

import { useEffect, useState } from 'react';
import { useRouter, usePathname } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import Navbar from '@/components/customer/common/Navbar';
import {
    Heart,
    ShoppingCart,
    Package,
    Gavel,
    Clock3,
    UserCog,
    Star,
    ReceiptText
} from 'lucide-react';

export default function MyPage() {
    const router = useRouter();
    const pathname = usePathname();
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
    const userName = useAuthStore((state) => state.userName);
    const [mounted, setMounted] = useState(false);

    useEffect(() => setMounted(true), []);

    useEffect(() => {
        if (mounted && !isAuthenticated()) {
            router.push('/customer/member/login?redirectTo=/customer/mypage');
        }
    }, [mounted, isAuthenticated, router]);

    if (!mounted || !isAuthenticated()) return null;

    return (
        <>
            <Navbar />
            <main className="min-h-screen py-5">
                <div className="max-w-6xl mx-auto bg-white rounded-xl px-6 pt-6 pb-0">
                    <div className="relative mb-6">
                        <h1 className="text-2xl font-bold">My Page</h1>
                        {userName && (
                            <span className="absolute right-0 top-7 text-sm text-gray-500">
                {userName}님, 환영합니다.
              </span>
                        )}
                    </div>

                    <hr className="invisible mb-6" />

                    {/* ✅ 주요 기능 */}
                    <section className="mt-12 mb-20">
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-6 justify-items-center">
                            <button
                                className="flex flex-col items-center justify-center w-24 h-24 rounded-full bg-black text-white hover:bg-gray-800 transition"
                                onClick={() => router.push('/customer/mypage/orders')}
                            >
                                <ReceiptText size={24} />
                                <span className="text-sm mt-1">구매내역</span>
                            </button>

                            <button
                                className="flex flex-col items-center justify-center w-24 h-24 rounded-full bg-black text-white hover:bg-gray-800 transition"
                                onClick={() => router.push('/customer/mypage/wishlist')}
                            >
                                <Heart size={27} />
                                <span className="text-sm mt-1">찜 목록</span>
                            </button>

                            <button
                                className="flex flex-col items-center justify-center w-24 h-24 rounded-full bg-black text-white hover:bg-gray-800 transition"
                                onClick={() => router.push('/customer/cart')}
                            >
                                <ShoppingCart size={27} />
                                <span className="text-sm mt-1">장바구니</span>
                            </button>

                            <button
                                className="flex flex-col items-center justify-center w-24 h-24 rounded-full bg-black text-white hover:bg-gray-800 transition"
                                onClick={() => router.push('/customer/member/auctions/won')}
                            >
                                <Gavel size={27} />
                                <span className="text-sm mt-1">낙찰한 경매</span>
                            </button>

                            <button
                                className="flex flex-col items-center justify-center w-24 h-24 rounded-full bg-black text-white hover:bg-gray-800 transition"
                                onClick={() => router.push('/customer/mypage/edit')}
                            >
                                <UserCog size={27} />
                                <span className="text-sm mt-1">개인정보</span>
                            </button>

                            <button
                                className="flex flex-col items-center justify-center w-24 h-24 rounded-full bg-black text-white hover:bg-gray-800 transition"
                                onClick={() => router.push('/customer/mypage/reviews')}
                            >
                                <Star size={27} />
                                <span className="text-sm mt-1">리뷰</span>
                            </button>
                        </div>
                    </section>

                    {/* ✅ 활동 정보 */}
                    <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div className="bg-gray-50 rounded-xl p-5 transition">
                            <div className="flex items-center mb-3 gap-2">
                                <Package className="text-gray-600" size={20} />
                                <h2 className="font-semibold text-base">주문 및 배송 현황</h2>
                            </div>
                            <p className="text-sm text-gray-600">최근 주문한 상품이 없습니다.</p>
                        </div>

                        <div className="bg-gray-50 rounded-xl p-5 transition">
                            <div className="flex items-center mb-3 gap-2">
                                <Gavel className="text-gray-600" size={20} />
                                <h2 className="font-semibold text-base">참여 중인 경매</h2>
                            </div>
                            <p className="text-sm text-gray-600">현재 참여 중인 경매가 없습니다.</p>
                        </div>

                        <div className="bg-gray-50 rounded-xl p-5 transition">
                            <div className="flex items-center mb-3 gap-2">
                                <Clock3 className="text-gray-600" size={20} />
                                <h2 className="font-semibold text-base">최근 본 상품</h2>
                            </div>
                            <p className="text-sm text-gray-600">최근 본 상품이 없습니다.</p>
                        </div>
                    </section>
                </div>
            </main>
            <div className="w-full bg-gray-100 py-8 mt-10 text-center text-sm text-gray-600">
                <p className="mb-1 font-semibold">© 2025 Realive</p>
                <p>중고 가구 거래 플랫폼 | 개인정보처리방침 | 이용약관</p>
            </div>
        </>
    );
}
