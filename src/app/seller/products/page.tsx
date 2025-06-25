'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

import { getMyProducts } from '@/service/seller/productService';
import { ProductListItem } from '@/types/seller/product/productList';

export default function ProductListPage() {
  const checking = useSellerAuthGuard();
   

  const router = useRouter();
  const searchParams = useSearchParams();

  const [products, setProducts] = useState<ProductListItem[]>([]);
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
        <div className="flex-1 w-full h-full px-4 py-8 bg-gray-100">
          <h1 className="text-xl md:text-2xl font-bold mb-4 md:mb-6">내 상품 목록</h1>

          {/* 🔍 검색 필터 */}
          <div className="flex flex-col md:flex-row gap-3 md:gap-4 mb-4 md:mb-6">
            <input
              type="text"
              placeholder="상품명 검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="flex-1 border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="border border-gray-300 rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              <option value="">전체 상태</option>
              <option value="상">상</option>
              <option value="중">중</option>
              <option value="하">하</option>
            </select>
            <button 
              onClick={handleSearch} 
              className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
            >
              검색
            </button>
            <button 
              onClick={handleRegisterClick} 
              className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 transition-colors"
            >
              상품 등록
            </button>
          </div>

          {/* 📋 목록 */}
          <div className="grid gap-4">
            {products.map((product) => (
              <div key={product.id} className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm hover:shadow-md transition-shadow">
                <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                  <div className="flex-1">
                    <h2 className="font-semibold text-lg text-gray-800 mb-2">{product.name}</h2>
                    <div className="grid grid-cols-2 md:grid-cols-3 gap-2 text-sm text-gray-600">
                      <p>가격: <span className="font-medium">{product.price.toLocaleString()}원</span></p>
                      <p>상태: <span className="font-medium">{product.status}</span></p>
                    </div>
                  </div>
                <button
                  onClick={() => router.push(`/seller/products/${product.id}`)}
                    className="w-full md:w-auto bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                >
                  상세 보기
                </button>
                </div>
              </div>
            ))}
          </div>

          {/* 페이지네이션 */}
          {totalPages > 1 && (
          <div className="flex justify-center mt-6 space-x-2">
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i + 1}
                onClick={() => goToPage(i + 1)}
                  className={`px-3 py-2 border rounded-md text-sm ${
                    currentPage === i + 1 
                      ? 'bg-blue-600 text-white border-blue-600' 
                      : 'bg-white text-gray-800 border-gray-300 hover:bg-gray-50'
                  } focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors`}
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
