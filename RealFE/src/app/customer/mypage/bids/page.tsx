'use client';

import { useState, useEffect } from 'react';
import Image from 'next/image';
import { useRouter } from 'next/navigation';

import {
  customerBidService,
  customerAuctionService,
} from '@/service/customer/auctionService';
import { publicAuctionService }  from '@/service/customer/publicAuctionService';
import type { Bid, Auction }     from '@/types/customer/auctions';

import GlobalDialog        from '@/components/ui/GlobalDialog';
import useRequireAuth      from '@/hooks/useRequireAuth';
import { useGlobalDialog } from '@/app/context/dialogContext';
import { useAuthStore }    from '@/store/customer/authStore';

import AuctionCard from '@/components/customer/auctions/AuctionCard'; // ⬅️ 마퀴 슬라이더

/* 카드 한 장 */
function BidCard({
  bid,
  auction,
  leading,
}: {
  bid: Bid;
  auction: Auction;
  leading: boolean;
}) {
  const router = useRouter();
  const current = auction.currentPrice;

  return (
    <div
      onClick={() => router.push(`/auctions/${bid.auctionId}`)}
      className="flex gap-4 p-4 rounded-lg hover:shadow transition
                 cursor-pointer"
    >
      {/* 썸네일 */}
      <div className="relative w-24 h-24 shrink-0 rounded-lg overflow-hidden bg-gray-100">
        <Image
          src={auction.adminProduct?.imageUrl || '/images/placeholder.png'}
          alt={auction.adminProduct?.productName || 'thumb'}
          fill
          className="object-cover"
        />
      </div>

      {/* 정보 */}
      <div className="flex-1 space-y-1 overflow-hidden">
        <p className="font-light truncate">
          {auction.adminProduct?.productName ?? `경매 #${bid.auctionId}`}
        </p>
        <p className="text-sm">
          내 입찰가&nbsp;
          <b>{bid.bidPrice.toLocaleString()}원</b>
        </p>
        <p className="text-sm">
          현재가&nbsp;
          {current.toLocaleString()}원
        </p>
        <span
          className={`px-2 py-0.5 text-xs font-light rounded-full
            ${leading ? 'bg-blue-100 text-blue-700'
                       : 'bg-red-100 text-red-700'}`}
        >
          {leading ? '상위 입찰' : '경쟁 중'}
        </span>
      </div>
    </div>
  );
}

export default function MyBidCardsPage() {
  /* 로그인 가드 */
  const accessToken = useRequireAuth();
  const myId        = useAuthStore((s) => s.id);
  const { show, open, message, handleClose } = useGlobalDialog();

  /* 상태 */
  const [cards, setCards] = useState<
    { bid: Bid; auction: Auction; leading: boolean }[]
  >([]);
  const [publicAuctions, setPublicAuctions] = useState<Auction[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!accessToken) return;

    (async () => {
      try {
        /* 1. 나의 모든 입찰 (desc) */
        const all = (
          await customerBidService.getMyBids({ page: 0, size: 999 })
        ).content;

        /* 2. auctionId별 가장 최근 1건만 보관 */
        const latestMap = new Map<number, Bid>();
        for (const b of all) {
          if (!latestMap.has(b.auctionId)) latestMap.set(b.auctionId, b);
        }
        const latest = [...latestMap.values()];

        /* 3. 각 경매 상세 조회 */
        const auctions = await Promise.all(
          latest.map((b) => customerAuctionService.getAuctionById(b.auctionId)),
        );

        /* 4. 카드 데이터 조합 */
        const list = latest.map((b, idx) => {
          const au = auctions[idx];
          const leading =
            b.bidPrice >= au.currentPrice && b.customerId === myId;
          return { bid: b, auction: au, leading };
        });
        setCards(list);

        /* 5. 하단 슬라이더용 공개 경매 */
        const pub = await publicAuctionService.fetchPublicActiveAuctions();
        setPublicAuctions(pub.content);
      } catch {
        show('입찰 내역을 불러오지 못했습니다.');
      } finally {
        setLoading(false);
      }
    })();
  }, [accessToken, myId, show]);

  if (!accessToken) return null;

  /* ───────── UI ───────── */
  return (
    <>
      <GlobalDialog open={open} message={message} onClose={handleClose} />

      <main className="max-w-5xl mx-auto px-4 py-8">
        {/* 카드 그리드 */}
        {cards.length === 0 && !loading ? (
          <p className="text-gray-600">참여한 경매가 없습니다.</p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {cards.map(({ bid, auction, leading }) => (
              <BidCard
                key={`${bid.id}-${bid.auctionId}`}
                bid={bid}
                auction={auction}
                leading={leading}
              />
            ))}
          </div>
        )}

        {/* 하단 마퀴 슬라이더 */}
        {publicAuctions.length > 0 && (
          <section className="mt-12">
            <h2 className="text-sm font-light text-gray-400 mb-3">
              놓치면 아쉬운 다른 경매들도 있어요
            </h2>
            <AuctionCard auctions={publicAuctions} />
          </section>
        )}
      </main>
    </>
  );
}
