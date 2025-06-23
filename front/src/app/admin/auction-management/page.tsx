"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { adminAuctionService, adminBidService } from '@/service/admin/auctionService';
import { AuctionResponseDTO, BidResponseDTO } from '@/types/admin/auction';

export default function AuctionManagementPage() {
  const router = useRouter();
  const [auctions, setAuctions] = useState<AuctionResponseDTO[]>([]);
  const [bids, setBids] = useState<BidResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [auctionSearch, setAuctionSearch] = useState("");
  const [bidSearch, setBidSearch] = useState("");

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [auctionsData, bidsData] = await Promise.all([
        adminAuctionService.getAuctions({ size: 5 }),
        adminBidService.getBidsByAuction(1, 0, 5) // 임시로 첫 번째 경매의 입찰 내역
      ]);
      setAuctions(auctionsData.content);
      setBids(bidsData.content);
      setError(null);
    } catch (err) {
      console.error('데이터 조회 실패:', err);
      setError('데이터를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filteredAuctions = auctions.filter(a => 
    a.name.includes(auctionSearch) || 
    a.productName.includes(auctionSearch) || 
    a.sellerName.includes(auctionSearch)
  );
  const filteredBids = bids.filter(b => 
    b.customerName.includes(bidSearch) || 
    b.auctionName.includes(bidSearch)
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
          onClick={fetchData}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="p-8 flex flex-row gap-8 overflow-x-auto">
      {/* 경매 요약 - 테이블형 */}
      <div className="bg-white rounded shadow p-6 min-w-[400px]">
        <h2 className="text-lg font-bold mb-4">경매</h2>
        <table className="min-w-full border text-sm">
          <thead>
            <tr className="bg-gray-100">
              <th className="px-2 py-1">경매명</th>
              <th className="px-2 py-1">판매자</th>
              <th className="px-2 py-1">상태</th>
            </tr>
          </thead>
          <tbody>
            {filteredAuctions.slice(0, 5).map(a => (
              <tr key={a.id}>
                <td className="px-2 py-1">{a.name}</td>
                <td className="px-2 py-1">{a.sellerName}</td>
                <td className="px-2 py-1">
                  {a.status === 'ACTIVE' ? '진행중' : a.status === 'ENDED' ? '종료' : '취소됨'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        {filteredAuctions.length === 0 && (
          <div className="text-center text-gray-500 mt-2">경매가 없습니다.</div>
        )}
      </div>

      {/* 입찰 내역 요약 - 테이블형 */}
      <div className="bg-white rounded shadow p-6 min-w-[400px]">
        <h2 className="text-lg font-bold mb-4">입찰 내역</h2>
        <table className="min-w-full border text-sm">
          <thead>
            <tr className="bg-gray-100">
              <th className="px-2 py-1">경매명</th>
              <th className="px-2 py-1">입찰자</th>
              <th className="px-2 py-1">금액</th>
            </tr>
          </thead>
          <tbody>
            {filteredBids.slice(0, 5).map((b) => (
              <tr key={b.id}>
                <td className="px-2 py-1">{b.auctionName}</td>
                <td className="px-2 py-1">{b.customerName}</td>
                <td className="px-2 py-1">{b.bidAmount.toLocaleString()}원</td>
              </tr>
            ))}
          </tbody>
        </table>
        {filteredBids.length === 0 && (
          <div className="text-center text-gray-500 mt-2">입찰 내역이 없습니다.</div>
        )}
      </div>
    </div>
  );
} 