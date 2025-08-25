"use client";
import { useRouter } from "next/navigation";
import { useState, useEffect } from "react";

interface Qna {
  id: number;
  user: string;
  title: string;
  created: string;
  answered: boolean;
}

const dummyQna: Qna[] = [];

export default function QnaManagementPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);
  const [answeredFilter, setAnsweredFilter] = useState("");

  // 인증 체크
  useEffect(() => {
    if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
      router.replace('/admin/login');
      return;
    }
  }, [router]);

  const filtered = dummyQna.filter(q => {
    const matchesSearch = q.title.includes(search) || q.user.includes(search);
    const matchesAnswered = !answeredFilter || 
      (answeredFilter === 'answered' && q.answered) ||
      (answeredFilter === 'unanswered' && !q.answered);
    return matchesSearch && matchesAnswered;
  });

  // 페이징된 Q&A 목록
  const paginatedQna = filtered.slice((currentPage - 1) * pageSize, currentPage * pageSize);
  const totalPages = Math.ceil(filtered.length / pageSize);

  // 페이지 변경 핸들러
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  // 필터 변경 시 첫 페이지로 이동
  useEffect(() => {
    setCurrentPage(1);
  }, [search, answeredFilter]);

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    return null;
  }

  return (
    <div className="w-full max-w-full min-h-screen bg-gray-50 p-2 sm:p-6 overflow-x-auto">
      <div className="w-full max-w-full">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Q&A 관리</h1>
              <p className="text-sm text-gray-600 mt-1">고객 문의를 관리하고 답변을 처리할 수 있습니다.</p>
            </div>
            <div className="flex items-center gap-3">
              <div className="text-right">
                <div className="text-2xl font-bold text-purple-600">{filtered.length}</div>
                <div className="text-sm text-gray-500">총 문의</div>
              </div>
            </div>
          </div>
        </div>

        {/* 필터 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="flex flex-col sm:flex-row gap-4">
            <div className="flex-1">
              <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
                검색
              </label>
        <input
                id="search"
          type="text"
                placeholder="제목 또는 작성자로 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
        />
      </div>
            <div className="sm:w-48">
              <label htmlFor="answeredFilter" className="block text-sm font-medium text-gray-700 mb-2">
                답변 상태
              </label>
              <select
                id="answeredFilter"
                value={answeredFilter}
                onChange={e => setAnsweredFilter(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-purple-500"
              >
                <option value="">전체</option>
                <option value="answered">답변완료</option>
                <option value="unanswered">미답변</option>
              </select>
            </div>
          </div>
        </div>

        {/* 데스크탑 표 */}
        <div className="hidden md:block">
          <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      번호
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      작성자
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      제목
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      작성일
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      답변여부
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      상세
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      액션
                    </th>
          </tr>
        </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {paginatedQna.map((q, idx) => (
                    <tr key={q.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {(currentPage - 1) * pageSize + idx + 1}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {q.user}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">
                        <div className="max-w-xs truncate" title={q.title}>
                          {q.title}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {q.created}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          q.answered 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {q.answered ? "답변완료" : "미답변"}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                <button 
                          className="text-blue-600 hover:text-blue-900 underline" 
                  onClick={() => router.push(`/admin/qna-management/${q.id}`)}
                >
                  상세
                </button>
              </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div className="flex space-x-2">
                          <button className="bg-green-500 hover:bg-green-600 text-white px-3 py-1 rounded text-xs">
                            답변
                          </button>
                          <button className="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-xs">
                            삭제
                          </button>
                        </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
            </div>
          </div>
        </div>

        {/* 모바일 카드형 리스트 */}
        <div className="block md:hidden space-y-4">
          {paginatedQna.map((q, idx) => (
            <div key={q.id} className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
              <div className="flex items-start justify-between mb-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-sm font-medium text-gray-900">
                      {(currentPage - 1) * pageSize + idx + 1}
                    </span>
                    <span className="text-sm text-gray-500">•</span>
                    <span className="text-sm text-gray-600">{q.user}</span>
                  </div>
                  <h3 className="font-medium text-gray-900 mb-1 line-clamp-2">
                    {q.title}
                  </h3>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-500">{q.created}</span>
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      q.answered 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {q.answered ? "답변완료" : "미답변"}
                    </span>
                  </div>
                </div>
              </div>
              
              <div className="flex gap-2">
                <button 
                  className="flex-1 bg-blue-600 text-white py-2 px-3 rounded-md hover:bg-blue-700 transition-colors text-sm"
                  onClick={() => router.push(`/admin/qna-management/${q.id}`)}
                >
                  상세 보기
                </button>
                <button className="bg-green-500 hover:bg-green-600 text-white px-3 py-2 rounded-md text-sm">
                  답변
                </button>
                <button className="bg-red-500 hover:bg-red-600 text-white px-3 py-2 rounded-md text-sm">
                  삭제
                </button>
              </div>
            </div>
          ))}
        </div>

        {/* 페이징 */}
        {totalPages > 1 && (
          <div className="mt-6 flex justify-center">
            <div className="flex space-x-2">
              <button
                onClick={() => handlePageChange(currentPage - 1)}
                disabled={currentPage === 1}
                className="px-3 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                이전
              </button>
              
              {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
                const pageNum = Math.max(1, Math.min(totalPages - 4, currentPage - 2)) + i;
                return (
                  <button
                    key={pageNum}
                    onClick={() => handlePageChange(pageNum)}
                    className={`px-3 py-2 border rounded ${
                      currentPage === pageNum 
                        ? 'bg-purple-500 text-white' 
                        : 'hover:bg-gray-50'
                    }`}
                  >
                    {pageNum}
                  </button>
                );
              })}
              
              <button
                onClick={() => handlePageChange(currentPage + 1)}
                disabled={currentPage === totalPages}
                className="px-3 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
              >
                다음
              </button>
            </div>
          </div>
        )}

        {/* 결과가 없을 때 */}
        {filtered.length === 0 && (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">❓</div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">문의를 찾을 수 없습니다</h3>
            <p className="text-gray-600 mb-4">
              검색 조건을 변경하거나 필터를 초기화해보세요.
            </p>
            <button 
              onClick={() => {
                setSearch("");
                setAnsweredFilter("");
              }}
              className="bg-purple-600 text-white px-4 py-2 rounded hover:bg-purple-700 transition-colors"
            >
              필터 초기화
            </button>
          </div>
        )}
      </div>
    </div>
  );
} 