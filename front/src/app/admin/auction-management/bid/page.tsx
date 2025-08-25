"use client";
import { useRouter } from "next/navigation";
import { useEffect, useState } from 'react';
import { adminBidService } from '@/service/admin/auctionService';
import { BidResponseDTO } from '@/types/admin/auction';

export default function BidHistoryPage() {
  const router = useRouter();
  const [bids, setBids] = useState<BidResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");

  useEffect(() => {
    // 임시로 첫 번째 경매의 입찰 내역을 가져옵니다
    // 실제로는 경매 선택 기능이 필요할 수 있습니다
    fetchBids();
  }, []);

  const fetchBids = async () => {
    try {
      setLoading(true);
      // 임시로 경매 ID 1의 입찰 내역을 가져옵니다
      const response = await adminBidService.getBidsByAuction(1);
      setBids(response.content);
      setError(null);
    } catch (err) {
      console.error('입찰 내역 조회 실패:', err);
      setError('입찰 내역을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filteredBids = bids.filter(bid => 
    bid.customerName.includes(search) || 
    bid.auctionName.includes(search)
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
          onClick={fetchBids}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="mb-4">
        <input 
          className="border px-2 py-1" 
          placeholder="입찰자/경매명 검색" 
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </div>
      <table className="w-full border-collapse border">
        <thead>
          <tr className="bg-gray-100">
            <th className="border px-2 py-1">경매명</th>
            <th className="border px-2 py-1">입찰자</th>
            <th className="border px-2 py-1">입찰금액</th>
            <th className="border px-2 py-1">입찰시간</th>
            <th className="border px-2 py-1">낙찰여부</th>
            <th className="border px-2 py-1">Action</th>
          </tr>
        </thead>
        <tbody>
          {filteredBids.map((bid) => (
            <tr key={bid.id}>
              <td className="border px-2 py-1">{bid.auctionName}</td>
              <td className="border px-2 py-1">{bid.customerName}</td>
              <td className="border px-2 py-1">{bid.bidAmount.toLocaleString()}원</td>
              <td className="border px-2 py-1">{new Date(bid.bidTime).toLocaleString()}</td>
              <td className="border px-2 py-1">{bid.isWinning ? '낙찰' : '-'}</td>
              <td className="border px-2 py-1">
                <button 
                  onClick={() => router.push(`/admin/auction-management/bid/${bid.id}`)}
                  className="px-2 py-1 bg-blue-500 text-white rounded text-sm"
                >
                  상세보기
                </button>
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
    </div>
  );
} 