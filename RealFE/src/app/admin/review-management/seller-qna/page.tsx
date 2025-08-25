"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { getAdminReviewQnaList, answerAdminReviewQna } from "@/service/admin/reviewService";
import { getSellerQnaStatistics } from "@/service/admin/adminQnaService";
import { AdminReviewQna, AdminReviewQnaListRequest } from "@/types/admin/review";
import { useGlobalDialog } from "@/app/context/dialogContext";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Badge } from "@/components/ui/badge";
import { MessageSquare, Filter, RefreshCw, Search, User, Store, Calendar, ChevronLeft, ChevronRight } from "lucide-react";

export default function ReviewQnaPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const [qnas, setQnas] = useState<AdminReviewQna[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [answeredFilter, setAnsweredFilter] = useState<string>("all");
  const [selectedQna, setSelectedQna] = useState<AdminReviewQna | null>(null);
  const [answerText, setAnswerText] = useState("");
  const [statistics, setStatistics] = useState<{ totalCount: number; answeredCount: number; unansweredCount: number }>({
    totalCount: 0,
    answeredCount: 0,
    unansweredCount: 0
  });
  const [totalElements, setTotalElements] = useState(0);
  const {show} = useGlobalDialog();

  // snake_case → camelCase 변환 함수
  const toCamelQna = (qna: any): AdminReviewQna => ({
    ...qna,
    isAnswered: qna.isAnswered ?? qna.is_answered ?? qna.answered ?? false,
    answeredAt: qna.answeredAt ?? qna.answered_at,
    createdAt: qna.createdAt ?? qna.created_at,
    updatedAt: qna.updatedAt ?? qna.updated_at,
  });

  // 통계 조회
  const fetchStatistics = async () => {
    try {
      const stats = await getSellerQnaStatistics();
      setStatistics({
        totalCount: stats.totalCount,
        answeredCount: stats.answeredCount,
        unansweredCount: stats.unansweredCount
      });
    } catch (err: any) {
      console.error('통계 조회 실패:', err);
      // 통계 조회 실패 시 기본값 유지
    }
  };

  // Q&A 목록 조회
  const fetchQnas = async (page: number = 1) => {
    try {
      setLoading(true);
      setError(null);
      
      const params: AdminReviewQnaListRequest = {
        page: page - 1, // 백엔드는 0-based pagination
        size: 10,
        search: search || undefined,
        isAnswered: answeredFilter === "all" ? undefined : answeredFilter === "true" ? true : answeredFilter === "false" ? false : undefined
      };

      // 디버깅용 로그 추가
      console.log('Q&A 목록 조회 파라미터:', params);
      console.log('검색어:', search);
      console.log('답변 상태 필터:', answeredFilter);

      const response = await getAdminReviewQnaList(params);
      // camelCase로 일괄 변환
      const mappedQnas = (response.content as any[]).map(toCamelQna);
      setQnas(mappedQnas);
      setTotalPages(response.totalPages);
      setCurrentPage(page);
      setTotalElements(response.totalElements ?? 0);
      
      // 통계는 별도로 조회 (전체 통계)
      await fetchStatistics();
    } catch (err: any) {
      console.error('Q&A 목록 조회 실패:', err);
      setError(err.message || 'Q&A 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 초기 로드
  useEffect(() => {
    fetchQnas();
  }, []);

  // 검색 및 필터 적용
  const handleSearch = () => {
    fetchQnas(1);
  };

  // 답변 등록
  const handleSubmitAnswer = async () => {
    if (!selectedQna || !answerText.trim()) {
      show('답변을 입력해주세요.');
      return;
    }

    try {
      await answerAdminReviewQna(selectedQna.id, { answer: answerText });
      show('답변이 등록되었습니다.');
      
      // 즉시 목록 상태 업데이트
      setQnas(prevQnas => 
        prevQnas.map(qna => 
          qna.id === selectedQna.id 
            ? { ...qna, isAnswered: true, answer: answerText }
            : qna
        )
      );
      
      setSelectedQna(null);
      setAnswerText("");
      
      // 통계 업데이트
      await fetchStatistics();
    } catch (err: any) {
      console.error('답변 등록 실패:', err);
      show(err.message || '답변 등록에 실패했습니다.');
    }
  };

  // 상태 텍스트 변환
  const getStatusText = (status: string) => {
    switch (status) {
      case 'PENDING': return '대기중';
      case 'ANSWERED': return '답변완료';
      case 'HIDDEN': return '숨김';
      default: return status;
    }
  };

  // 상태별 스타일
  const getStatusStyle = (status: string) => {
    switch (status) {
      case 'PENDING': return 'text-yellow-600';
      case 'ANSWERED': return 'text-green-600';
      case 'HIDDEN': return 'text-gray-600';
      default: return '';
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
        <div className="text-red-600 text-center mb-4">{error}</div>
        <button 
          onClick={() => fetchQnas()} 
          className="bg-blue-500 text-white px-4 py-2 rounded"
        >
          다시 시도
        </button>
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
                <h1 className="text-4xl font-bold text-gray-800">Q&A 관리</h1>
                <p className="text-gray-600 text-lg mt-2">판매자 Q&A를 관리하고 답변할 수 있습니다.</p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <Button onClick={() => fetchQnas(currentPage)} variant="outline" className="flex items-center space-x-2">
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
              <div className="text-3xl font-bold text-purple-700">{statistics?.totalCount ?? 0}</div>
              <div className="text-sm text-gray-500 mt-1">총 문의</div>
            </CardContent>
          </Card>
          <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
            <CardContent className="flex flex-col items-center py-6">
              <div className="text-3xl font-bold text-green-700">{statistics?.answeredCount ?? 0}</div>
              <div className="text-sm text-gray-500 mt-1">답변완료</div>
            </CardContent>
          </Card>
          <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
            <CardContent className="flex flex-col items-center py-6">
              <div className="text-3xl font-bold text-red-700">{statistics?.unansweredCount ?? 0}</div>
              <div className="text-sm text-gray-500 mt-1">미답변</div>
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
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="relative">
                <Search className="w-4 h-4 absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400" />
                <Input
                  type="text"
                  placeholder="제목/내용/작성자 검색"
                  value={search}
                  onChange={e => setSearch(e.target.value)}
                  onKeyPress={e => e.key === 'Enter' && handleSearch()}
                  className="pl-10"
                />
              </div>
              <Select value={answeredFilter} onValueChange={setAnsweredFilter}>
                <SelectTrigger className="w-full">
                  <SelectValue placeholder="전체 답변상태" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">전체</SelectItem>
                  <SelectItem value="false">미답변</SelectItem>
                  <SelectItem value="true">답변완료</SelectItem>
                </SelectContent>
              </Select>
              <Button onClick={handleSearch} className="bg-gray-800 hover:bg-gray-700 col-span-1 md:col-span-2 mt-2">검색</Button>
            </div>
          </CardContent>
        </Card>

        {/* Q&A 목록 Card */}
        <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
          <CardHeader>
            <CardTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
              <MessageSquare className="w-5 h-5" />
              Q&A 목록 ({qnas.length}개)
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-4">
              {qnas?.map(qna => (
                <div key={qna.id} className="bg-gray-50 rounded-xl p-6 flex flex-col md:flex-row md:items-center md:justify-between gap-4 shadow-sm border border-gray-100 transition">
                  <div className="flex-1 min-w-0">
                    <div className="flex flex-wrap items-center gap-2 mb-2">
                      <span className="font-semibold text-lg text-gray-800 truncate max-w-xs" title={qna.title}>{qna.title}</span>
                      <Badge variant={qna.isAnswered ? "default" : "secondary"} className={`ml-2 ${qna.isAnswered ? 'bg-green-100 text-green-800 border border-green-300' : 'bg-red-100 text-red-800 border border-red-300'}`}>{qna.isAnswered ? "답변완료" : "미답변"}</Badge>
                    </div>
                    <div className="flex flex-wrap gap-4 text-sm text-gray-600 mb-2">
                      <span><User className="inline w-4 h-4 mr-1 text-gray-400" />{qna.userName}</span>
                      <span><Store className="inline w-4 h-4 mr-1 text-gray-400" />상품: {qna.productName}</span>
                      <span><Calendar className="inline w-4 h-4 mr-1 text-gray-400" />{new Date(qna.createdAt).toLocaleDateString()}</span>
                    </div>
                    <div className="text-gray-700 text-sm truncate max-w-2xl" title={qna.content}>{qna.content}</div>
                  </div>
                  <div className="flex flex-col items-end gap-2 min-w-[120px]">
                    <Button
                      variant="default"
                      size="sm"
                      onClick={() => router.push(`/admin/review-management/seller-qna/${qna.id}`)}
                      className={qna.isAnswered
                        ? "bg-green-600 hover:bg-green-700 text-white"
                        : "bg-blue-600 hover:bg-blue-700 text-white"}
                    >
                      {qna.isAnswered ? "상세보기" : "답변하기"}
                    </Button>
                    {qna.isAnswered && (
                      <div className="text-green-600 text-xs mt-1">답변완료 처리됨</div>
                    )}
                  </div>
                </div>
              ))}
            </div>

            {/* 페이징 UI */}
            {totalPages > 1 && (
              <div className="flex items-center justify-center space-x-2 mt-6">
                <Button
                  variant="outline"
                  onClick={() => fetchQnas(currentPage - 1)}
                  disabled={currentPage <= 1}
                  className="flex items-center space-x-1"
                >
                  <ChevronLeft className="w-4 h-4" />
                  이전
                </Button>
                
                <div className="flex items-center space-x-1">
                  {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                    <Button
                      key={page}
                      variant={currentPage === page ? "default" : "outline"}
                      onClick={() => fetchQnas(page)}
                      className="w-8 h-8"
                    >
                      {page}
                    </Button>
                  ))}
                </div>
                
                <Button
                  variant="outline"
                  onClick={() => fetchQnas(currentPage + 1)}
                  disabled={currentPage >= totalPages}
                  className="flex items-center space-x-1"
                >
                  다음
                  <ChevronRight className="w-4 h-4" />
                </Button>
              </div>
            )}

            {/* 페이지 정보 표시 */}
            <div className="text-center text-sm text-gray-500 mt-4">
              총 {totalElements}개의 문의 중 {(currentPage - 1) * 10 + 1} - {Math.min(currentPage * 10, totalElements)}번째 문의를 보여주고 있습니다.
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}