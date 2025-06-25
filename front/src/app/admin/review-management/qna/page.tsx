"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { getAdminReviewQnaList, answerAdminReviewQna } from "@/service/admin/reviewService";
import { AdminReviewQna, AdminReviewQnaListRequest } from "@/types/admin/review";

export default function ReviewQnaPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const [qnas, setQnas] = useState<AdminReviewQna[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [statusFilter, setStatusFilter] = useState<string>("");
  const [answeredFilter, setAnsweredFilter] = useState<string>("");
  const [selectedQna, setSelectedQna] = useState<AdminReviewQna | null>(null);
  const [answerText, setAnswerText] = useState("");

  // Q&A 목록 조회
  const fetchQnas = async (page: number = 1) => {
    try {
      setLoading(true);
      setError(null);
      
      const params: AdminReviewQnaListRequest = {
        page: page - 1, // 백엔드는 0-based pagination
        size: 10,
        search: search || undefined,
        status: statusFilter || undefined,
        isAnswered: answeredFilter === "true" ? true : answeredFilter === "false" ? false : undefined
      };

      const response = await getAdminReviewQnaList(params);
      setQnas(response.content);
      setTotalPages(response.totalPages);
      setCurrentPage(page);
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
      alert('답변을 입력해주세요.');
      return;
    }

    try {
      await answerAdminReviewQna(selectedQna.id, { answer: answerText });
      alert('답변이 등록되었습니다.');
      setSelectedQna(null);
      setAnswerText("");
      fetchQnas(currentPage); // 목록 새로고침
    } catch (err: any) {
      console.error('답변 등록 실패:', err);
      alert(err.message || '답변 등록에 실패했습니다.');
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
    <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-8 overflow-x-auto">
      <h1 className="text-2xl font-bold mb-6">Q&A 관리</h1>
      {/* 데스크탑 표 */}
      <div className="hidden md:block">
        {/* 검색 및 필터 */}
        <div className="mb-6 flex gap-4 items-center">
          <input
            type="text"
            placeholder="제목/내용/작성자 검색"
            value={search}
            onChange={e => setSearch(e.target.value)}
            onKeyPress={e => e.key === 'Enter' && handleSearch()}
            className="border rounded px-3 py-2 flex-1"
          />
          <select
            value={statusFilter}
            onChange={e => setStatusFilter(e.target.value)}
            className="border rounded px-3 py-2"
          >
            <option value="">전체 상태</option>
            <option value="PENDING">대기중</option>
            <option value="ANSWERED">답변완료</option>
            <option value="HIDDEN">숨김</option>
          </select>
          <select
            value={answeredFilter}
            onChange={e => setAnsweredFilter(e.target.value)}
            className="border rounded px-3 py-2"
          >
            <option value="">전체</option>
            <option value="false">미답변</option>
            <option value="true">답변완료</option>
          </select>
          <button 
            onClick={handleSearch}
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            검색
          </button>
        </div>

        {/* Q&A 테이블 */}
        <div className="overflow-x-auto w-full">
          <table className="min-w-[900px] w-full border text-sm">
            <thead>
              <tr className="bg-gray-100">
                <th className="whitespace-nowrap px-2 py-2 text-xs">상품명</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">제목</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">작성자</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">작성일</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">상태</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">답변상태</th>
                <th className="whitespace-nowrap px-2 py-2 text-xs">상세</th>
              </tr>
            </thead>
            <tbody>
                {qnas?.map(qna => (
                  <tr key={qna.id} className="hover:bg-gray-50">
                    <td className="whitespace-nowrap px-2 py-2 text-xs">{qna.productName}</td>
                    <td className="whitespace-nowrap px-2 py-2 text-xs max-w-xs truncate" title={qna.title}>
                      {qna.title}
                    </td>
                    <td className="whitespace-nowrap px-2 py-2 text-xs">{qna.userName}</td>
                    <td className="whitespace-nowrap px-2 py-2 text-xs">
                      {new Date(qna.createdAt).toLocaleDateString()}
                    </td>
                    <td className={`whitespace-nowrap px-2 py-2 text-center ${getStatusStyle(qna.status)}`}>
                      {getStatusText(qna.status)}
                    </td>
                    <td className="whitespace-nowrap px-2 py-2 text-center">
                      <span className={qna.isAnswered ? 'text-green-600' : 'text-red-600'}>
                        {qna.isAnswered ? '답변완료' : '미답변'}
                      </span>
                    </td>
                    <td className="whitespace-nowrap px-2 py-2 text-center">
                    <button 
                        className="text-blue-600 underline hover:text-blue-800" 
                        onClick={() => {
                          console.log('Q&A 상세 버튼 클릭:', qna.id);
                          try {
                            router.push(`/admin/review-management/qna/${qna.id}`);
                          } catch (error) {
                            console.error('라우터 에러:', error);
                            window.location.href = `/admin/review-management/qna/${qna.id}`;
                          }
                        }}
                    >
                        상세
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* 페이지네이션 */}
        {totalPages > 1 && (
          <div className="mt-6 flex justify-center gap-2">
            <button
              onClick={() => fetchQnas(currentPage - 1)}
              disabled={currentPage === 1}
              className="px-3 py-1 border rounded disabled:opacity-50"
            >
              이전
            </button>
            <span className="px-3 py-1">
              {currentPage} / {totalPages}
            </span>
            <button
              onClick={() => fetchQnas(currentPage + 1)}
              disabled={currentPage === totalPages}
              className="px-3 py-1 border rounded disabled:opacity-50"
            >
              다음
            </button>
          </div>
        )}

        {(!qnas || qnas.length === 0) && !loading && (
          <div className="text-center text-gray-500 mt-8">
            조회된 Q&A가 없습니다.
          </div>
        )}
      </div>
      {/* 모바일 카드형 */}
      <div className="block md:hidden space-y-4">
        {qnas?.map((qna, idx) => (
          <div key={qna.id} className="bg-white rounded shadow p-4">
            <div className="font-bold mb-2">상품명: {qna.productName}</div>
            <div className="mb-1">제목: {qna.title}</div>
            <div className="mb-1">작성자: {qna.userName}</div>
            <div className="mb-1">작성일: {new Date(qna.createdAt).toLocaleDateString()}</div>
            <div className="mb-1">상태: {getStatusText(qna.status)}</div>
            <div className="mb-1">답변상태: {qna.isAnswered ? '답변완료' : '미답변'}</div>
            <div>
              <button
                className="text-blue-600 underline"
                onClick={() => {
                  router.push(`/admin/review-management/qna/${qna.id}`);
                }}
              >상세</button>
            </div>
          </div>
        ))}
      </div>

      {/* 답변 모달 */}
      {selectedQna && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[500px] max-w-[800px] max-h-[80vh] overflow-y-auto">
            <h2 className="text-xl font-bold mb-4">Q&A 답변</h2>
            <div className="space-y-4 mb-6">
              <div>
                <p className="font-semibold">상품명</p>
                <p>{selectedQna.productName}</p>
              </div>
              <div>
                <p className="font-semibold">제목</p>
                <p>{selectedQna.title}</p>
              </div>
              <div>
                <p className="font-semibold">작성자</p>
                <p>{selectedQna.userName}</p>
              </div>
              <div>
                <p className="font-semibold">내용</p>
                <p className="whitespace-pre-wrap">{selectedQna.content}</p>
              </div>
              {selectedQna.answer && (
                <div>
                  <p className="font-semibold">기존 답변</p>
                  <p className="whitespace-pre-wrap text-gray-600">{selectedQna.answer}</p>
                </div>
              )}
            </div>
            {!selectedQna.isAnswered && (
              <div className="mb-6">
                <p className="font-semibold mb-2">답변</p>
                <textarea
                  value={answerText}
                  onChange={e => setAnswerText(e.target.value)}
                  placeholder="답변을 입력하세요..."
                  className="w-full h-32 border rounded p-2 resize-none"
                />
              </div>
            )}
            <div className="flex gap-2">
              {!selectedQna.isAnswered && (
                <button 
                  className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                  onClick={handleSubmitAnswer}
                >
                  답변 등록
                </button>
              )}
              <button 
                className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
                onClick={() => {
                  setSelectedQna(null);
                  setAnswerText("");
                }}
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}