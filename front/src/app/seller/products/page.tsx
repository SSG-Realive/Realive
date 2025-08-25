'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { Armchair, Layers, AlertTriangle, Plus, Eye, TrendingUp, TrendingDown, BadgeCheck, Ban, Calculator } from 'lucide-react';

import { getMyProducts } from '@/service/seller/productService';
import { ProductListItem } from '@/types/seller/product/productList';

export default function ProductListPage() {
  const checking = useSellerAuthGuard();
   

  const router = useRouter();
  const searchParams = useSearchParams();

  const [products, setProducts] = useState<ProductListItem[]>([]);
  const [totalProductCount, setTotalProductCount] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(1);
  const [sidebarOpen, setSidebarOpen] = useState(false);

  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  const toggleSidebar = () => {
    setSidebarOpen(!sidebarOpen);
  };

  useEffect(() => {
    if (checking) return;

    const page = parseInt(searchParams.get('page') || '1', 10);
    setCurrentPage(page);
    fetchProductList(page);
  }, [searchParams, checking]);

  const fetchProductList = async (page: number) => {
    try {
      const data = await getMyProducts({
        page,
        keyword,
        status: statusFilter,
      });

      setProducts(data.dtoList);
      setTotalProductCount(data.total);
      setTotalPages(Math.ceil(data.total / data.size));
    } catch (err) {
      console.error('상품 목록 조회 실패', err);
    }
  };

  const goToPage = (page: number) => {
    router.push(`/seller/products?page=${page}`);
  };

  const handleSearch = () => {
    if (checking) return;
    fetchProductList(1);
    router.push(`/seller/products?page=1`);
  };

  const handleRegisterClick = () => {
    router.push('/seller/products/new');
  };

  // 통계 계산
  const avgPrice = products.length > 0 ? Math.round(products.reduce((sum, p) => sum + (p.price || 0), 0) / products.length) : 0;
  const maxPrice = products.length > 0 ? Math.max(...products.map(p => p.price)) : 0;
  const minPrice = products.length > 0 ? Math.min(...products.map(p => p.price)) : 0;
  const pendingCount = products.filter(p => p.status === '승인대기').length;
  const rejectedCount = products.filter(p => p.status === '반려').length;

  if (checking) return (
    <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-gray-50 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p className="text-gray-600">인증 확인 중...</p>
      </div>
    </div>
  ); // ✅ 인증 확인 중 UI 
  return (
    <>
      <div className="hidden">
      <SellerHeader toggleSidebar={toggleSidebar} />
      </div>
      <SellerLayout>
        <div className="flex-1 w-full h-full px-4 py-8 bg-[#a89f91]">
          <h1 className="text-xl md:text-2xl font-bold mb-6 text-[#5b4636]">상품 관리</h1>

          {/* 상단 통계 카드 */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-6">
            <section className="bg-[#e9dec7] p-6 rounded-xl shadow border border-[#bfa06a] flex items-center justify-between">
              <div>
                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">총 등록 상품</h2>
                <p className="text-xl md:text-2xl font-bold text-[#5b4636]">{totalProductCount}개</p>
              </div>
              <Armchair className="w-8 h-8 text-[#bfa06a]" />
            </section>
            <section className="bg-[#e9dec7] p-6 rounded-xl shadow border border-[#bfa06a] flex items-center justify-between">
              <div>
                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">상품 평균 가격</h2>
                <p className="text-xl md:text-2xl font-bold text-[#388e3c]">{avgPrice.toLocaleString()}원</p>
              </div>
              <Calculator className="w-8 h-8 text-[#bfa06a]" />
            </section>
            <section className="bg-[#e9dec7] p-6 rounded-xl shadow border border-[#bfa06a] flex items-center justify-between">
              <div>
                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">최고가/최저가</h2>
                <p className="text-xl md:text-2xl font-bold text-[#5b4636]">{maxPrice.toLocaleString()}원 / {minPrice.toLocaleString()}원</p>
              </div>
              <TrendingUp className="w-8 h-8 text-[#bfa06a]" />
            </section>
            <section className="bg-[#e9dec7] p-6 rounded-xl shadow border border-[#bfa06a] flex items-center justify-between">
              <div>
                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">상품 등록</h2>
                <button onClick={handleRegisterClick} className="bg-[#bfa06a] text-[#4b3a2f] px-4 py-2 rounded-md hover:bg-[#5b4636] hover:text-[#e9dec7] flex items-center gap-2 transition-colors">
                  <Plus className="w-4 h-4" /> 상품 등록
                </button>
              </div>
            </section>
          </div>

          {/* 검색/필터 영역 */}
          <div className="flex flex-col md:flex-row gap-3 md:gap-4 mb-6 items-center">
            <input
              type="text"
              placeholder="상품명 검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="flex-1 border border-[#bfa06a] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
            />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="border border-[#bfa06a] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
            >
              <option value="">전체 상태</option>
              <option value="상">상</option>
              <option value="중">중</option>
              <option value="하">하</option>
            </select>
            <button 
              onClick={handleSearch} 
              className="bg-[#bfa06a] text-[#4b3a2f] px-4 py-2 rounded-md hover:bg-[#5b4636] hover:text-[#e9dec7] flex items-center gap-2 transition-colors"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24"><circle cx="11" cy="11" r="8" /><line x1="21" y1="21" x2="16.65" y2="16.65" /></svg>
              검색
            </button>
          </div>

          {/* 상품 리스트 (쇼피파이 스타일 테이블+카드) */}
          <div className="overflow-x-auto bg-[#e9dec7] rounded-xl shadow border border-[#bfa06a]">
            <table className="min-w-full divide-y divide-[#bfa06a]">
              <thead className="bg-[#e9dec7]">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">상품명</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">가격</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">상태</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">재고</th>
                  <th className="px-6 py-3 text-center text-xs font-medium text-[#bfa06a] uppercase tracking-wider">액션</th>
                </tr>
              </thead>
              <tbody className="bg-[#e9dec7] divide-y divide-[#bfa06a]">
                {products.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="text-center py-8 text-[#bfa06a]">상품이 없습니다.</td>
                  </tr>
                ) : (
                  products.map((product) => (
                    <tr key={product.id} className="hover:bg-[#bfa06a] transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap font-semibold text-[#5b4636]">{product.name}</td>
                      <td className="px-6 py-4 whitespace-nowrap text-[#5b4636]">{product.price.toLocaleString()}원</td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`px-2 py-1 rounded text-xs font-bold ${product.status === '상' ? 'bg-green-100 text-green-700' : product.status === '중' ? 'bg-yellow-100 text-yellow-700' : 'bg-red-100 text-red-700'}`}>{product.status}</span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-[#5b4636]">{product.stock ?? 0}개</td>
                      <td className="px-6 py-4 whitespace-nowrap text-center">
                        <button
                          onClick={() => router.push(`/seller/products/${product.id}`)}
                          className="inline-flex items-center gap-1 bg-[#bfa06a] text-[#4b3a2f] px-3 py-1.5 rounded hover:bg-[#5b4636] hover:text-[#e9dec7] text-sm transition-colors"
                        >
                          <Eye className="w-4 h-4" /> 상세 보기
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          {/* 페이지네이션 */}
          {totalPages > 1 && (
            <div className="flex justify-center mt-6 space-x-2">
              {Array.from({ length: totalPages }, (_, i) => (
                <button
                  key={i + 1}
                  onClick={() => goToPage(i + 1)}
                  className={`px-4 py-2 rounded-lg font-bold shadow-sm border text-sm transition-colors
                    ${currentPage === i + 1
                      ? 'bg-[#bfa06a] text-[#4b3a2f] border-[#bfa06a]'
                      : 'bg-[#e9dec7] text-[#5b4636] border-[#bfa06a] hover:bg-[#bfa06a] hover:text-[#4b3a2f]'}
                  `}
                >
                  {i + 1}
                </button>
              ))}
            </div>
          )}
        </div>
      </SellerLayout>
    </>
  );
}
