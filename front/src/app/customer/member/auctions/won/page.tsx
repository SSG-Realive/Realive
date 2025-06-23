'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';

interface AuctionWin {
  auctionId: number;
  productName: string;
  productImageUrl: string | null;
  winningBidPrice: number;
  auctionEndTime: string;
  paymentDeadline: string;
  isPaid: boolean;
  paymentStatus: string;
  isNewWin: boolean;
  winMessage: string;
}

interface WonAuctionsResponse {
  content: AuctionWin[];
  totalPages: number;
  number: number;
  last: boolean;
}

export default function WonAuctionsPage() {
  const [wonAuctions, setWonAuctions] = useState<AuctionWin[]>([]);
  const [loading, setLoading] = useState(true);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const router = useRouter();

  useEffect(() => {
    fetchWonAuctions();
  }, [currentPage]);

  const fetchWonAuctions = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/customer/auction-wins?page=${currentPage}&size=10`, {
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('낙찰한 경매 목록을 불러오는데 실패했습니다.');
      }

      const data: WonAuctionsResponse = await response.json();
      setWonAuctions(data.content);
      setTotalPages(data.totalPages);
    } catch (error) {
      console.error('낙찰한 경매 목록 조회 오류:', error);
      alert('낙찰한 경매 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatPrice = (price: number) => {
    return price.toLocaleString() + '원';
  };

  const getPaymentStatusColor = (isPaid: boolean) => {
    return isPaid ? 'text-green-600' : 'text-red-600';
  };

  const getPaymentStatusText = (isPaid: boolean) => {
    return isPaid ? '결제완료' : '결제대기';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 p-8">
        <div className="max-w-6xl mx-auto">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">낙찰한 경매 목록을 불러오는 중...</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-6xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">낙찰한 경매</h1>
          <p className="text-gray-600">성공적으로 낙찰한 경매 상품들을 확인하세요.</p>
        </div>

        {/* 낙찰 알림 */}
        {wonAuctions.some(auction => auction.isNewWin) && (
          <div className="mb-6 p-4 bg-blue-50 border border-blue-200 rounded-lg">
            <div className="flex items-center">
              <div className="text-blue-600 text-2xl mr-3">🎉</div>
              <div>
                <h3 className="text-lg font-semibold text-blue-900 mb-1">새로운 낙찰!</h3>
                <p className="text-blue-800">
                  {wonAuctions.filter(auction => auction.isNewWin).length}개의 새로운 낙찰이 있습니다. 
                  결제를 완료해주세요!
                </p>
              </div>
            </div>
          </div>
        )}

        {wonAuctions.length === 0 ? (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">🏆</div>
            <h3 className="text-xl font-semibold text-gray-600 mb-2">아직 낙찰한 경매가 없습니다</h3>
            <p className="text-gray-500 mb-6">경매에 참여하여 상품을 낙찰해보세요!</p>
            <Link
              href="/main/auctions"
              className="inline-block bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors"
            >
              경매 보러가기
            </Link>
          </div>
        ) : (
          <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
            {wonAuctions.map((auction) => (
              <div key={auction.auctionId} className="bg-white rounded-lg shadow-md overflow-hidden">
                <div className="aspect-w-16 aspect-h-9 bg-gray-200">
                  {auction.productImageUrl ? (
                    <img
                      src={auction.productImageUrl}
                      alt={auction.productName}
                      className="w-full h-48 object-cover"
                    />
                  ) : (
                    <div className="w-full h-48 flex items-center justify-center text-gray-400">
                      이미지 없음
                    </div>
                  )}
                </div>
                
                <div className="p-6">
                  <h3 className="text-lg font-semibold text-gray-900 mb-2 line-clamp-2">
                    {auction.productName}
                  </h3>
                  
                  {/* 낙찰 알림 표시 */}
                  {auction.isNewWin && (
                    <div className="mb-3 p-2 bg-green-50 border border-green-200 rounded-md">
                      <p className="text-sm text-green-800 font-medium">
                        {auction.winMessage}
                      </p>
                    </div>
                  )}
                  
                  <div className="space-y-3">
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">낙찰가</span>
                      <span className="text-lg font-bold text-blue-600">
                        {formatPrice(auction.winningBidPrice)}
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">경매 종료</span>
                      <span className="text-sm text-gray-900">
                        {formatDate(auction.auctionEndTime)}
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">결제 마감</span>
                      <span className="text-sm text-gray-900">
                        {formatDate(auction.paymentDeadline)}
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">결제 상태</span>
                      <span className={`text-sm font-semibold ${getPaymentStatusColor(auction.isPaid)}`}>
                        {getPaymentStatusText(auction.isPaid)}
                      </span>
                    </div>
                  </div>
                  
                  <div className="mt-6">
                    {auction.isPaid ? (
                      <div className="text-center">
                        <span className="inline-block bg-green-100 text-green-800 px-3 py-1 rounded-full text-sm">
                          결제 완료
                        </span>
                      </div>
                    ) : (
                      <button
                        onClick={() => router.push(`/customer/member/auctions/won/${auction.auctionId}/payment`)}
                        className="w-full bg-blue-600 text-white py-2 px-4 rounded-lg hover:bg-blue-700 transition-colors"
                      >
                        결제하기
                      </button>
                    )}
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {totalPages > 1 && (
          <div className="mt-8 flex justify-center">
            <nav className="flex space-x-2">
              <button
                onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
                disabled={currentPage === 0}
                className="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                이전
              </button>
              
              {Array.from({ length: totalPages }, (_, i) => (
                <button
                  key={i}
                  onClick={() => setCurrentPage(i)}
                  className={`px-3 py-2 text-sm font-medium rounded-md ${
                    currentPage === i
                      ? 'bg-blue-600 text-white'
                      : 'text-gray-500 bg-white border border-gray-300 hover:bg-gray-50'
                  }`}
                >
                  {i + 1}
                </button>
              ))}
              
              <button
                onClick={() => setCurrentPage(Math.min(totalPages - 1, currentPage + 1))}
                disabled={currentPage === totalPages - 1}
                className="px-3 py-2 text-sm font-medium text-gray-500 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                다음
              </button>
            </nav>
          </div>
        )}
      </div>
    </div>
  );
} 