"use client";
import React, { useState, useEffect } from "react";
import { Card, CardContent } from "@/components/ui/card";
import { ScrollArea } from "@/components/ui/scroll-area";
import Link from "next/link";
import { useRouter } from 'next/navigation';
import { adminAuctionService } from '@/service/admin/auctionService';
import { AuctionResponseDTO } from '@/types/admin/auction';

type StatusFilter = 'ALL' | 'PROCEEDING' | 'SCHEDULED' | 'COMPLETED';

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
    
    // 상태 필터링
    let matchesStatus = true;
    if (statusFilter === 'PROCEEDING') {
      matchesStatus = a.statusText === '진행중';
    } else if (statusFilter === 'SCHEDULED') {
      matchesStatus = a.statusText === '예정';
    } else if (statusFilter === 'COMPLETED') {
      matchesStatus = a.statusText === '종료';
    }
    
    return matchesSearch && matchesStatus;
  });

  // 이미지 URL 생성 함수
  const getImageUrl = (imagePath: string): string => {
    if (!imagePath) return '/images/placeholder.png';
    if (imagePath.startsWith('http')) return imagePath;
    return `http://localhost:8080${imagePath}`;
  };

  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  const handleStatusFilterChange = (filter: StatusFilter) => {
    setStatusFilter(filter);
    setCurrentPage(0); // 필터 변경 시 첫 페이지로 이동
  };

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
        <div className="text-red-500 text-center">{error}</div>
        <button 
          onClick={fetchAuctions}
          className="mt-4 px-4 py-2 bg-blue-500 text-white rounded"
        >
          다시 시도
        </button>
      </div>
    );
  }

  return (
    <div className="p-8">
      <div className="mb-6">
        <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
          <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center">
            <input
              type="text"
              placeholder="상품명/상태 검색"
              value={search}
              onChange={e => setSearch(e.target.value)}
              className="border rounded px-3 py-2 w-64"
            />
            
            {/* 상태 필터 버튼들 */}
            <div className="flex gap-2">
              <button
                onClick={() => handleStatusFilterChange('ALL')}
                className={`px-4 py-2 rounded text-sm font-medium transition-colors ${
                  statusFilter === 'ALL'
                    ? 'bg-blue-500 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                전체
              </button>
              <button
                onClick={() => handleStatusFilterChange('PROCEEDING')}
                className={`px-4 py-2 rounded text-sm font-medium transition-colors ${
                  statusFilter === 'PROCEEDING'
                    ? 'bg-blue-500 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                진행중
              </button>
              <button
                onClick={() => handleStatusFilterChange('SCHEDULED')}
                className={`px-4 py-2 rounded text-sm font-medium transition-colors ${
                  statusFilter === 'SCHEDULED'
                    ? 'bg-blue-500 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                예정
              </button>
              <button
                onClick={() => handleStatusFilterChange('COMPLETED')}
                className={`px-4 py-2 rounded text-sm font-medium transition-colors ${
                  statusFilter === 'COMPLETED'
                    ? 'bg-blue-500 text-white'
                    : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                }`}
              >
                종료
              </button>
            </div>
          </div>
          
          <div className="text-sm text-gray-600">
            총 {totalElements}개 중 {currentPage * 10 + 1}-{Math.min((currentPage + 1) * 10, totalElements)}개
          </div>
        </div>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {filtered.length === 0 ? (
          <div className="col-span-full text-center py-8">
            {search || statusFilter !== 'ALL' ? '검색 결과가 없습니다.' : '등록된 경매가 없습니다.'}
          </div>
        ) : (
          filtered.map(a => (
            <div key={a.id} className="border rounded-lg p-4 hover:shadow-md transition-shadow">
              <div className="flex items-start space-x-4">
                <div className="flex-shrink-0">
                  {a.adminProduct?.imageThumbnailUrl ? (
                    <img 
                      src={getImageUrl(a.adminProduct.imageThumbnailUrl)} 
                      alt="auction" 
                      className="w-20 h-20 object-cover rounded"
                      onError={(e) => {
                        e.currentTarget.src = '/images/placeholder.png';
                      }}
                    />
                  ) : (
                    <div className="w-20 h-20 bg-gray-200 flex items-center justify-center rounded text-xs">
                      이미지 없음
                    </div>
                  )}
                </div>
                <div className="flex-1 min-w-0">
                  <h3 className="font-semibold text-lg truncate">{a.adminProduct?.productName}</h3>
                  <div className="text-sm text-gray-600 space-y-1 mt-2">
                    <div>시작가: {a.startPrice?.toLocaleString()}원</div>
                    <div>현재가: {a.currentPrice?.toLocaleString()}원</div>
                    <div>시작시간: {new Date(a.startTime).toLocaleString()}</div>
                    <div>종료시간: {new Date(a.endTime).toLocaleString()}</div>
                    <div className="font-medium">
                      상태: {a.statusText || a.status}
                    </div>
                    <div>상품설명: {a.adminProduct?.productDescription}</div>
                  </div>
                  <div className="mt-3">
                    <Link 
                      href={`/admin/auction-management/list/${a.id}`} 
                      className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                    >
                      상세보기 →
                    </Link>
                  </div>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* 페이지네이션 */}
      {totalPages > 1 && (
        <div className="mt-8 flex justify-center">
          <div className="flex space-x-2">
            <button
              onClick={() => handlePageChange(currentPage - 1)}
              disabled={currentPage === 0}
              className="px-3 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              이전
            </button>
            
            {Array.from({ length: Math.min(5, totalPages) }, (_, i) => {
              const pageNum = Math.max(0, Math.min(totalPages - 5, currentPage - 2)) + i;
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
                  {pageNum + 1}
                </button>
              );
            })}
            
            <button
              onClick={() => handlePageChange(currentPage + 1)}
              disabled={currentPage === totalPages - 1}
              className="px-3 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
            >
              다음
            </button>
          </div>
        </div>
      )}

      {/* 상세 모달 */}
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[320px] max-w-md">
            <h2 className="text-xl font-bold mb-4">경매 상세</h2>
            {selected.adminProduct?.imageThumbnailUrl && (
              <img 
                src={getImageUrl(selected.adminProduct.imageThumbnailUrl)} 
                alt="auction" 
                className="mb-4 w-40 h-40 object-cover rounded"
                onError={(e) => {
                  e.currentTarget.src = '/images/placeholder.png';
                }}
              />
            )}
            <p><b>상품명:</b> {selected.adminProduct?.productName}</p>
            <p><b>시작가:</b> {selected.startPrice?.toLocaleString()}원</p>
            <p><b>현재가:</b> {selected.currentPrice?.toLocaleString()}원</p>
            <p><b>시작시간:</b> {new Date(selected.startTime).toLocaleString()}</p>
            <p><b>종료시간:</b> {new Date(selected.endTime).toLocaleString()}</p>
            <p><b>상태:</b> {selected.status}</p>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelected(null)}>닫기</button>
          </div>
        </div>
      )}
    </div>
  );
}

export default AuctionListPage; 