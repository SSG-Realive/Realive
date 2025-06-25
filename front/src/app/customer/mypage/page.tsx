'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import Navbar from '@/components/customer/common/Navbar';
import { Package, Gavel, Clock3 } from 'lucide-react';

export default function MyPage() {
    const router = useRouter();
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated);
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
            <main className="max-w-6xl mx-auto p-6">
                <h1 className="text-2xl font-bold mb-8">My Page</h1>

                {/* ✅ 주요 기능 */}
                <section className="mb-12">
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                        <button
                            className="bg-black text-white p-4 rounded hover:bg-gray-800 transition"
                            onClick={() => router.push('/customer/mypage/wishlist')}
                        >
                            찜 목록
                        </button>
                        <button
                            className="bg-black text-white p-4 rounded hover:bg-gray-800 transition"
                            onClick={() => router.push('/customer/cart')}
                        >
                            장바구니
                        </button>
                        <button
                            className="bg-black text-white p-4 rounded hover:bg-gray-800 transition"
                            onClick={() => router.push('/customer/member/auctions/won')}
                        >
                            낙찰한 경매
                        </button>
                        <button
                            className="bg-black text-white p-4 rounded hover:bg-gray-800 transition"
                            onClick={() => router.push('/customer/mypage/edit')}
                        >
                            개인정보 관리
                        </button>
                        <button
                            className="bg-black text-white p-4 rounded hover:bg-gray-800 transition"
                            onClick={() => router.push('/customer/mypage/reviews')}
                        >
                            리뷰
                        </button>
                    </div>
                </section>

                {/* ✅ 활동 정보 */}
                <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div className="bg-gray-50 rounded-xl p-5 shadow hover:shadow-md transition">
                        <div className="flex items-center mb-3 gap-2">
                            <Package className="text-gray-600" size={20} />
                            <h2 className="font-semibold text-base">주문 및 배송 현황</h2>
                        </div>
                        <p className="text-sm text-gray-600">최근 주문한 상품이 없습니다.</p>
                    </div>

                    <div className="bg-gray-50 rounded-xl p-5 shadow hover:shadow-md transition">
                        <div className="flex items-center mb-3 gap-2">
                            <Gavel className="text-gray-600" size={20} />
                            <h2 className="font-semibold text-base">참여 중인 경매</h2>
                        </div>
                        <p className="text-sm text-gray-600">현재 참여 중인 경매가 없습니다.</p>
                    </div>

                    <div className="bg-gray-50 rounded-xl p-5 shadow hover:shadow-md transition">
                        <div className="flex items-center mb-3 gap-2">
                            <Clock3 className="text-gray-600" size={20} />
                            <h2 className="font-semibold text-base">최근 본 상품</h2>
                        </div>
                        <p className="text-sm text-gray-600">최근 본 상품이 없습니다.</p>
                    </div>
                </section>
            </main>
        </>
    );
}
