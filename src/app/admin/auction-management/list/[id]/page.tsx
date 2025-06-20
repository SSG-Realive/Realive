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

  // 이미지 URL 생성 함수
  const getImageUrl = (imagePath: string): string => {
    if (!imagePath) return '/images/placeholder.png';
    if (imagePath.startsWith('http')) return imagePath;
    return `http://localhost:8080${imagePath}`;
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
    <div className="p-8 max-w-4xl mx-auto">
      <div className="bg-white rounded-lg shadow-lg p-6">
        <h1 className="text-3xl font-bold mb-6">{auction.adminProduct?.productName}</h1>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* 상품 이미지 */}
          <div>
            {auction.adminProduct?.imageThumbnailUrl ? (
              <img 
                src={getImageUrl(auction.adminProduct.imageThumbnailUrl)} 
                alt={auction.adminProduct.productName} 
                className="w-full h-64 object-cover rounded-lg"
                onError={(e) => {
                  e.currentTarget.src = '/images/placeholder.png';
                }}
              />
            ) : (
              <div className="w-full h-64 bg-gray-200 flex items-center justify-center rounded-lg">
                <span className="text-gray-500">이미지 없음</span>
              </div>
            )}
          </div>

          {/* 경매 정보 */}
          <div className="space-y-4">
            <div>
              <h2 className="text-xl font-semibold mb-3">경매 정보</h2>
              <div className="space-y-2 text-gray-700">
                <div><span className="font-medium">상품명:</span> {auction.adminProduct?.productName}</div>
                <div><span className="font-medium">상품 설명:</span> {auction.adminProduct?.productDescription}</div>
                <div><span className="font-medium">시작가:</span> {auction.startPrice?.toLocaleString()}원</div>
                <div><span className="font-medium">현재가:</span> {auction.currentPrice?.toLocaleString()}원</div>
                <div><span className="font-medium">상태:</span> 
                  <span className={`ml-2 px-2 py-1 rounded text-sm font-medium ${
                    auction.statusText === '진행중' ? 'bg-green-100 text-green-800' :
                    auction.statusText === '예정' ? 'bg-blue-100 text-blue-800' :
                    auction.statusText === '종료' ? 'bg-gray-100 text-gray-800' :
                    'bg-red-100 text-red-800'
                  }`}>
                    {auction.statusText || auction.status}
                  </span>
                </div>
                <div><span className="font-medium">시작시간:</span> {new Date(auction.startTime).toLocaleString()}</div>
                <div><span className="font-medium">종료시간:</span> {new Date(auction.endTime).toLocaleString()}</div>
                <div><span className="font-medium">등록시간:</span> {new Date(auction.createdAt).toLocaleString()}</div>
                <div><span className="font-medium">수정시간:</span> {new Date(auction.updatedAt).toLocaleString()}</div>
              </div>
            </div>

            {/* 상품 정보 */}
            <div>
              <h2 className="text-xl font-semibold mb-3">상품 정보</h2>
              <div className="space-y-2 text-gray-700">
                <div><span className="font-medium">상품 ID:</span> {auction.adminProduct?.productId}</div>
                <div><span className="font-medium">구매가:</span> {auction.adminProduct?.purchasePrice?.toLocaleString()}원</div>
                <div><span className="font-medium">판매자 ID:</span> {auction.adminProduct?.purchasedFromSellerId || 'N/A'}</div>
                <div><span className="font-medium">구매일:</span> {auction.adminProduct?.purchasedAt ? new Date(auction.adminProduct.purchasedAt).toLocaleDateString() : 'N/A'}</div>
                <div><span className="font-medium">경매 등록 여부:</span> {auction.adminProduct?.auctioned ? '등록됨' : '미등록'}</div>
              </div>
            </div>
          </div>
        </div>

        {/* 액션 버튼들 */}
        <div className="mt-8 flex gap-4">
          <button 
            onClick={() => window.history.back()}
            className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600 transition-colors"
          >
            목록으로 돌아가기
          </button>
          <button 
            onClick={() => router.push(`/admin/auction-management/bid/auction/${auction.id}`)}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors"
          >
            입찰 내역 보기
          </button>
          <button 
            className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600 transition-colors"
          >
            경매 수정
          </button>
          {auction.statusText !== '종료' && auction.statusText !== '취소' && (
            <button 
              className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 transition-colors"
            >
              경매 취소
            </button>
          )}
        </div>
      </div>
    </div>
  );
} 