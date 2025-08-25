'use client';

import { useEffect, useState, useRef } from 'react';
import { useRouter } from 'next/navigation';
import {
    Heart, ShoppingCart, Package, Gavel,
    UserCog, Star, ReceiptText, ChevronLeft
} from 'lucide-react';

import { useAuthStore } from '@/store/customer/authStore';
import { customerBidService } from '@/service/customer/auctionService';
import type { Bid } from '@/types/customer/auctions';
import { Order } from '@/types/customer/order/order';
import { getRecentOrder } from '@/service/order/orderService';
import { useGlobalDialog } from '@/app/context/dialogContext';
import OrderPreviewCard from '@/components/customer/order/OrderPreviewCard';

export default function MyPage() {
    const router = useRouter();
    const { show } = useGlobalDialog();

    const isAuthenticatedFn = useAuthStore((s) => s.isAuthenticated);
    const userName = useAuthStore((s) => s.userName);

    const [mounted, setMounted] = useState(false);
    useEffect(() => setMounted(true), []);

    const [recentOrder, setRecentOrder] = useState<Order | null>(null);
    const [bids, setBids] = useState<Bid[]>([]);
    const [page, setPage] = useState(0);
    const ITEMS_PER_PAGE = 4;

    const totalPages = Math.ceil(bids.length / ITEMS_PER_PAGE);
    const canPrev = page > 0;
    const canNext = page < totalPages - 1;

    // 최근 주문 정보 호출
    useEffect(() => {
        if (!isAuthenticatedFn()) return;

        (async () => {
            try {
                const data = await getRecentOrder();
                setRecentOrder(data);
            } catch {
                console.warn('최근 주문 정보를 불러오지 못했습니다.');
            }
        })();
    }, [isAuthenticatedFn]);

    // 입찰 내역 호출
    useEffect(() => {
        if (!isAuthenticatedFn()) return;

        (async () => {
            try {
                const res = await customerBidService.getMyBids({ page: 0, size: 999 });
                setBids(res.content);
                if (res.content.length === 0) {
                    await show('현재 참여 중인 경매가 없습니다.');
                }
            } catch {
                await show('입찰 내역을 불러오지 못했습니다.');
            }
        })();
    }, [show, isAuthenticatedFn]);

    useEffect(() => {
        if (mounted && !isAuthenticatedFn()) {
            router.push('/customer/member/login?redirectTo=/customer/mypage');
        }
    }, [mounted, isAuthenticatedFn, router]);

    const touchX = useRef(0);
    const notReady = !mounted || !isAuthenticatedFn();
    if (notReady) return null;

    return (
        <main className="min-h-screen py-5">
            <div className="max-w-6xl mx-auto bg-white rounded-xl px-6 pt-6">
                {/* 타이틀 */}
                <div className="mb-6">
                    {userName && (
                        <span className="text-sm text-gray-600 font-light">
                            {userName}님, 환영합니다.
                        </span>
                    )}
                </div>

                <hr className="invisible mb-6" />

                {/* 주요 메뉴 */}
                <section className="mt-12 mb-20">
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-6 justify-items-center">
                        <CircleBtn label="주문내역" icon={<ReceiptText size={24} />} onClick={() => router.push('/customer/mypage/orders')} />
                        <CircleBtn label="찜 목록" icon={<Heart size={27} />} onClick={() => router.push('/customer/mypage/wishlist')} />
                        <CircleBtn label="장바구니" icon={<ShoppingCart size={27} />} onClick={() => router.push('/customer/cart')} />
                        <CircleBtn label="낙찰한 경매" icon={<Gavel size={27} />} onClick={() => router.push('/customer/member/auctions/won')} />
                        <CircleBtn label="개인정보" icon={<UserCog size={27} />} onClick={() => router.push('/customer/mypage/edit')} />
                        <CircleBtn label="리뷰" icon={<Star size={27} />} onClick={() => router.push('/customer/mypage/reviews')} />
                    </div>
                </section>

                {/* 활동 정보 */}
                <section className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {/* 주문/배송 */}
                    <div className="relative bg-white-50 rounded-xl p-5">
                        <div className="flex items-center mb-3 gap-2">
                            <Package className="text-gray-600" size={20} />
                            <h2 className="font-light text-base">최근 주문 및 배송 현황</h2>
                            <button
                                onClick={() => router.push('/customer/mypage/orders')}
                                className="absolute top-2 right-2 text-xs text-gray-500 hover:text-gray-700"
                            >
                                전체보기
                            </button>
                        </div>
                        <div className="text-sm text-gray-600">
                            {recentOrder ? (
                                <OrderPreviewCard order={recentOrder} />
                            ) : (
                                <p className="text-sm text-gray-600">최근 주문한 상품이 없습니다.</p>
                            )}
                        </div>
                    </div>


                    {/* 참여 중인 경매 */}
                    <div className="relative bg-white-50 rounded-xl p-5 cursor-pointer">
                        <div className="flex items-center mb-3 gap-2">
                            <Gavel className="text-gray-600" size={20} />
                            <h2 className="font-light text-base">참여 중인 경매</h2>
                        </div>

                        {bids.length === 0 ? (
                            <p className="text-sm text-gray-600">현재 참여 중인 경매가 없습니다.</p>
                        ) : (
                            <div className="relative">
                                {canPrev && (
                                    <NavBtn dir="left" onClick={(e) => { e.stopPropagation(); setPage(p => p - 1); }} />
                                )}
                                {canNext && (
                                    <NavBtn dir="right" onClick={(e) => { e.stopPropagation(); setPage(p => p + 1); }} />
                                )}

                                <div
                                    className="overflow-hidden"
                                    onTouchStart={(e) => { touchX.current = e.touches[0].clientX; }}
                                    onTouchEnd={(e) => {
                                        const dx = e.changedTouches[0].clientX - touchX.current;
                                        if (dx > 50 && canPrev) setPage(p => p - 1);
                                        if (dx < -50 && canNext) setPage(p => p + 1);
                                    }}
                                >
                                    <div
                                        className="flex transition-transform duration-300"
                                        style={{ transform: `translateX(-${page * 100}%)` }}
                                    >
                                        {bids.map((b) => (
                                            <div key={b.id} className="shrink-0 w-full px-1">
                                                <div className="w-[95%] md:w-[92%] mx-auto bg-white rounded-lg px-4 py-2 shadow-sm hover:shadow flex items-center justify-between">
                                                    <div className="flex items-center gap-2">
                                                        <span className="text-xs text-gray-500">#{b.auctionId}</span>
                                                        <span
                                                            className={`text-[11px] font-light px-2 py-[1px] rounded-full
                                                                ${b.leading
                                                                ? 'bg-blue-100 text-blue-700'
                                                                : 'bg-red-100 text-red-700'}`}
                                                        >
                                                            {b.leading ? '상위 입찰' : '경쟁 중'}
                                                        </span>
                                                    </div>
                                                    <p className="text-sm font-light">{b.bidPrice.toLocaleString()}원</p>
                                                </div>
                                            </div>
                                        ))}
                                    </div>
                                </div>
                            </div>
                        )}

                        <button
                            onClick={() => router.push('/customer/mypage/bids')}
                            className="absolute top-2 right-2 text-xs text-gray-500 hover:text-gray-700"
                        >
                            전체보기
                        </button>
                    </div>
                </section>
            </div>
        </main>
    );
}

/* ===== 헬퍼 컴포넌트 ===== */
function CircleBtn({ label, icon, onClick }: { label: string; icon: React.ReactNode; onClick: () => void }) {
    return (
        <button
            className="flex flex-col items-center justify-center w-24 h-24 rounded-full
                 bg-black text-white hover:bg-gray-800 transition"
            onClick={onClick}
        >
            {icon}
            <span className="text-sm mt-1">{label}</span>
        </button>
    );
}

function NavBtn({
                    dir,
                    onClick,
                }: {
    dir: 'left' | 'right';
    onClick: React.MouseEventHandler<HTMLButtonElement>;
}) {
    return (
        <button
            onClick={onClick}
            className={`absolute top-1/2 -translate-y-1/2 bg-white shadow rounded-full p-1
        md:flex hidden z-20
        ${dir === 'left' ? '-left-3' : '-right-3'}`}
        >
            {dir === 'left' ? <ChevronLeft size={18} /> : null}
        </button>
    );
}
