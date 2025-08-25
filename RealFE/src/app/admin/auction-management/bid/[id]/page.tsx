"use client";
import React, { useState, useEffect } from "react";
import { useParams, useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { 
  Users, 
  DollarSign, 
  Calendar, 
  ArrowLeft,
  XCircle,
  Gavel,
  User,
  Clock
} from "lucide-react";
import { adminBidService, adminAuctionService } from '@/service/admin/auctionService';
import { BidResponseDTO, AuctionResponseDTO } from '@/types/admin/auction';

export default function BidDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { id } = params;
  const bidId = Array.isArray(id) ? id[0] : id;

  const [bid, setBid] = useState<BidResponseDTO | null>(null);
  const [auction, setAuction] = useState<AuctionResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (bidId) {
      fetchBidDetails();
    }
  }, [bidId]);

  const fetchBidDetails = async () => {
    try {
      setLoading(true);
      setError(null);

      // 전체 입찰 내역에서 해당 입찰 찾기
      const response = await adminBidService.getAllBids(0, 1000); // 충분한 데이터 로드
      const foundBid = response.content.find(b => b.id.toString() === bidId);

      if (!foundBid) {
        setError('입찰 정보를 찾을 수 없습니다.');
        return;
      }

      setBid(foundBid);

      // 해당 경매 정보 조회
      let auctionResponse: AuctionResponseDTO | null = null;
      try {
        auctionResponse = await adminAuctionService.getAuctionById(foundBid.auctionId);
        setAuction(auctionResponse);
      } catch (auctionErr) {
        console.error('경매 정보 조회 실패:', auctionErr);
      }



    } catch (err: any) {
      console.error('입찰 상세 정보 조회 실패:', err);
      if (err.response?.status === 403) {
        setError('관리자 인증이 필요합니다. 다시 로그인해주세요.');
        if (typeof window !== 'undefined') {
          localStorage.removeItem('adminToken');
          window.location.replace('/admin/login');
        }
      } else {
        setError('입찰 정보를 불러오는데 실패했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  // 관리자 토큰 체크
  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">입찰 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error || !bid) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-32 h-32 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <XCircle className="w-16 h-16 text-red-500" />
          </div>
          <h3 className="text-2xl font-bold text-gray-800 mb-4">오류가 발생했습니다</h3>
          <p className="text-gray-600 text-lg mb-8">{error || '입찰 정보를 찾을 수 없습니다.'}</p>
          <Button onClick={() => router.push('/admin/auction-management/bid')} className="bg-gray-800 hover:bg-gray-700">
            목록으로 돌아가기
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-4xl mx-auto p-6">
        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-4">
              <Button
                onClick={() => router.push('/admin/auction-management/bid')}
                variant="ghost"
                className="p-2"
              >
                <ArrowLeft className="w-5 h-5" />
              </Button>
              <div className="relative">
                <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center shadow-lg">
                  <Users className="w-8 h-8 text-white" />
                </div>
              </div>
          <div>
                <h1 className="text-4xl font-bold text-gray-800">
                  입찰 상세 정보
                </h1>
                <p className="text-gray-600 text-lg mt-2">
                  입찰 ID: {bid.id}
                </p>
              </div>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* 입찰자 정보 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <User className="w-5 h-5" />
                <span>입찰자 정보</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="flex items-center space-x-4">
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center">
                  <User className="w-8 h-8 text-blue-600" />
          </div>
          <div>
                  <h3 className="text-lg font-semibold text-gray-800">
                    {bid.customerName || '입찰자 없음'}
                  </h3>
                  <p className="text-gray-500">고객 ID: {bid.customerId}</p>
                </div>
              </div>
              <div className="space-y-3">
                <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                  <span className="text-gray-600">입찰 가격</span>
                  <span className="text-2xl font-bold text-green-600">
                    {bid.bidPrice ? bid.bidPrice.toLocaleString() : 'N/A'}원
                  </span>
                </div>
                {auction && (
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="text-gray-600">현재가</span>
                    <span className="text-2xl font-bold text-blue-600">
                      {auction.currentPrice ? auction.currentPrice.toLocaleString() : 'N/A'}원
                    </span>
                  </div>
                )}
                <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                  <span className="text-gray-600">입찰 시간</span>
                  <span className="text-sm text-gray-800">
                    {bid.bidTime ? new Date(bid.bidTime).toLocaleString() : 'N/A'}
                  </span>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* 경매 정보 */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center space-x-2">
                <Gavel className="w-5 h-5" />
                <span>경매 정보</span>
              </CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              {auction ? (
                <>
                  <div className="flex items-center space-x-4">
                    <div className="w-16 h-16 bg-gray-200 rounded-lg flex items-center justify-center">
                      <Gavel className="w-8 h-8 text-gray-600" />
          </div>
          <div>
                      <h3 className="text-lg font-semibold text-gray-800">
                        {auction.adminProduct?.productName || '상품명 없음'}
                      </h3>
                    </div>
                  </div>
                  
                  <div className="space-y-3">
                    <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <span className="text-gray-600">시작가</span>
                      <span className="font-semibold">
                        {auction.startPrice ? auction.startPrice.toLocaleString() : 'N/A'}원
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <span className="text-gray-600">현재가</span>
                      <span className="font-semibold text-blue-600">
                        {auction.currentPrice ? auction.currentPrice.toLocaleString() : 'N/A'}원
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <span className="text-gray-600">경매 상태</span>
                      <Badge className="bg-green-500 text-white">
                        {auction.statusText || auction.status}
                      </Badge>
                    </div>
                    
                    <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <span className="text-gray-600">시작 시간</span>
                      <span className="text-sm text-gray-800">
                        {auction.startTime ? new Date(auction.startTime).toLocaleString() : 'N/A'}
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                      <span className="text-gray-600">종료 시간</span>
                      <span className="text-sm text-gray-800">
                        {auction.endTime ? new Date(auction.endTime).toLocaleString() : 'N/A'}
                      </span>
                    </div>
          </div>
                </>
              ) : (
                <div className="text-center py-8 text-gray-500">
                  <Gavel className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                  <p>경매 정보를 불러올 수 없습니다.</p>
          </div>
              )}
            </CardContent>
          </Card>
        </div>

        {/* 액션 버튼 */}
        <div className="mt-8 flex justify-center">
          <Button
            onClick={() => router.push('/admin/auction-management/bid')}
            className="bg-gray-800 hover:bg-gray-700"
          >
            <ArrowLeft className="w-4 h-4 mr-2" />
            목록으로 돌아가기
          </Button>
        </div>
      </div>
    </div>
  );
} 