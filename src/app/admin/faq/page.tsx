"use client";
import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';

interface FAQ {
  id: number;
  question: string;
  author: string;
  created: string;
  status: "answered" | "unanswered";
  category: string;
}

const dummyFAQ: FAQ[] = [
  { id: 1, question: "배송은 얼마나 걸리나요?", author: "user2", created: "2024-06-10", status: "unanswered", category: "배송" },
  { id: 2, question: "환불은 어떻게 하나요?", author: "user3", created: "2024-06-09", status: "answered", category: "환불" },
  { id: 3, question: "상품 교환 가능한가요?", author: "user4", created: "2024-06-08", status: "answered", category: "교환" },
  { id: 4, question: "포인트 적립 방법", author: "user5", created: "2024-06-07", status: "unanswered", category: "포인트" },
  { id: 5, question: "회원 탈퇴 방법", author: "user6", created: "2024-06-06", status: "answered", category: "회원" },
  { id: 6, question: "비밀번호 변경", author: "user7", created: "2024-06-05", status: "unanswered", category: "회원" },
  { id: 7, question: "쿠폰 사용법", author: "user8", created: "2024-06-04", status: "answered", category: "쿠폰" },
  { id: 8, question: "상품 리뷰 작성", author: "user9", created: "2024-06-03", status: "answered", category: "리뷰" },
  { id: 9, question: "결제 오류 해결", author: "user10", created: "2024-06-02", status: "unanswered", category: "결제" },
  { id: 10, question: "배송지 변경", author: "user11", created: "2024-06-01", status: "answered", category: "배송" },
  { id: 11, question: "상품 재고 문의", author: "user12", created: "2024-05-31", status: "unanswered", category: "상품" },
  { id: 12, question: "할인 이벤트 기간", author: "user13", created: "2024-05-30", status: "answered", category: "이벤트" },
];

export default function AdminFAQPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);
  const [statusFilter, setStatusFilter] = useState("");
  const [categoryFilter, setCategoryFilter] = useState("");

  // 인증 체크
  useEffect(() => {
    if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
      router.replace('/admin/login');
      return;
    }
  }, [router]);

  const filtered = dummyFAQ.filter(faq => {
    const matchesSearch = faq.question.includes(search) || faq.author.includes(search);
    const matchesStatus = !statusFilter || faq.status === statusFilter;
    const matchesCategory = !categoryFilter || faq.category === categoryFilter;
    return matchesSearch && matchesStatus && matchesCategory;
  });

  // 페이징된 FAQ 목록
  const paginatedFAQ = filtered.slice((currentPage - 1) * pageSize, currentPage * pageSize);
  const totalPages = Math.ceil(filtered.length / pageSize);

  // 페이지 변경 핸들러
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  // 필터 변경 시 첫 페이지로 이동
  useEffect(() => {
    setCurrentPage(1);
  }, [search, statusFilter, categoryFilter]);

  // 카테고리 목록
  const categories = [...new Set(dummyFAQ.map(faq => faq.category))];

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
              <h1 className="text-2xl font-bold text-gray-900">FAQ 관리</h1>
              <p className="text-sm text-gray-600 mt-1">자주 묻는 질문을 관리하고 답변을 처리할 수 있습니다.</p>
            </div>
            <div className="flex items-center gap-3">
              <div className="text-right">
                <div className="text-2xl font-bold text-blue-600">{filtered.length}</div>
                <div className="text-sm text-gray-500">총 FAQ</div>
              </div>
            </div>
          </div>
        </div>

        {/* 필터 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label htmlFor="search" className="block text-sm font-medium text-gray-700 mb-2">
                검색
              </label>
              <input
                id="search"
                type="text"
                placeholder="질문 또는 작성자로 검색"
                value={search}
                onChange={e => setSearch(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
            <div>
              <label htmlFor="statusFilter" className="block text-sm font-medium text-gray-700 mb-2">
                상태
              </label>
              <select
                id="statusFilter"
                value={statusFilter}
                onChange={e => setStatusFilter(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">전체</option>
                <option value="answered">답변완료</option>
                <option value="unanswered">미답변</option>
              </select>
            </div>
            <div>
              <label htmlFor="categoryFilter" className="block text-sm font-medium text-gray-700 mb-2">
                카테고리
              </label>
              <select
                id="categoryFilter"
                value={categoryFilter}
                onChange={e => setCategoryFilter(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">전체</option>
                {categories.map(category => (
                  <option key={category} value={category}>{category}</option>
                ))}
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
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                      질문
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      작성자
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      카테고리
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      등록일
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      상태
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      액션
                    </th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {paginatedFAQ.map((faq, idx) => (
                    <tr key={faq.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {(currentPage - 1) * pageSize + idx + 1}
                      </td>
                      <td className="px-6 py-4 text-sm text-gray-900">
                        <div className="max-w-xs truncate" title={faq.question}>
                          {faq.question}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {faq.author}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">
                          {faq.category}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {faq.created}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          faq.status === 'answered' 
                            ? 'bg-green-100 text-green-800' 
                            : 'bg-red-100 text-red-800'
                        }`}>
                          {faq.status === 'answered' ? "답변완료" : "미답변"}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <div className="flex space-x-2">
                          <button className="bg-blue-500 hover:bg-blue-600 text-white px-3 py-1 rounded text-xs">
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
          {paginatedFAQ.map((faq, idx) => (
            <div key={faq.id} className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
              <div className="flex items-start justify-between mb-3">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <span className="text-sm font-medium text-gray-900">
                      {(currentPage - 1) * pageSize + idx + 1}
                    </span>
                    <span className="text-sm text-gray-500">•</span>
                    <span className="text-sm text-gray-600">{faq.author}</span>
                    <span className="text-sm text-gray-500">•</span>
                    <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">
                      {faq.category}
                    </span>
                  </div>
                  <h3 className="font-medium text-gray-900 mb-1 line-clamp-2">
                    {faq.question}
                  </h3>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-500">{faq.created}</span>
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      faq.status === 'answered' 
                        ? 'bg-green-100 text-green-800' 
                        : 'bg-red-100 text-red-800'
                    }`}>
                      {faq.status === 'answered' ? "답변완료" : "미답변"}
                    </span>
                  </div>
                </div>
              </div>
              
              <div className="flex gap-2">
                <button className="flex-1 bg-blue-500 hover:bg-blue-600 text-white px-3 py-2 rounded-md text-sm">
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
                        ? 'bg-blue-500 text-white' 
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
            <h3 className="text-lg font-medium text-gray-900 mb-2">FAQ를 찾을 수 없습니다</h3>
            <p className="text-gray-600 mb-4">
              검색 조건을 변경하거나 필터를 초기화해보세요.
            </p>
            <button 
              onClick={() => {
                setSearch("");
                setStatusFilter("");
                setCategoryFilter("");
              }}
              className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors"
            >
              필터 초기화
            </button>
          </div>
        )}
      </div>
    </div>
  );
} 