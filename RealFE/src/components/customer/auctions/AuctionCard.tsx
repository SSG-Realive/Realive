'use client'

import { Auction } from '@/types/customer/auctions'
import Link from 'next/link'
import { useEffect, useRef } from 'react'

/* ──────────────── 개별 카드 컴포넌트 ──────────────── */
export function AuctionItemCard({ auction }: { auction: Auction }) {
    const {
        id,
        startPrice,
        currentPrice,
        endTime,
        adminProduct,
    } = auction

    return (
        <li className="shrink-0 w-64 sm:w-96 px-3">
            <Link
                href={`/auctions/${id}`}
                className="block rounded-2xl bg-white shadow hover:shadow-md transition"
            >
                {/* 이미지 영역 */}
                <div className="aspect-[1] bg-gray-100 rounded-none overflow-hidden">
                    <img
                        src={
                            adminProduct?.imageUrls?.[0] ||
                            adminProduct?.imageThumbnailUrl ||
                            '/images/placeholder.png'
                        }
                        alt={adminProduct?.productName || '상품'}
                        className="w-full h-full object-contain"
                    />
                </div>

                {/* 텍스트 영역 */}
                <div className="p-4 text-left">
                    <p className="text-base font-light text-gray-900 text-center truncate">
                        {adminProduct?.productName ?? '상품 없음'}
                    </p>
                    <p className="text-sm font-light text-gray-800 text-center truncate">
                        시작가 <span className="ml-1">KRW {startPrice?.toLocaleString()}</span>
                    </p>
                    <p className="text-sm font-bold text-gray-800 text-center truncate">
                        현재가 <span className="ml-1">KRW {currentPrice?.toLocaleString()}</span>
                    </p>
                    <p className="text-sm font-light text-red-500 text-center truncate mt-0.5">
                        종료 {endTime ? new Date(endTime).toLocaleString() : '-'}
                    </p>
                </div>
            </Link>
        </li>
    )
}

/* ──────────────── 마퀴 슬라이드 컴포넌트 ──────────────── */
export default function AuctionCard({ auctions }: { auctions: Auction[] }) {
    const trackRef = useRef<HTMLUListElement>(null)

    useEffect(() => {
        const calc = () => {
            if (!trackRef.current) return
            const len =
                Array.from(trackRef.current.children).reduce(
                    (s, el) => s + (el as HTMLElement).offsetWidth,
                    0,
                ) || 1
            trackRef.current.style.setProperty('--track-len', `${len}px`)
        }
        calc()
        window.addEventListener('resize', calc)
        return () => window.removeEventListener('resize', calc)
    }, [auctions])

    return (
        <div className="relative overflow-x-auto no-scrollbar w-full">
            <ul
                ref={trackRef}
                className="auction-track flex gap-3 py-2 w-full select-none"
            >
                {[...auctions, ...auctions].map((a, i) => (
                    <AuctionItemCard key={`${a.id}-${i}`} auction={a} />
                ))}
            </ul>

            <style jsx>{`
        .auction-track {
          animation: scroll var(--scroll-time, 30s) linear infinite;
        }
        .auction-track:hover {
          animation-play-state: paused;
        }
        @keyframes scroll {
          from {
            transform: translateX(0);
          }
          to {
            transform: translateX(calc(var(--track-len) / -2));
          }
        }
      `}</style>
        </div>
    )
}
