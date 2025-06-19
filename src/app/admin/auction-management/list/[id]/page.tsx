"use client";
import { useParams, useRouter } from "next/navigation";
import { Card, CardContent } from "@/components/ui/card";
import Link from "next/link";
import { useEffect, useState } from "react";
import { adminAuctionService } from '@/service/admin/auctionService';
import { AuctionResponseDTO } from '@/types/admin/auction';

export default function AuctionDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = Number(params.id);
  const [auction, setAuction] = useState<AuctionResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      fetchAuctionDetail();
    }
  }, [id]);

  const fetchAuctionDetail = async () => {
    try {
      setLoading(true);
      const data = await adminAuctionService.getAuctionById(id);
      setAuction(data);
      setError(null);
    } catch (err) {
      console.error('경매 상세 정보 조회 실패:', err);
      setError('경매 정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

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
          onClick={fetchAuctionDetail}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded"
        >
          다시 시도
        </button>
      </div>
    );
  }

  if (!auction) {
    return (
      <div className="p-8">
        <div className="text-center">경매 정보를 찾을 수 없습니다.</div>
        <Link href="/admin/auction-management/list" className="mt-4 text-blue-600 underline">
          목록으로 돌아가기
        </Link>
      </div>
    );
  }

  return (
    <div className="p-8 max-w-lg mx-auto">
      <Card>
        <CardContent className="flex flex-col gap-4 p-6">
          {auction.productImage ? (
            <img src={auction.productImage} alt="auction" className="w-full h-48 object-cover rounded" />
          ) : (
            <div className="w-full h-48 bg-gray-200 flex items-center justify-center rounded">
              이미지 없음
            </div>
          )}
          <div className="font-bold text-xl">{auction.name}</div>
          <div>상품명: {auction.productName}</div>
          <div>판매자: {auction.sellerName}</div>
          <div>시작일: {new Date(auction.startTime).toLocaleString()}</div>
          <div>종료일: {new Date(auction.endTime).toLocaleString()}</div>
          <div>시작가: {auction.startPrice.toLocaleString()}원</div>
          <div>현재가: {auction.currentPrice ? `${auction.currentPrice.toLocaleString()}원` : '-'}</div>
          <div>즉시구매가: {auction.buyNowPrice ? `${auction.buyNowPrice.toLocaleString()}원` : '-'}</div>
          <div>상태: {auction.status === 'ACTIVE' ? '진행중' : auction.status === 'ENDED' ? '종료' : '취소됨'}</div>
          <div>카테고리: {auction.category}</div>
          {auction.status === "ENDED" && (
            <>
              <div>낙찰자: {auction.winnerName || "-"}</div>
              <div>낙찰가: {auction.winningPrice ? `${auction.winningPrice.toLocaleString()}원` : "-"}</div>
            </>
          )}
          <div className="flex gap-2 mt-4">
            <Link href="/admin/auction-management/list" className="text-blue-600 underline">
              목록으로
            </Link>
            <button 
              onClick={() => router.push(`/admin/auction-management/bid/auction/${auction.id}`)}
              className="text-blue-600 underline"
            >
              입찰 내역 보기
            </button>
          </div>
        </CardContent>
      </Card>
    </div>
  );
} 