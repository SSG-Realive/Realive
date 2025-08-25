"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { 
  Users, 
  Search, 
  Filter, 
  Calendar, 
  DollarSign,
  Eye,
  XCircle,
  ArrowLeft,
  ArrowRight,
  Gavel
} from "lucide-react";
import { adminBidService, adminAuctionService } from '@/service/admin/auctionService';
import { BidResponseDTO, AuctionResponseDTO } from '@/types/admin/auction';

type BidFilter = 'ALL' | 'RECENT' | 'HIGH_PRICE' | 'LOW_PRICE';

export default function BidHistoryPage() {
  const router = useRouter();
  const [bids, setBids] = useState<BidResponseDTO[]>([]);
  const [auctions, setAuctions] = useState<AuctionResponseDTO[]>([]);
  const [selectedAuctionId, setSelectedAuctionId] = useState<number | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [search, setSearch] = useState("");
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [bidFilter, setBidFilter] = useState<BidFilter>('ALL');

  useEffect(() => {
    fetchAuctions();
  }, []);

  useEffect(() => {
    if (selectedAuctionId) {
      fetchBids();
    } else {
      fetchAllBids();
    }
  }, [selectedAuctionId, currentPage, bidFilter]);

  const fetchAuctions = async () => {
    try {
      const response = await adminAuctionService.getAuctions({ size: 100 });
      setAuctions(response.content || []);
    } catch (err) {
      console.error('경매 목록 조회 실패:', err);
      // 에러가 발생해도 빈 배열로 설정하여 페이지가 정상 작동하도록 함
      setAuctions([]);
    }
  };

  const fetchBids = async () => {
    if (!selectedAuctionId) return;
    
    try {
      setLoading(true);
      setError(null);
      
      const response = await adminBidService.getBidsByAuction(selectedAuctionId, currentPage, 10);
      setBids(response.content || []);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err: any) {
      console.error('입찰 내역 조회 실패:', err);
      if (err.response?.status === 403) {
        setError('관리자 인증이 필요합니다. 다시 로그인해주세요.');
        if (typeof window !== 'undefined') {
          localStorage.removeItem('adminToken');
          window.location.replace('/admin/login');
        }
      } else {
        setError('입찰 내역을 불러오는데 실패했습니다.');
      }
    } finally {
      setLoading(false);
    }
  };

  const fetchAllBids = async () => {
    try {
      setLoading(true);
      setError(null);
      
      const response = await adminBidService.getAllBids(currentPage, 10);
      setBids(response.content || []);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err: any) {
      console.error('전체 입찰 내역 조회 실패:', err);
      if (err.response?.status === 403) {
        setError('관리자 인증이 필요합니다. 다시 로그인해주세요.');
        if (typeof window !== 'undefined') {
          localStorage.removeItem('adminToken');
          window.location.replace('/admin/login');
        }
      } else {
      setError('입찰 내역을 불러오는데 실패했습니다.');
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

  // 검색어와 필터를 적용한 필터링
  const filteredBids = bids.filter(bid => {
    if (!bid) return false;
    
    // 검색어 필터링
    const matchesSearch = !search || 
      (bid.customerName && bid.customerName.includes(search)) ||
      (bid.auctionId && bid.auctionId.toString().includes(search));
    
    return matchesSearch;
  });

  // 필터에 따른 정렬
  const sortedBids = [...filteredBids].sort((a, b) => {
    switch (bidFilter) {
      case 'RECENT':
        return new Date(b.bidTime).getTime() - new Date(a.bidTime).getTime();
      case 'HIGH_PRICE':
        return b.bidPrice - a.bidPrice;
      case 'LOW_PRICE':
        return a.bidPrice - b.bidPrice;
      default:
        return 0;
    }
  });

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleAuctionChange = (auctionId: number | null) => {
    setSelectedAuctionId(auctionId);
    setCurrentPage(0);
  };

  const handleFilterChange = (filter: BidFilter) => {
    setBidFilter(filter);
    setCurrentPage(0);
  };

  const getAuctionName = (auctionId: number) => {
    const auction = auctions.find(a => a.id === auctionId);
    return auction?.adminProduct?.productName || `경매 #${auctionId}`;
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">입찰 내역을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-32 h-32 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <XCircle className="w-16 h-16 text-red-500" />
          </div>
          <h3 className="text-2xl font-bold text-gray-800 mb-4">오류가 발생했습니다</h3>
          <p className="text-gray-600 text-lg mb-8">{error}</p>
          <Button onClick={selectedAuctionId ? fetchBids : fetchAllBids} className="bg-gray-800 hover:bg-gray-700">
          다시 시도
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto p-6">
        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-4">
              <div className="relative">
                <div className="w-16 h-16 bg-blue-600 rounded-2xl flex items-center justify-center shadow-lg">
                  <Users className="w-8 h-8 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-bold text-gray-800">
                  입찰 내역 관리
                </h1>
                <p className="text-gray-600 text-lg mt-2">
                  경매별 입찰 내역을 확인하고 관리할 수 있습니다.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* 검색 및 필터 섹션 */}
        <Card className="mb-6">
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Search className="w-5 h-5" />
              <span>검색 및 필터</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="flex flex-col lg:flex-row gap-4 items-start lg:items-center justify-between">
              <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
                {/* 경매 선택 드롭다운 */}
                <div className="relative">
                                     <select
                     value={selectedAuctionId || ''}
                     onChange={(e) => handleAuctionChange(e.target.value ? Number(e.target.value) : null)}
                     className="pl-10 pr-8 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                   >
                     <option value="">전체 경매</option>
                     {auctions.length > 0 ? (
                       auctions.map((auction) => (
                         <option key={auction.id} value={auction.id}>
                           {auction.adminProduct?.productName || `경매 #${auction.id}`}
                         </option>
                       ))
                     ) : (
                       <option value="" disabled>경매 목록을 불러올 수 없습니다</option>
                     )}
                   </select>
                  <Gavel className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                </div>

                {/* 검색 입력 */}
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                  <Input
                    type="text"
                    placeholder="입찰자명 또는 경매 ID 검색"
          value={search}
                    onChange={e => setSearch(e.target.value)}
                    className="pl-10 w-64"
        />
      </div>
                
                {/* 필터 버튼들 */}
                <div className="flex gap-2">
                  <Button
                    onClick={() => handleFilterChange('ALL')}
                    variant={bidFilter === 'ALL' ? 'default' : 'outline'}
                    size="sm"
                  >
                    전체
                  </Button>
                  <Button
                    onClick={() => handleFilterChange('RECENT')}
                    variant={bidFilter === 'RECENT' ? 'default' : 'outline'}
                    size="sm"
                  >
                    최신순
                  </Button>
                  <Button
                    onClick={() => handleFilterChange('HIGH_PRICE')}
                    variant={bidFilter === 'HIGH_PRICE' ? 'default' : 'outline'}
                    size="sm"
                  >
                    높은가격순
                  </Button>
                  <Button
                    onClick={() => handleFilterChange('LOW_PRICE')}
                    variant={bidFilter === 'LOW_PRICE' ? 'default' : 'outline'}
                    size="sm"
                  >
                    낮은가격순
                  </Button>
                </div>
              </div>
              
              <div className="text-sm text-gray-600 bg-gray-100 px-3 py-2 rounded-lg">
                총 {totalElements}개 중 {currentPage * 10 + 1}-{Math.min((currentPage + 1) * 10, totalElements)}개
              </div>
            </div>
          </CardContent>
        </Card>
        
        {/* 입찰 내역 테이블 */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center space-x-2">
              <Users className="w-5 h-5" />
              <span>입찰 내역</span>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="overflow-x-auto">
              <table className="w-full">
        <thead>
                  <tr className="bg-gray-50 border-b">
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">경매명</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">입찰자</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">입찰금액</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">입찰시간</th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
          </tr>
        </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {sortedBids.length === 0 ? (
                    <tr>
                      <td colSpan={5} className="px-6 py-12 text-center">
                        <Users className="w-12 h-12 mx-auto mb-4 text-gray-300" />
                        <p className="text-gray-500">
                          {search || selectedAuctionId ? '검색 결과가 없습니다.' : '입찰 내역이 없습니다.'}
                        </p>
                      </td>
                    </tr>
                  ) : (
                    sortedBids.map((bid) => (
            <tr key={bid.id}>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900">
                            {getAuctionName(bid.auctionId)}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-sm font-medium text-gray-900">
                            {bid.customerName || '입찰자 없음'}
                          </div>
                          <div className="text-sm text-gray-500">
                            ID: {bid.customerId}
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap">
                          <div className="text-lg font-bold text-green-600">
                            {bid.bidPrice ? bid.bidPrice.toLocaleString() : 'N/A'}원
                          </div>
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                          {bid.bidTime ? new Date(bid.bidTime).toLocaleString() : 'N/A'}
                        </td>
                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                          <Button
                  onClick={() => router.push(`/admin/auction-management/bid/${bid.id}`)}
                            variant="ghost"
                            size="sm"
                            className="text-blue-600 hover:text-blue-800"
                >
                            <Eye className="w-4 h-4 mr-1" />
                  상세보기
                          </Button>
              </td>
            </tr>
                    ))
                  )}
        </tbody>
      </table>
            </div>
          </CardContent>
        </Card>

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <Card className="mt-8">
            <CardContent className="flex justify-center py-6">
              <div className="flex space-x-2">
                <Button
                  onClick={() => handlePageChange(currentPage - 1)}
                  disabled={currentPage === 0}
                  variant="outline"
                  size="sm"
                >
                  <ArrowLeft className="w-4 h-4 mr-1" />
                  이전
                </Button>
                
                {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                  const pageNum = Math.max(0, Math.min(totalPages - 5, currentPage - 2)) + i;
                  return (
                    <Button
                      key={pageNum}
                      onClick={() => handlePageChange(pageNum)}
                      variant={currentPage === pageNum ? 'default' : 'outline'}
                      size="sm"
                    >
                      {pageNum + 1}
                    </Button>
                  );
                })}
                
                <Button
                  onClick={() => handlePageChange(currentPage + 1)}
                  disabled={currentPage === totalPages - 1}
                  variant="outline"
                  size="sm"
                >
                  다음
                  <ArrowRight className="w-4 h-4 ml-1" />
                </Button>
        </div>
            </CardContent>
          </Card>
      )}
      </div>
    </div>
  );
} 