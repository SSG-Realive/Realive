"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { 
  Gavel, 
  TrendingUp, 
  Users, 
  DollarSign, 
  Clock, 
  CheckCircle, 
  XCircle, 
  Eye,
  Search,
  Filter,
  Calendar,
  Package
} from "lucide-react";
import { adminAuctionService, adminBidService } from '@/service/admin/auctionService';
import { AuctionResponseDTO, BidResponseDTO } from '@/types/admin/auction';

interface AuctionStats {
  totalAuctions: number;
  activeAuctions: number;
  completedAuctions: number;
  totalBids: number;
  totalRevenue: number;
}

export default function AuctionManagementPage() {
  const router = useRouter();
  const [auctions, setAuctions] = useState<AuctionResponseDTO[]>([]);
  const [bids, setBids] = useState<BidResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [auctionSearch, setAuctionSearch] = useState("");
  const [bidSearch, setBidSearch] = useState("");
  const [stats, setStats] = useState<AuctionStats>({
    totalAuctions: 0,
    activeAuctions: 0,
    completedAuctions: 0,
    totalBids: 0,
    totalRevenue: 0
  });
  const [auctionMap, setAuctionMap] = useState<Map<number, any>>(new Map());

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    try {
      setLoading(true);
      const [auctionsData, bidsData] = await Promise.all([
        adminAuctionService.getAuctions({ size: 100 }), // 더 많은 데이터 로드
        adminBidService.getAllBids(0, 20) // 전체 입찰 내역 (최신 20개)
      ]);
      
      const auctionsList = auctionsData.content || [];
      const bidsList = bidsData.content || [];
      
      // 경매 정보를 맵으로 만들어서 입찰에서 경매명을 찾을 수 있도록 함
      const newAuctionMap = new Map();
      auctionsList.forEach(auction => {
        newAuctionMap.set(auction.id, auction);
      });
      setAuctionMap(newAuctionMap);
      
      console.log('경매 데이터:', auctionsList);
      console.log('입찰 데이터:', bidsList);
      console.log('첫 번째 입찰 상세:', bidsList[0]);
      
      setAuctions(auctionsList);
      setBids(bidsList);
      
      // 통계 계산
      const now = new Date();
      const activeAuctions = auctionsList.filter(a => 
        a.status === 'PROCEEDING' && new Date(a.startTime) <= now
      ).length;
      const completedAuctions = auctionsList.filter(a => a.status === 'COMPLETED').length;
      const totalRevenue = bidsList.reduce((sum, bid) => sum + (bid.bidPrice || 0), 0);
      
      setStats({
        totalAuctions: auctionsList.length,
        activeAuctions,
        completedAuctions,
        totalBids: bidsList.length,
        totalRevenue
      });
      
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
    (a.adminProduct?.productName?.includes(auctionSearch) || false) || 
    (a.status?.includes(auctionSearch) || false)
  );
  
  const filteredBids = bids.filter(b => 
    (b.customerName?.includes(bidSearch) || false)
  );

  const getStatusColor = (status: string, startTime?: string) => {
    // 시작 시간이 현재보다 뒤면 예정
    if (startTime && new Date(startTime) > new Date()) {
      return 'bg-yellow-500 text-white';
    }
    
    switch (status) {
      case 'PROCEEDING': return 'bg-green-500 text-white';
      case 'COMPLETED': return 'bg-blue-500 text-white';
      case 'CANCELLED': return 'bg-red-500 text-white';
      case 'FAILED': return 'bg-gray-500 text-white';
      default: return 'bg-gray-500 text-white';
    }
  };

  const getStatusText = (status: string, startTime?: string) => {
    // 시작 시간이 현재보다 뒤면 예정
    if (startTime && new Date(startTime) > new Date()) {
      return '예정';
    }
    
    switch (status) {
      case 'PROCEEDING': return '진행중';
      case 'COMPLETED': return '완료';
      case 'CANCELLED': return '취소됨';
      case 'FAILED': return '실패';
      default: return status;
    }
  };

  if (loading) {
    return (
      <div className="h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">경매 데이터를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-32 h-32 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <XCircle className="w-16 h-16 text-red-500" />
          </div>
          <h3 className="text-2xl font-bold text-gray-800 mb-4">오류가 발생했습니다</h3>
          <p className="text-gray-600 text-lg mb-8">{error}</p>
          <Button onClick={fetchData} className="bg-gray-800 hover:bg-gray-700">
            다시 시도
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-50">
      <div className="max-w-7xl mx-auto p-6">
        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-4">
              <div className="relative">
                <div className="w-16 h-16 bg-gray-800 rounded-2xl flex items-center justify-center shadow-lg">
                  <Gavel className="w-8 h-8 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-bold text-gray-800">
                  경매 관리 대시보드
                </h1>
                <p className="text-gray-600 text-lg mt-2">
                  경매 현황과 입찰 내역을 한눈에 확인할 수 있습니다.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* 통계 카드 섹션 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          <Card className="bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-all duration-200">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-1">총 경매 수</p>
                  <p className="text-3xl font-bold text-gray-900">{stats.totalAuctions}</p>
                </div>
                <div className="p-3 rounded-xl bg-blue-100">
                  <Gavel className="w-6 h-6 text-blue-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-all duration-200">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-1">진행중 경매</p>
                  <p className="text-3xl font-bold text-gray-900">{stats.activeAuctions}</p>
                </div>
                <div className="p-3 rounded-xl bg-green-100">
                  <TrendingUp className="w-6 h-6 text-green-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-all duration-200">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-1">완료된 경매</p>
                  <p className="text-3xl font-bold text-gray-900">{stats.completedAuctions}</p>
                </div>
                <div className="p-3 rounded-xl bg-purple-100">
                  <CheckCircle className="w-6 h-6 text-purple-600" />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card className="bg-white rounded-xl shadow-sm border border-gray-200 hover:shadow-md transition-all duration-200">
            <CardContent className="p-6">
              <div className="flex items-center justify-between">
                <div>
                  <p className="text-sm font-medium text-gray-600 mb-1">총 입찰 수</p>
                  <p className="text-3xl font-bold text-gray-900">{stats.totalBids}</p>
                </div>
                <div className="p-3 rounded-xl bg-orange-100">
                  <Users className="w-6 h-6 text-orange-600" />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        {/* 메인 콘텐츠 섹션 */}
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
          {/* 경매 현황 */}
          <Card className="bg-white rounded-xl shadow-sm border border-gray-200">
            <CardHeader className="pb-4">
              <div className="flex items-center justify-between">
                <CardTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
                  <Gavel className="w-5 h-5" />
                  최근 경매 현황
                </CardTitle>
                <div className="flex items-center gap-2">
                  <div className="relative">
                    <Search className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                    <Input
                      type="text"
                      placeholder="경매 검색..."
                      value={auctionSearch}
                      onChange={(e) => setAuctionSearch(e.target.value)}
                      className="pl-10 w-48"
                    />
                  </div>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-96">
                <div className="space-y-4">
                  {filteredAuctions.slice(0, 20).map((auction) => (
                    <div key={auction.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors">
                      <div className="flex items-center space-x-4">
                        <div className="w-12 h-12 bg-gray-200 rounded-lg flex items-center justify-center">
                          <Package className="w-6 h-6 text-gray-600" />
                        </div>
                        <div>
                          <h4 className="font-semibold text-gray-800 truncate max-w-48">
                            {auction.adminProduct?.productName || '상품명 없음'}
                          </h4>
                          <div className="flex items-center gap-2 mt-1">
                            <Badge className={getStatusColor(auction.status, auction.startTime)}>
                              {getStatusText(auction.status, auction.startTime)}
                            </Badge>
                            <span className="text-sm text-gray-500">
                              {auction.startPrice ? auction.startPrice.toLocaleString() : 'N/A'}원
                            </span>
                          </div>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="text-sm text-gray-500">
                          {auction.startTime ? new Date(auction.startTime).toLocaleDateString() : 'N/A'}
                        </div>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => router.push(`/admin/auction-management/list/${auction.id}`)}
                          className="text-blue-600 hover:text-blue-800"
                        >
                          상세보기
                        </Button>
                      </div>
                    </div>
                  ))}
                  {filteredAuctions.length === 0 && (
                    <div className="text-center py-8 text-gray-500">
                      <Gavel className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                      <p>등록된 경매가 없습니다.</p>
                    </div>
                  )}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>

          {/* 입찰 내역 */}
          <Card className="bg-white rounded-xl shadow-sm border border-gray-200">
            <CardHeader className="pb-4">
              <div className="flex items-center justify-between">
                <CardTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
                  <Users className="w-5 h-5" />
                  최근 입찰 내역
                </CardTitle>
                <div className="flex items-center gap-2">
                  <div className="relative">
                    <Search className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                    <Input
                      type="text"
                      placeholder="입찰 검색..."
                      value={bidSearch}
                      onChange={(e) => setBidSearch(e.target.value)}
                      className="pl-10 w-48"
                    />
                  </div>
                </div>
              </div>
            </CardHeader>
            <CardContent>
              <ScrollArea className="h-96">
                <div className="space-y-4">
                  {filteredBids.slice(0, 20).map((bid) => (
                    <div key={bid.id} className="flex items-center justify-between p-4 bg-gray-50 rounded-xl hover:bg-gray-100 transition-colors">
                      <div className="flex items-center space-x-4">
                        <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                          <DollarSign className="w-6 h-6 text-blue-600" />
                        </div>
                        <div>
                          <h4 className="font-semibold text-gray-800 truncate max-w-48">
                            {auctionMap.get(bid.auctionId)?.adminProduct?.productName || `경매 #${bid.auctionId}`}
                          </h4>
                          <p className="text-sm text-gray-600 mt-1">
                            {bid.customerName || '입찰자 없음'}
                          </p>
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-bold text-lg text-green-600">
                          {bid.bidPrice ? bid.bidPrice.toLocaleString() : 'N/A'}원
                        </div>
                        <div className="text-sm text-gray-500">
                          {bid.bidTime ? new Date(bid.bidTime).toLocaleDateString() : 'N/A'}
                        </div>
                      </div>
                    </div>
                  ))}
                  {filteredBids.length === 0 && (
                    <div className="text-center py-8 text-gray-500">
                      <Users className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                      <p>입찰 내역이 없습니다.</p>
                    </div>
                  )}
                </div>
              </ScrollArea>
            </CardContent>
          </Card>
        </div>

        {/* 빠른 액션 섹션 */}
        <div className="mt-8">
          <Card className="bg-white rounded-xl shadow-sm border border-gray-200">
            <CardHeader>
              <CardTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
                <Clock className="w-5 h-5" />
                바로가기
              </CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <Button 
                  onClick={() => router.push('/admin/auction-management/list')}
                  className="h-16 bg-gray-800 hover:bg-gray-700 text-white"
                >
                  <div className="text-center">
                    <Gavel className="w-6 h-6 mx-auto mb-2" />
                    <div className="text-sm font-medium">전체 경매 관리</div>
                  </div>
                </Button>
                <Button 
                  onClick={() => router.push('/admin/auction-management/bid')}
                  className="h-16 bg-blue-600 hover:bg-blue-700 text-white"
                >
                  <div className="text-center">
                    <Users className="w-6 h-6 mx-auto mb-2" />
                    <div className="text-sm font-medium">입찰 내역 관리</div>
                  </div>
                </Button>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
} 