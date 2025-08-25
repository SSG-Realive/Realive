"use client";
import React, { useState, useEffect } from "react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { ScrollArea } from "@/components/ui/scroll-area";
import { 
  Gavel, 
  Search, 
  Filter, 
  Calendar, 
  Package,
  Eye,
  XCircle,
  ArrowLeft,
  ArrowRight
} from "lucide-react";
import Link from "next/link";
import { useRouter } from 'next/navigation';
import { adminAuctionService } from '@/service/admin/auctionService';
import { AuctionResponseDTO } from '@/types/admin/auction';

type StatusFilter = 'ALL' | 'PROCEEDING' | 'SCHEDULED' | 'COMPLETED' | 'FAILED' | 'CANCELLED';

function AuctionListPage() {
  const [search, setSearch] = useState("");
  const [selected, setSelected] = useState<AuctionResponseDTO | null>(null);
  const [auctions, setAuctions] = useState<AuctionResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [statusFilter, setStatusFilter] = useState<StatusFilter>('ALL');
  const router = useRouter();

  useEffect(() => {
    fetchAuctions();
  }, [currentPage, statusFilter]);

  const fetchAuctions = async () => {
    try {
      setLoading(true);
      setError(null);
      
      // 상태 필터에 따른 API 파라미터 설정
      let statusParam = undefined;
      if (statusFilter === 'PROCEEDING') {
        statusParam = 'PROCEEDING';
      } else if (statusFilter === 'SCHEDULED') {
        statusParam = 'SCHEDULED';
      } else if (statusFilter === 'COMPLETED') {
        statusParam = 'COMPLETED';
      } else if (statusFilter === 'FAILED') {
        statusParam = 'FAILED';
      } else if (statusFilter === 'CANCELLED') {
        statusParam = 'CANCELLED';
      }
      
      const response = await adminAuctionService.getAuctions({
        page: currentPage,
        size: 10,
        status: statusParam
      });
      setAuctions(response.content || []);
      setTotalPages(response.totalPages);
      setTotalElements(response.totalElements);
    } catch (err: any) {
      console.error('경매 목록 조회 실패:', err);
      if (err.response?.status === 403) {
        setError('관리자 인증이 필요합니다. 다시 로그인해주세요.');
        if (typeof window !== 'undefined') {
          localStorage.removeItem('adminToken');
          window.location.replace('/admin/login');
        }
      } else {
        setError('경매 목록을 불러오는데 실패했습니다.');
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

  // 검색어와 상태 필터를 모두 적용한 필터링
  const filtered = auctions.filter(a => {
    if (!a) return false;
    
    // 검색어 필터링
    const matchesSearch = !search || 
      (a.adminProduct?.productName && a.adminProduct.productName.includes(search)) ||
      (a.statusText && a.statusText.includes(search));
    
    // 상태 필터링 - 백엔드 statusText 값에 맞게 수정
    let matchesStatus = true;
    if (statusFilter === 'PROCEEDING') {
      matchesStatus = a.statusText === '진행중';
    } else if (statusFilter === 'SCHEDULED') {
      matchesStatus = a.statusText === '예정';
    } else if (statusFilter === 'COMPLETED') {
      matchesStatus = a.statusText === '종료';
    } else if (statusFilter === 'FAILED') {
      matchesStatus = a.statusText === '실패';
    } else if (statusFilter === 'CANCELLED') {
      matchesStatus = a.statusText === '취소';
    }
    
    return matchesSearch && matchesStatus;
  });

  // 이미지 URL 생성 함수
  const getImageUrl = (imagePath: string): string => {
    if (!imagePath) return '/images/placeholder.png';
    if (imagePath.startsWith('http')) return imagePath;
    return `https://www.realive-ssg.click/api${imagePath}`;
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleStatusFilterChange = (filter: StatusFilter) => {
    setStatusFilter(filter);
    setCurrentPage(0); // 필터 변경 시 첫 페이지로 이동
  };

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
      case 'COMPLETED': return '종료';
      case 'CANCELLED': return '취소';
      case 'FAILED': return '실패';
      default: return status;
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">경매 목록을 불러오는 중...</p>
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
          <Button onClick={fetchAuctions} className="bg-gray-800 hover:bg-gray-700">
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
                <div className="w-16 h-16 bg-gray-800 rounded-2xl flex items-center justify-center shadow-lg">
                  <Gavel className="w-8 h-8 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-bold text-gray-800">
                  경매 목록 관리
                </h1>
                <p className="text-gray-600 text-lg mt-2">
                  등록된 모든 경매를 확인하고 관리할 수 있습니다.
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
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-4 h-4" />
                  <Input
                    type="text"
                    placeholder="상품명 또는 상태로 검색"
                    value={search}
                    onChange={e => setSearch(e.target.value)}
                    className="pl-10 w-64"
                  />
                </div>
                
                {/* 상태 필터 버튼들 */}
                <div className="flex gap-2">
                  <Button
                    onClick={() => handleStatusFilterChange('ALL')}
                    variant={statusFilter === 'ALL' ? 'default' : 'outline'}
                    size="sm"
                  >
                    전체
                  </Button>
                  <Button
                    onClick={() => handleStatusFilterChange('PROCEEDING')}
                    variant={statusFilter === 'PROCEEDING' ? 'default' : 'outline'}
                    size="sm"
                  >
                    진행중
                  </Button>
                  <Button
                    onClick={() => handleStatusFilterChange('SCHEDULED')}
                    variant={statusFilter === 'SCHEDULED' ? 'default' : 'outline'}
                    size="sm"
                  >
                    예정
                  </Button>
                  <Button
                    onClick={() => handleStatusFilterChange('COMPLETED')}
                    variant={statusFilter === 'COMPLETED' ? 'default' : 'outline'}
                    size="sm"
                  >
                    종료
                  </Button>
                  <Button
                    onClick={() => handleStatusFilterChange('FAILED')}
                    variant={statusFilter === 'FAILED' ? 'default' : 'outline'}
                    size="sm"
                  >
                    실패
                  </Button>
                  <Button
                    onClick={() => handleStatusFilterChange('CANCELLED')}
                    variant={statusFilter === 'CANCELLED' ? 'default' : 'outline'}
                    size="sm"
                  >
                    취소
                  </Button>
                </div>
              </div>
              
              <div className="text-sm text-gray-600 bg-gray-100 px-3 py-2 rounded-lg">
                총 {totalElements}개 중 {currentPage * 10 + 1}-{Math.min((currentPage + 1) * 10, totalElements)}개
              </div>
            </div>
          </CardContent>
        </Card>
        
        {/* 경매 목록 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filtered.length === 0 ? (
            <div className="col-span-full">
              <Card>
                <CardContent className="flex flex-col items-center justify-center py-12">
                  <Package className="w-16 h-16 text-gray-400 mb-4" />
                  <h3 className="text-xl font-semibold text-gray-600 mb-2">
                    {search || statusFilter !== 'ALL' ? '검색 결과가 없습니다.' : '등록된 경매가 없습니다.'}
                  </h3>
                  <p className="text-gray-500 text-center">
                    {search || statusFilter !== 'ALL' 
                      ? '다른 검색어나 필터를 시도해보세요.' 
                      : '새로운 경매를 등록해보세요.'}
                  </p>
                </CardContent>
              </Card>
            </div>
          ) : (
            filtered.map(a => (
              <Card key={a.id} className="hover:shadow-lg transition-all duration-200 hover:-translate-y-1">
                <CardContent className="p-6">
                  <div className="flex items-start space-x-4">
                    <div className="flex-shrink-0">
                      {a.adminProduct?.imageThumbnailUrl ? (
                        <img 
                          src={getImageUrl(a.adminProduct.imageThumbnailUrl)} 
                          alt="auction" 
                          className="w-20 h-20 object-cover rounded-lg shadow-sm"
                          onError={(e) => {
                            e.currentTarget.src = '/images/placeholder.png';
                          }}
                        />
                      ) : (
                        <div className="w-20 h-20 bg-gray-200 flex items-center justify-center rounded-lg text-xs text-gray-500">
                          이미지 없음
                        </div>
                      )}
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="font-semibold text-lg truncate text-gray-800 mb-2">
                        {a.adminProduct?.productName}
                      </h3>
                      <div className="space-y-2 text-sm text-gray-600">
                        <div className="flex justify-between">
                          <span>시작가:</span>
                          <span className="font-medium">{a.startPrice ? a.startPrice.toLocaleString() : 'N/A'}원</span>
                        </div>
                        <div className="flex justify-between">
                          <span>현재가:</span>
                          <span className="font-medium text-blue-600">{a.currentPrice ? a.currentPrice.toLocaleString() : 'N/A'}원</span>
                        </div>
                        <div className="flex justify-between">
                          <span>시작시간:</span>
                          <span className="text-xs">{new Date(a.startTime).toLocaleString()}</span>
                        </div>
                        <div className="flex justify-between">
                          <span>종료시간:</span>
                          <span className="text-xs">{new Date(a.endTime).toLocaleString()}</span>
                        </div>
                        <div className="flex justify-between items-center">
                          <span>상태:</span>
                          <Badge className={getStatusColor(a.status, a.startTime)}>
                            {getStatusText(a.status, a.startTime)}
                          </Badge>
                        </div>
                      </div>
                      <div className="mt-4">
                        <Link 
                          href={`/admin/auction-management/list/${a.id}`} 
                          className="inline-flex items-center space-x-1 text-blue-600 hover:text-blue-800 text-sm font-medium transition-colors"
                        >
                          <Eye className="w-4 h-4" />
                          <span>상세보기</span>
                        </Link>
                      </div>
                    </div>
                  </div>
                </CardContent>
              </Card>
            ))
          )}
        </div>

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

export default AuctionListPage; 