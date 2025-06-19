"use client";
import React, { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import Link from "next/link";
import { useRouter } from 'next/navigation';
import { adminAuctionService } from '@/service/admin/auctionService';
import { AuctionResponseDTO } from '@/types/admin/auction';

function AuctionListPage() {
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<AuctionResponseDTO | null>(null);
  const [auctions, setAuctions] = useState<AuctionResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  useEffect(() => {
    fetchAuctions();
  }, []);

  const fetchAuctions = async () => {
    try {
      setLoading(true);
      const response = await adminAuctionService.getAuctions();
      setAuctions(response.content);
      setError(null);
    } catch (err) {
      console.error('경매 목록 조회 실패:', err);
      setError('경매 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filtered = auctions.filter(a => 
    a.name.includes(search) || 
    a.productName.includes(search) || 
    a.sellerName.includes(search)
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
          onClick={fetchAuctions}
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
          type="text"
          placeholder="경매명/상품명/판매자 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      <div className="p-0 w-full max-w-none">
        <table className="w-full min-w-full table-fixed border text-sm">
          <colgroup>
            <col style={{ width: '128px' }} />
            <col style={{ width: 'auto' }} />
            <col style={{ width: '120px' }} />
          </colgroup>
        <thead>
          <tr className="bg-gray-100">
              <th className="px-2 py-1">사진</th>
              <th className="px-2 py-1">정보</th>
              <th className="px-2 py-1">상세</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(a => (
            <tr key={a.id}>
                <td className="px-2 py-1 align-top">
                  {a.productImage
                    ? <img src={a.productImage} alt="auction" className="w-32 h-32 rounded-full object-cover" style={{ minWidth: 128, minHeight: 128, maxWidth: 128, maxHeight: 128 }} />
                    : <div className="w-32 h-32 bg-gray-200 flex items-center justify-center rounded-full text-xs font-semibold" style={{ minWidth: 128, minHeight: 128, maxWidth: 128, maxHeight: 128 }}>이미지</div>
                  }
                </td>
                <td className="px-2 py-1 align-top">
                  <div className="bg-gray-200 rounded p-3 flex flex-col gap-10 divide-y divide-gray-300">
                    <div className="font-bold h-32 flex items-center text-2xl py-2">{a.name}</div>
                    <div className="font-bold py-2">경매 시작 시간: {new Date(a.startTime).toLocaleString()}</div>
                    <div className="font-bold py-2">경매 시작가: {a.startPrice.toLocaleString()}원</div>
                    <div className="font-bold py-2">경매 마감 시간: {new Date(a.endTime).toLocaleString()}</div>
                    <div className="font-bold py-2">즉시 구매가: {a.buyNowPrice ? `${a.buyNowPrice.toLocaleString()}원` : '-'}</div>
                    <div className="font-bold py-2">상태: {a.status === 'ACTIVE' ? '진행중' : a.status === 'ENDED' ? '종료' : '취소됨'}</div>
                    {a.status === "ENDED" && (
                      <>
                        <div className="font-bold py-2">낙찰자: {a.winnerName || '-'}</div>
                        <div className="font-bold py-2">낙찰가: {a.winningPrice ? `${a.winningPrice.toLocaleString()}원` : '-'}</div>
                      </>
                    )}
                  </div>
                </td>
                <td className="px-2 py-1 align-top">
                  <Link href={`/admin/auction-management/list/${a.id}`} className="text-blue-600 underline">상세조회</Link>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      </div>
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[320px]">
            <h2 className="text-xl font-bold mb-4">경매 상세</h2>
            {selected.productImage && (
              <img src={selected.productImage} alt="auction" className="mb-4 w-40 h-40 object-cover rounded" />
            )}
            <p><b>경매명:</b> {selected.name}</p>
            <p><b>상품명:</b> {selected.productName}</p>
            <p><b>판매자:</b> {selected.sellerName}</p>
            <p><b>시작일:</b> {new Date(selected.startTime).toLocaleString()}</p>
            <p><b>종료일:</b> {new Date(selected.endTime).toLocaleString()}</p>
            <p><b>상태:</b> {selected.status === 'ACTIVE' ? '진행중' : selected.status === 'ENDED' ? '종료' : '취소됨'}</p>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelected(null)}>닫기</button>
          </div>
        </div>
      )}
    </div>
  );
} 

export default Object.assign(AuctionListPage, { pageTitle: '경매 목록' }); 