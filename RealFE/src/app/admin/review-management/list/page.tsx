"use client";
import React, { useState, useEffect, useCallback } from "react";
import { useRouter } from "next/navigation";
import { getAdminReviewList, updateAdminReview } from "@/service/admin/reviewService";
import { AdminReview, AdminReviewListRequest, AdminReviewListResponse, getTrafficLightEmoji, getTrafficLightText, getTrafficLightBgClass } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";
import { useGlobalDialog } from "@/app/context/dialogContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { ScrollArea } from "@/components/ui/scroll-area";
import { 
  Search, 
  Filter, 
  Eye, 
  EyeOff, 
  Star, 
  Calendar, 
  User, 
  Store, 
  MessageSquare,
  AlertTriangle,
  ChevronLeft,
  ChevronRight,
  RefreshCw
} from "lucide-react";

export default function ReviewListPage() {
  const router = useRouter();
  const { accessToken } = useAdminAuthStore();
  const [reviews, setReviews] = useState<AdminReview[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [sortOption, setSortOption] = useState('createdAt,desc');
  const [filters, setFilters] = useState({
    productFilter: '',
    sellerFilter: '',
  });
  const [searchTimeout, setSearchTimeout] = useState<NodeJS.Timeout | null>(null);
  const {show} = useGlobalDialog();
  const [totalElements, setTotalElements] = useState(0);

  const handleFilterChange = (key: string, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  // 디바운싱된 검색 함수
  const debouncedSearch = useCallback((page: number = 1) => {
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }
    
    const timeout = setTimeout(() => {
      fetchReviews(page);
    }, 500); // 500ms 지연
    
    setSearchTimeout(timeout);
  }, [searchTimeout]);

  // 즉시 검색 함수 (버튼 클릭 시)
  const applyFilters = () => {
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }
    fetchReviews(1);
  };

  const fetchReviews = async (page: number = 1) => {
    try {
      setLoading(true);
      setError(null);
      
      const params: AdminReviewListRequest = {
        page: page - 1,
        size: 10,
        sort: sortOption,
        productFilter: filters.productFilter || undefined,
        sellerFilter: filters.sellerFilter || undefined,
      };

      console.log('리뷰 목록 조회 요청:', params);
      const response = await getAdminReviewList(params);
      console.log('리뷰 목록 조회 응답:', response);
      setReviews(response.content);
      setTotalPages(response.totalPages);
      setCurrentPage(page);
      setTotalElements(response.totalElements ?? 0);
    } catch (err: any) {
      console.error('리뷰 목록 조회 실패:', err);
      console.error('에러 상세 정보:', {
        message: err.message,
        status: err.response?.status,
        statusText: err.response?.statusText,
        data: err.response?.data,
        headers: err.response?.headers
      });
      setError(err.message || '리뷰 목록을 불러오는데 실패했습니다.');
      if (err.response?.status === 403) {
        router.replace('/admin/login');
      }
    } finally {
      setLoading(false);
    }
  };

  // 필터 변경 시 자동 검색
  useEffect(() => {
    if (accessToken) {
      debouncedSearch(1);
    }
  }, [filters, sortOption, accessToken]);

  // 정렬 옵션 변경 시 즉시 검색 (디바운싱 없이)
  const handleSortChange = (value: string) => {
    setSortOption(value);
    // 정렬 변경 시 즉시 검색 실행
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }
    // 즉시 검색 실행
    setTimeout(() => {
      fetchReviews(1);
    }, 0);
  };

  // 페이지 변경 시 검색
  useEffect(() => {
    if (accessToken && currentPage > 1) {
      fetchReviews(currentPage);
    }
  }, [currentPage, accessToken]);

  useEffect(() => {
    if (!accessToken) {
      router.replace('/admin/login');
    }
  }, [accessToken, router]);

  // 컴포넌트 언마운트 시 타이머 정리
  useEffect(() => {
    return () => {
      if (searchTimeout) {
        clearTimeout(searchTimeout);
      }
    };
  }, [searchTimeout]);

  const handleToggleVisibility = async (reviewId: number, isHidden: boolean) => {
    try {
      await updateAdminReview(reviewId, !isHidden);
      // Refresh list
      setReviews(reviews.map(r => r.reviewId === reviewId ? { ...r, isHidden: !isHidden } : r));
    } catch (err: any) {
      console.error('리뷰 상태 변경 실패:', err);
      show(err.message || '리뷰 상태 변경에 실패했습니다.');
    }
  };

  // 신호등 색상 반환 함수
  const getTrafficLightColor = (rating: number): string => {
    if (rating <= 2) return '#ef4444'; // 빨강 (1-2점: 부정적)
    if (rating === 3) return '#facc15'; // 노랑 (3점: 보통)
    return '#22c55e'; // 초록 (4-5점: 긍정적)
  };

  // 통계 계산 (전체 기준)
  const totalCount = totalElements ?? 0;
  const hiddenCount = reviews.filter(r => r.isHidden).length;
  const visibleCount = totalCount - hiddenCount;

  if (loading && !reviews.length) {
    return (
      <div className="h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">리뷰 데이터를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-32 h-32 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <AlertTriangle className="w-16 h-16 text-red-500" />
          </div>
          <h3 className="text-2xl font-bold text-gray-800 mb-4">오류가 발생했습니다</h3>
          <p className="text-gray-600 text-lg mb-8">{error}</p>
          <Button onClick={() => fetchReviews(1)} className="bg-gray-800 hover:bg-gray-700">
            다시 시도
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-50 min-h-screen">
      <div className="max-w-7xl mx-auto p-6">
        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-4">
              <div className="relative">
                <div className="w-16 h-16 bg-gray-800 rounded-2xl flex items-center justify-center shadow-lg">
                  <MessageSquare className="w-8 h-8 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-bold text-gray-800">리뷰 관리</h1>
                <p className="text-gray-600 text-lg mt-2">고객 리뷰를 관리하고 모니터링할 수 있습니다.</p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <Button onClick={() => fetchReviews(currentPage)} variant="outline" className="flex items-center space-x-2">
                <RefreshCw className="w-4 h-4" />
                <span>새로고침</span>
              </Button>
            </div>
          </div>
        </div>

        {/* 통계 카드 */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
          <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
            <CardContent className="flex flex-col items-center py-6">
              <div className="text-3xl font-bold text-purple-700">{totalCount}</div>
              <div className="text-sm text-gray-500 mt-1">총 리뷰</div>
            </CardContent>
          </Card>
          <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
            <CardContent className="flex flex-col items-center py-6">
              <div className="text-3xl font-bold text-green-700">{visibleCount}</div>
              <div className="text-sm text-gray-500 mt-1">공개</div>
            </CardContent>
          </Card>
          <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
            <CardContent className="flex flex-col items-center py-6">
              <div className="text-3xl font-bold text-red-700">{hiddenCount}</div>
              <div className="text-sm text-gray-500 mt-1">숨김</div>
            </CardContent>
          </Card>
        </div>

        {/* 검색/필터 Card */}
        <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none mb-6">
          <CardHeader>
            <CardTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
              <Filter className="w-5 h-5" />
              검색 및 필터
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="relative">
                <Search className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <Input
                  type="text"
                  placeholder="상품명으로 검색..."
                  value={filters.productFilter}
                  onChange={(e) => handleFilterChange('productFilter', e.target.value)}
                  className="pl-10"
                />
              </div>
              <div className="relative">
                <Store className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <Input
                  type="text"
                  placeholder="판매자명으로 검색..."
                  value={filters.sellerFilter}
                  onChange={(e) => handleFilterChange('sellerFilter', e.target.value)}
                  className="pl-10"
                />
              </div>
              <div className="flex gap-2">
                <Select value={sortOption} onValueChange={handleSortChange}>
                  <SelectTrigger className="flex-1">
                    <SelectValue placeholder="정렬 기준" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="createdAt,desc">최신순</SelectItem>
                    <SelectItem value="rating,desc">평점 높은 순</SelectItem>
                    <SelectItem value="rating,asc">평점 낮은 순</SelectItem>
                  </SelectContent>
                </Select>
                <Button onClick={applyFilters} className="bg-gray-800 hover:bg-gray-700">
                  검색
                </Button>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* 리뷰 목록 Card */}
        <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
          <CardHeader>
            <CardTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
              <MessageSquare className="w-5 h-5" />
              리뷰 목록 ({reviews.length}개)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {reviews?.map((review) => (
                <div key={review.reviewId} className="bg-gray-50 rounded-xl p-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4 shadow-sm border border-gray-100 transition">
                  <div className="flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-2 mb-2">
                      <span className="font-semibold text-lg text-gray-800 truncate max-w-xs" title={review.productName}>{review.productName || '상품명 없음'}</span>
                      <Badge variant={review.isHidden ? "secondary" : "default"} className={`ml-2 ${review.isHidden ? 'bg-red-100 text-red-800 border border-red-300' : 'bg-green-100 text-green-800 border border-green-300'}`}>{review.isHidden ? '숨김' : '공개'}</Badge>
                    </div>
                    <div className="flex flex-wrap gap-4 text-sm text-gray-600 mb-2">
                      <span><User className="inline w-4 h-4 mr-1 text-gray-400" />{review.customerName}</span>
                      <span><Store className="inline w-4 h-4 mr-1 text-gray-400" />판매자: {review.sellerName}</span>
                      <span><Calendar className="inline w-4 h-4 mr-1 text-gray-400" />{new Date(review.createdAt).toLocaleDateString()}</span>
                    </div>
                    <div className="text-gray-700 text-sm truncate max-w-2xl" title={review.contentSummary || review.content}>{review.contentSummary || review.content || '내용 없음'}</div>
                  </div>
                  <div className="flex flex-col items-end gap-2 min-w-[120px]">
                    <Button
                      variant="outline"
                      size="sm"
                      onClick={() => handleToggleVisibility(review.reviewId, review.isHidden)}
                      className="flex items-center gap-1"
                    >
                      {review.isHidden ? <Eye className="w-4 h-4" /> : <EyeOff className="w-4 h-4" />}
                      {review.isHidden ? '공개로 변경' : '숨김으로 변경'}
                    </Button>
                    <Button
                      variant="default"
                      size="sm"
                      onClick={() => router.push(`/admin/review-management/list/${review.reviewId}`)}
                      className="bg-gray-800 w-full !hover:bg-gray-800 !hover:shadow-none !hover:text-inherit"
                    >
                      상세보기
                    </Button>
                  </div>
                </div>
              ))}
              {(!reviews || reviews.length === 0) && !loading && (
                <div className="text-center py-12">
                  <MessageSquare className="w-16 h-16 mx-auto mb-4 text-gray-300" />
                  <h3 className="text-lg font-medium text-gray-500 mb-2">조회된 리뷰가 없습니다</h3>
                  <p className="text-gray-400">검색 조건을 변경해보세요.</p>
                </div>
              )}
            </div>
          </CardContent>
        </Card>

        {/* 페이징 */}
        {totalPages > 1 && (
          <div className="mt-8 flex justify-center gap-2">
            <Button
              variant="outline"
              onClick={() => fetchReviews(currentPage - 1)}
              disabled={currentPage === 1}
              className="px-3 py-1 border rounded disabled:opacity-50"
            >
              <ChevronLeft className="w-4 h-4" /> 이전
            </Button>
            <span className="px-3 py-1 text-gray-700 font-semibold">
              {currentPage} / {totalPages}
            </span>
            <Button
              variant="outline"
              onClick={() => fetchReviews(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="px-3 py-1 border rounded disabled:opacity-50"
            >
              다음 <ChevronRight className="w-4 h-4" />
            </Button>
          </div>
        )}
      </div>
    </div>
  );
}