'use client';
import React, { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuctionStore } from '@/store/admin/auctionStore';

const AuctionListPage = () => {
  const router = useRouter();
  const { auctions, loading, fetchAuctions } = useAuctionStore();

  useEffect(() => {
    fetchAuctions();
  }, [fetchAuctions]);

  if (loading) return <div className="p-8 text-center">로딩 중...</div>;

  return (
    <div className="p-8">
      <h2 className="text-2xl font-bold mb-6">준비중인 경매 상품</h2>
      <div className="space-y-4">
        {auctions.length === 0 ? (
          <div className="text-center text-gray-500">등록된 경매가 없습니다.</div>
        ) : (
          auctions.map((auction) => (
            <div key={auction.id} className="flex bg-white border rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow">
              <div className="w-32 h-32 bg-gray-200 rounded-lg mr-6 flex items-center justify-center">
                {auction.adminProduct?.imageThumbnailUrl ? (
                  <img 
                    src={auction.adminProduct.imageThumbnailUrl.startsWith('http') 
                      ? auction.adminProduct.imageThumbnailUrl 
                      : `http://localhost:8080${auction.adminProduct.imageThumbnailUrl}`} 
                    alt={auction.adminProduct.productName}
                    className="w-full h-full object-cover rounded-lg"
                    onError={(e) => {
                      e.currentTarget.src = '/images/placeholder.png';
                    }}
                  />
                ) : (
                  <span className="text-gray-500 text-sm">이미지 없음</span>
                )}
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-semibold mb-2">{auction.adminProduct?.productName || '상품명 없음'}</h3>
                <div className="space-y-1 text-gray-600">
                  <div>경매 시작: {auction.startTime ? new Date(auction.startTime).toLocaleString() : 'N/A'}</div>
                  <div>경매 마감: {auction.endTime ? new Date(auction.endTime).toLocaleString() : 'N/A'}</div>
                  <div>시작가: {auction.startPrice?.toLocaleString()}원</div>
                  <div>현재가: {auction.currentPrice?.toLocaleString()}원</div>
                  <div>
                    상태: 
                    <span className={`ml-2 px-2 py-1 rounded text-sm font-medium ${
                      auction.statusText === '진행중' ? 'bg-green-100 text-green-800' :
                      auction.statusText === '예정' ? 'bg-blue-100 text-blue-800' :
                      auction.statusText === '종료' ? 'bg-gray-100 text-gray-800' :
                      'bg-red-100 text-red-800'
                    }`}>
                      {auction.statusText || auction.status}
                    </span>
                  </div>
                </div>
              </div>
              <button 
                onClick={() => router.push(`/admin/auctions/${auction.id}`)}
                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition-colors self-center"
              >
                상세보기
              </button>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default AuctionListPage; 