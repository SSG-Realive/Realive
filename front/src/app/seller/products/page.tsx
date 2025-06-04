'use client';

import { useEffect, useState } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Header from '@/components/Header';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

import { getMyProducts } from '@/service/productService';
import { ProductListItem } from '@/types/productList';

export default function ProductListPage() {
  const checking = useSellerAuthGuard();
    if (checking) return <div className="p-8">인증 확인 중...</div>;

  const router = useRouter();
  const searchParams = useSearchParams();

  const [products, setProducts] = useState<ProductListItem[]>([]);
  const [totalPages, setTotalPages] = useState(1);
  const [currentPage, setCurrentPage] = useState(1);

  const [keyword, setKeyword] = useState('');
  const [statusFilter, setStatusFilter] = useState('');

  useEffect(() => {
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
    fetchProductList(1);
    router.push(`/seller/products?page=1`);
  };

  const handleRegisterClick = () => {
    router.push('/seller/products/new');
  };

  return (
    <>
      <Header />
      <SellerLayout>
        <div className="max-w-5xl mx-auto p-6">
          <h1 className="text-2xl font-bold mb-4">내 상품 목록</h1>

          {/* 🔍 검색 필터 */}
          <div className="flex gap-4 mb-4">
            <input
              type="text"
              placeholder="상품명 검색"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="border p-2 w-1/3"
            />
            <select
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              className="border p-2 w-1/4"
            >
              <option value="">전체 상태</option>
              <option value="상">상</option>
              <option value="중">중</option>
              <option value="하">하</option>
            </select>
            <button onClick={handleSearch} className="bg-blue-600 text-white px-4 py-2 rounded">
              검색
            </button>
            <button onClick={handleRegisterClick} className="ml-auto bg-green-600 text-white px-4 py-2 rounded">
              상품 등록
            </button>
          </div>

          {/* 📋 목록 */}
          <div className="grid gap-4">
            {products.map((product) => (
              <div key={product.id} className="border p-4 rounded">
                <h2 className="font-semibold">{product.name}</h2>
                <p>가격: {product.price.toLocaleString()}원</p>
                <p>상태: {product.status}</p>
                <button
                  onClick={() => router.push(`/seller/products/${product.id}`)}
                  className="mt-2 px-4 py-1 bg-blue-600 text-white rounded"
                >
                  상세 보기
                </button>
              </div>
            ))}
          </div>

          {/* 페이지네이션 */}
          <div className="flex justify-center mt-6 space-x-2">
            {Array.from({ length: totalPages }, (_, i) => (
              <button
                key={i + 1}
                onClick={() => goToPage(i + 1)}
                className={`px-3 py-1 border rounded ${
                  currentPage === i + 1 ? 'bg-blue-600 text-white' : 'bg-white text-gray-800'
                }`}
              >
                {i + 1}
              </button>
            ))}
          </div>
        </div>
      </SellerLayout>
    </>
  );
}
