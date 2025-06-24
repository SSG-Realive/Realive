"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

interface Settlement {
  id: string;
  seller: string;
  amount: number;
  date: string;
  status: "Pending" | "Completed";
  sellerImage: string;
}

const dummySettlements: Settlement[] = [
  { id: "SETT001", seller: "홍길동 (hong123)", amount: 100000, date: "2024-06-01", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/men/31.jpg" },
  { id: "SETT002", seller: "김철수 (kim456)", amount: 200000, date: "2024-06-02", status: "Completed", sellerImage: "https://randomuser.me/api/portraits/men/32.jpg" },
  { id: "SETT003", seller: "이영희 (lee789)", amount: 150000, date: "2024-06-03", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/women/33.jpg" },
  { id: "SETT004", seller: "박민수 (park321)", amount: 120000, date: "2024-06-04", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/men/34.jpg" },
  { id: "SETT005", seller: "최지우 (choi654)", amount: 180000, date: "2024-06-05", status: "Completed", sellerImage: "https://randomuser.me/api/portraits/women/35.jpg" },
  { id: "SETT006", seller: "정수진 (jung789)", amount: 250000, date: "2024-06-06", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/women/36.jpg" },
  { id: "SETT007", seller: "강동원 (kang123)", amount: 300000, date: "2024-06-07", status: "Completed", sellerImage: "https://randomuser.me/api/portraits/men/37.jpg" },
  { id: "SETT008", seller: "윤서연 (yoon456)", amount: 90000, date: "2024-06-08", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/women/38.jpg" },
  { id: "SETT009", seller: "임태현 (lim789)", amount: 220000, date: "2024-06-09", status: "Completed", sellerImage: "https://randomuser.me/api/portraits/men/39.jpg" },
  { id: "SETT010", seller: "한소희 (han123)", amount: 170000, date: "2024-06-10", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/women/40.jpg" },
  { id: "SETT011", seller: "송민호 (song456)", amount: 280000, date: "2024-06-11", status: "Completed", sellerImage: "https://randomuser.me/api/portraits/men/41.jpg" },
  { id: "SETT012", seller: "조은영 (cho789)", amount: 110000, date: "2024-06-12", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/women/42.jpg" },
];

export default function SettlementManagementPage() {
  const [sellerFilter, setSellerFilter] = useState("");
  const [dateFilter, setDateFilter] = useState("");
  const [amountFilter, setAmountFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize] = useState(10);
  const router = useRouter();

  // 인증 체크
  useEffect(() => {
    if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
      router.replace('/admin/login');
      return;
    }
  }, [router]);

  const filtered = dummySettlements.filter(s =>
    (!sellerFilter || s.seller.includes(sellerFilter)) &&
    (!dateFilter || s.date === dateFilter) &&
    (!amountFilter || s.amount.toString() === amountFilter) &&
    (!statusFilter || s.status === statusFilter)
  );

  // 페이징된 정산 목록
  const paginatedSettlements = filtered.slice((currentPage - 1) * pageSize, currentPage * pageSize);
  const totalPages = Math.ceil(filtered.length / pageSize);

  // 페이지 변경 핸들러
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  // 필터 변경 시 첫 페이지로 이동
  useEffect(() => {
    setCurrentPage(1);
  }, [sellerFilter, dateFilter, amountFilter, statusFilter]);

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
              <h1 className="text-2xl font-bold text-gray-900">정산 관리</h1>
              <p className="text-sm text-gray-600 mt-1">판매자 정산을 관리하고 처리할 수 있습니다.</p>
            </div>
            <div className="flex items-center gap-3">
              <div className="text-right">
                <div className="text-2xl font-bold text-green-600">{filtered.length}</div>
                <div className="text-sm text-gray-500">총 정산</div>
              </div>
            </div>
          </div>
        </div>

        {/* 필터 */}
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <div>
              <label htmlFor="sellerFilter" className="block text-sm font-medium text-gray-700 mb-2">
                판매자 검색
              </label>
              <input
                id="sellerFilter"
                type="text"
                placeholder="판매자명으로 검색"
                value={sellerFilter}
                onChange={e => setSellerFilter(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
              />
            </div>
            <div>
              <label htmlFor="dateFilter" className="block text-sm font-medium text-gray-700 mb-2">
                정산일
              </label>
              <input
                id="dateFilter"
                type="date"
                value={dateFilter}
                onChange={e => setDateFilter(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
              />
            </div>
            <div>
              <label htmlFor="amountFilter" className="block text-sm font-medium text-gray-700 mb-2">
                금액
              </label>
              <input
                id="amountFilter"
                type="text"
                placeholder="금액으로 검색"
                value={amountFilter}
                onChange={e => setAmountFilter(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
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
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
              >
                <option value="">전체</option>
                <option value="Pending">대기중</option>
                <option value="Completed">완료</option>
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
                      정산ID
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      판매자
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      금액
                    </th>
                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider whitespace-nowrap">
                      정산일
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
                  {paginatedSettlements.map((s, idx) => (
                    <tr key={s.id} className="hover:bg-gray-50">
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        {s.id}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        <div className="flex items-center">
                          <img
                            className="h-8 w-8 rounded-full mr-3"
                            src={s.sellerImage}
                            alt={s.seller}
                          />
                          <span>{s.seller}</span>
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 font-medium">
                        {s.amount.toLocaleString()}원
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                        {s.date}
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                          s.status === "Pending"
                            ? "bg-yellow-100 text-yellow-800"
                            : "bg-green-100 text-green-800"
                        }`}>
                          {s.status === "Pending" ? "대기중" : "완료"}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                        <button 
                          className="text-blue-600 hover:text-blue-900 underline"
                          onClick={() => router.push(`/admin/settlement-management/${s.id}`)}
                        >
                          상세보기
                        </button>
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
          {paginatedSettlements.map((s, idx) => (
            <div key={s.id} className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
              <div className="flex items-start gap-4 mb-3">
                {/* 판매자 이미지 */}
                <img
                  className="h-12 w-12 rounded-full flex-shrink-0"
                  src={s.sellerImage}
                  alt={s.seller}
                />
                
                {/* 정산 정보 */}
                <div className="flex-1 min-w-0">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium text-gray-900">
                      {(currentPage - 1) * pageSize + idx + 1}
                    </span>
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                      s.status === "Pending"
                        ? "bg-yellow-100 text-yellow-800"
                        : "bg-green-100 text-green-800"
                    }`}>
                      {s.status === "Pending" ? "대기중" : "완료"}
                    </span>
                  </div>
                  <h3 className="font-medium text-gray-900 mb-1">
                    {s.seller}
                  </h3>
                  <div className="space-y-1 text-sm text-gray-600">
                    <div className="flex items-center justify-between">
                      <span>정산ID: {s.id}</span>
                      <span className="font-medium text-green-600">
                        {s.amount.toLocaleString()}원
                      </span>
                    </div>
                    <div className="text-gray-500">
                      정산일: {s.date}
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="flex gap-2">
                <button 
                  className="flex-1 bg-blue-600 text-white py-2 px-3 rounded-md hover:bg-blue-700 transition-colors text-sm"
                  onClick={() => router.push(`/admin/settlement-management/${s.id}`)}
                >
                  상세보기
                </button>
                {s.status === "Pending" && (
                  <button className="bg-green-500 hover:bg-green-600 text-white px-3 py-2 rounded-md text-sm">
                    정산처리
                  </button>
                )}
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
                        ? 'bg-green-500 text-white' 
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
            <div className="text-gray-400 text-6xl mb-4">💰</div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">정산 내역을 찾을 수 없습니다</h3>
            <p className="text-gray-600 mb-4">
              검색 조건을 변경하거나 필터를 초기화해보세요.
            </p>
            <button 
              onClick={() => {
                setSellerFilter("");
                setDateFilter("");
                setAmountFilter("");
                setStatusFilter("");
              }}
              className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition-colors"
            >
              필터 초기화
            </button>
          </div>
        )}
      </div>
    </div>
  );
} 