"use client";
import { useParams, useRouter } from "next/navigation";
import { useEffect, useState } from 'react';
import { adminBidService, adminAuctionService } from '@/service/admin/auctionService';
import { BidResponseDTO, AuctionResponseDTO } from '@/types/admin/auction';
import Link from "next/link";

export default function AuctionBidHistoryPage() {
  const params = useParams();
  const router = useRouter();
  const auctionId = Number(params.auctionId);
  const [bids, setBids] = useState<BidResponseDTO[]>([]);
  const [auction, setAuction] = useState<AuctionResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");

  useEffect(() => {
    if (auctionId) {
      fetchAuctionAndBids();
    }
  }, [auctionId]);

  const fetchAuctionAndBids = async () => {
    try {
      setLoading(true);
      const [auctionData, bidsData] = await Promise.all([
        adminAuctionService.getAuctionById(auctionId),
        adminBidService.getBidsByAuction(auctionId)
      ]);
      setAuction(auctionData);
      setBids(bidsData.content);
      setError(null);
    } catch (err) {
      console.error('경매 및 입찰 내역 조회 실패:', err);
      setError('경매 정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filteredBids = bids.filter(bid => 
    bid.customerName.includes(search)
  );

  if (loading) {
    return (
      <div className="p-8">
        <div className="text-center">로딩 중...</div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-8">
        <div className="text-red-500 text-center">{error}</div>
        <button 
          onClick={fetchAuctionAndBids}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="p-8">
      {auction && (
        <div className="mb-6 p-4 bg-gray-100 rounded">
          <h2 className="text-xl font-bold mb-2">{auction.name}</h2>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div>상품명: {auction.productName}</div>
            <div>판매자: {auction.sellerName}</div>
            <div>시작가: {auction.startPrice.toLocaleString()}원</div>
            <div>현재가: {auction.currentPrice ? `${auction.currentPrice.toLocaleString()}원` : '-'}</div>
            <div>상태: {auction.status === 'ACTIVE' ? '진행중' : auction.status === 'ENDED' ? '종료' : '취소됨'}</div>
            <div>입찰 수: {bids.length}건</div>
          </div>
        </div>
      )}

      <div className="mb-4">
        <input 
          className="border px-2 py-1" 
          placeholder="입찰자 검색" 
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>

      <table className="w-full border-collapse border">
        <thead>
          <tr className="bg-gray-100">
            <th className="border px-2 py-1">입찰자</th>
            <th className="border px-2 py-1">입찰금액</th>
            <th className="border px-2 py-1">입찰시간</th>
            <th className="border px-2 py-1">낙찰여부</th>
          </tr>
        </thead>
        <tbody>
          {filteredBids.map((bid) => (
            <tr key={bid.id}>
              <td className="border px-2 py-1">{bid.customerName}</td>
              <td className="border px-2 py-1">{bid.bidAmount.toLocaleString()}원</td>
              <td className="border px-2 py-1">{new Date(bid.bidTime).toLocaleString()}</td>
              <td className="border px-2 py-1">
                {bid.isWinning ? (
                  <span className="text-green-600 font-bold">낙찰</span>
                ) : (
                  '-'
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {filteredBids.length === 0 && !loading && (
        <div className="text-center mt-4 text-gray-500">
          입찰 내역이 없습니다.
        </div>
      )}

      <div className="mt-4">
        <Link 
          href="/admin/auction-management/bid" 
          className="text-blue-600 underline"
        >
          전체 입찰 내역으로
        </Link>
      </div>
    </div>
  );
} 