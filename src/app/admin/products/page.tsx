"use client";
import React, { useState, useEffect } from "react";
import { Package, DollarSign, Eye, Users, ShoppingCart, Filter, Search, X } from "lucide-react";
import apiClient from "@/lib/apiClient";
import Link from 'next/link';
import { useRouter } from 'next/navigation';

interface ProductStats {
  totalProducts: number;
  sellerProducts: number;
  adminProducts: number;
  highQualityProducts: number;    // 상
  mediumQualityProducts: number;  // 중
  lowQualityProducts: number;     // 하
  totalValue: number;
  averagePrice: number;
}

interface Product {
  id: number;
  name: string;
  price: number;
  stock: number;
  status: string;
  categoryName: string;
  productImages?: string[];
  isActive: boolean;
}

interface Category {
  id: number;
  name: string;
  parentId?: number;
}

interface FilterOptions {
  category: string;
  status: string;
  priceRange: string;
  stockRange: string;
}

export default function ProductDashboardPage() {
  const [stats, setStats] = useState<ProductStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showFilters, setShowFilters] = useState(false);
  const [filterOptions, setFilterOptions] = useState<FilterOptions>({
    category: "",
    status: "",
    priceRange: "",
    stockRange: ""
  });
  
  // 페이징 관련 state 추가
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [pageSize] = useState(10);
  
  // 계층형 카테고리 드롭다운을 위한 상태
  const [selectedParentCategory, setSelectedParentCategory] = useState<number | null>(null);
  const [categoryHierarchy, setCategoryHierarchy] = useState<{
    rootCategories: Category[];
    childCategories: Map<number, Category[]>;
    categoryMap: Map<number, Category>;
  } | null>(null);
  
  const router = useRouter();
  const [filteredProducts, setFilteredProducts] = useState<Product[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  useEffect(() => {
    fetchProductStats();
    fetchProducts();
  }, []);

  const fetchProductStats = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('adminToken');
      
      // 전체 상품 통계
      const allProductsRes = await apiClient.get('/admin/products?size=1000', {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      // 관리자 매입 상품 통계
      const adminProductsRes = await apiClient.get('/admin/owned-products?size=1000', {
        headers: { Authorization: `Bearer ${token}` }
      });

      const allProducts = allProductsRes.data.dtoList || [];
      const adminProducts = adminProductsRes.data.dtoList || [];
      
      console.log('전체 상품:', allProducts);
      console.log('관리자 매입 상품:', adminProducts);
      
      // 첫 번째 상품의 구조 확인
      if (allProducts.length > 0) {
        console.log('첫 번째 상품 구조:', allProducts[0]);
        console.log('isActive 필드:', allProducts[0].isActive);
        console.log('status 필드:', allProducts[0].status);
      }
      
      if (adminProducts.length > 0) {
        console.log('첫 번째 관리자 상품 구조:', adminProducts[0]);
        console.log('isAuctioned 필드:', adminProducts[0].isAuctioned);
      }
      
      // 판매자 상품 = 전체 상품 (관리자 매입 상품은 별도로 계산)
      const sellerProducts = allProducts;
      const adminProductsCount = adminProducts.length;
      
      // 전체 상품에서 상태별 통계 계산 (관리자 매입 상품 제외)
      const highQualityProducts = sellerProducts.filter((p: any) => p.status === "상").length;
      const mediumQualityProducts = sellerProducts.filter((p: any) => p.status === "중").length;
      const lowQualityProducts = sellerProducts.filter((p: any) => p.status === "하").length;
      
      console.log('상태별 통계:', {
        highQualityProducts,
        mediumQualityProducts,
        lowQualityProducts,
        totalProducts: sellerProducts.length
      });
      
      // 가격 통계 계산
      const totalValue = sellerProducts.reduce((sum: number, p: any) => sum + (p.price || 0), 0);
      const averagePrice = sellerProducts.length > 0 ? totalValue / sellerProducts.length : 0;

      const statsData = {
        totalProducts: sellerProducts.length,
        sellerProducts: sellerProducts.length,
        adminProducts: adminProductsCount,
        highQualityProducts,
        mediumQualityProducts,
        lowQualityProducts,
        totalValue,
        averagePrice
      };
      
      console.log('계산된 통계:', statsData);
      setStats(statsData);
      
      setError(null);
    } catch (error: any) {
      console.error('상품 통계 조회 실패:', error);
      setError('상품 통계를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const fetchProducts = async () => {
    try {
      const token = localStorage.getItem('adminToken');
      const response = await apiClient.get('/admin/products', {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      const products = response.data.dtoList || [];
      setFilteredProducts(products);
      setTotalPages(Math.ceil(products.length / pageSize));
    } catch (error) {
      console.error('상품 목록 조회 실패:', error);
    }
  };

  // 상태별 색상 반환 함수
  const getStatusColor = (status: string) => {
    switch (status) {
      case '상': return 'bg-green-100 text-green-800';
      case '중': return 'bg-yellow-100 text-yellow-800';
      case '하': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  // 필터 적용 함수
  const applyFilters = () => {
    // 실제 필터링 로직 구현
    console.log('필터 적용:', filterOptions);
  };

  // 필터 초기화 함수
  const clearFilters = () => {
    setFilterOptions({
      category: "",
      status: "",
      priceRange: "",
      stockRange: ""
    });
    setShowFilters(false);
  };

  // 상품 상세 보기 함수
  const handleQuickView = (product: Product) => {
    setSelectedProduct(product);
    setIsModalOpen(true);
  };

  // 페이징된 상품 목록 계산
  const paginatedProducts = filteredProducts.slice((currentPage - 1) * pageSize, currentPage * pageSize);

  // 페이지 변경 핸들러
  const handlePageChange = (page: number) => {
    setCurrentPage(page);
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-1/4 mb-6"></div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
              {[...Array(4)].map((_, i) => (
                <div key={i} className="bg-white rounded-lg shadow p-6">
                  <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
                  <div className="h-8 bg-gray-200 rounded w-1/2"></div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="text-center py-12">
            <div className="text-red-500 text-6xl mb-4">⚠️</div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">오류가 발생했습니다</h3>
            <p className="text-gray-600">{error}</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-7xl mx-auto">
        {/* 헤더 */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">상품 관리</h1>
          <p className="text-gray-600">전체 상품 현황과 통계를 확인할 수 있습니다.</p>
        </div>

        {/* 통계 카드 */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {/* 전체 상품 */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-lg font-semibold text-gray-900 mb-2">전체 상품</p>
                <p className="text-2xl font-bold text-gray-900">{stats?.totalProducts.toLocaleString()}</p>
              </div>
              <div className="p-3 bg-blue-100 rounded-full">
                <Package className="w-6 h-6 text-blue-600" />
              </div>
            </div>
            <div className="mt-4">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">판매자 상품</span>
                <span className="font-medium">{stats?.sellerProducts.toLocaleString()}</span>
              </div>
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">관리자 매입</span>
                <span className="font-medium">{stats?.adminProducts.toLocaleString()}</span>
              </div>
            </div>
          </div>

          {/* 품질: 상 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div>
              <p className="text-lg font-semibold text-gray-900 mb-2">품질: 상</p>
              <p className="text-2xl font-bold text-green-600">{stats?.highQualityProducts.toLocaleString()}</p>
            </div>
            <div className="p-3 bg-green-100 rounded-full">
              <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>
          </div>

          {/* 품질: 중 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div>
              <p className="text-lg font-semibold text-gray-900 mb-2">품질: 중</p>
              <p className="text-2xl font-bold text-yellow-600">{stats?.mediumQualityProducts.toLocaleString()}</p>
            </div>
            <div className="p-3 bg-yellow-100 rounded-full">
              <svg className="w-6 h-6 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
              </svg>
            </div>
          </div>

          {/* 품질: 하 */}
          <div className="bg-white p-6 rounded-lg shadow">
            <div>
              <p className="text-lg font-semibold text-gray-900 mb-2">품질: 하</p>
              <p className="text-2xl font-bold text-red-600">{stats?.lowQualityProducts.toLocaleString()}</p>
            </div>
            <div className="p-3 bg-red-100 rounded-full">
              <svg className="w-6 h-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
          </div>

          {/* 총 상품 가치 */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-lg font-semibold text-gray-900 mb-2">총 상품 가치</p>
                <p className="text-2xl font-bold text-purple-600">
                  {stats?.totalValue.toLocaleString()}원
                </p>
              </div>
              <div className="p-3 bg-purple-100 rounded-full">
                <DollarSign className="w-6 h-6 text-purple-600" />
              </div>
            </div>
            <div className="mt-4">
              <div className="flex justify-between text-sm">
                <span className="text-gray-600">평균 가격</span>
                <span className="font-medium">{stats?.averagePrice.toLocaleString()}원</span>
              </div>
            </div>
          </div>

          {/* 상품 상태 분포 */}
          <div className="bg-white rounded-lg shadow p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-lg font-semibold text-gray-900 mb-2">상품 상태 분포</p>
              </div>
              <div className="p-3 bg-blue-100 rounded-full">
                <Eye className="w-6 h-6 text-blue-600" />
              </div>
            </div>
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-green-500 rounded-full"></div>
                  <span className="text-sm text-gray-600">상품질</span>
                </div>
                <span className="font-medium text-green-600">
                  {stats?.totalProducts ? Math.round((stats.highQualityProducts / stats.totalProducts) * 100) : 0}%
                </span>
              </div>
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-yellow-500 rounded-full"></div>
                  <span className="text-sm text-gray-600">중품질</span>
                </div>
                <span className="font-medium text-yellow-600">
                  {stats?.totalProducts ? Math.round((stats.mediumQualityProducts / stats.totalProducts) * 100) : 0}%
                </span>
              </div>
              <div className="flex justify-between items-center">
                <div className="flex items-center gap-2">
                  <div className="w-3 h-3 bg-red-500 rounded-full"></div>
                  <span className="text-sm text-gray-600">하품질</span>
                </div>
                <span className="font-medium text-red-600">
                  {stats?.totalProducts ? Math.round((stats.lowQualityProducts / stats.totalProducts) * 100) : 0}%
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* 상품 목록 섹션 */}
        <div className="bg-white rounded-lg shadow">
          {/* 상품 목록 헤더 */}
          <div className="p-6 border-b border-gray-200">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-semibold text-gray-900">상품 목록</h2>
              <div className="flex gap-2">
                <button
                  onClick={() => setShowFilters(!showFilters)}
                  className="flex items-center gap-2 px-4 py-2 bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 transition-colors"
                >
                  <Filter className="w-4 h-4" />
                  필터
                </button>
                <Link
                  href="/admin/products/new"
                  className="flex items-center gap-2 px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                >
                  <Package className="w-4 h-4" />
                  상품 등록
                </Link>
              </div>
            </div>
          </div>

          {/* 필터 섹션 */}
          {showFilters && (
            <div className="p-6 border-b border-gray-200 bg-gray-50">
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">카테고리</label>
                  <select
                    value={filterOptions.category}
                    onChange={(e) => setFilterOptions({...filterOptions, category: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">전체</option>
                    <option value="가구">가구</option>
                    <option value="전자제품">전자제품</option>
                    <option value="의류">의류</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">상태</label>
                  <select
                    value={filterOptions.status}
                    onChange={(e) => setFilterOptions({...filterOptions, status: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">전체</option>
                    <option value="상">상</option>
                    <option value="중">중</option>
                    <option value="하">하</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">가격 범위</label>
                  <select
                    value={filterOptions.priceRange}
                    onChange={(e) => setFilterOptions({...filterOptions, priceRange: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">전체</option>
                    <option value="0-50000">5만원 이하</option>
                    <option value="50000-100000">5-10만원</option>
                    <option value="100000-500000">10-50만원</option>
                    <option value="500000+">50만원 이상</option>
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">재고 범위</label>
                  <select
                    value={filterOptions.stockRange}
                    onChange={(e) => setFilterOptions({...filterOptions, stockRange: e.target.value})}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  >
                    <option value="">전체</option>
                    <option value="0">품절</option>
                    <option value="1-5">1-5개</option>
                    <option value="5+">5개 이상</option>
                  </select>
                </div>
              </div>
              <div className="flex gap-2 mt-4">
                <button
                  onClick={applyFilters}
                  className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
                >
                  필터 적용
                </button>
                <button
                  onClick={clearFilters}
                  className="px-4 py-2 bg-gray-500 text-white rounded-md hover:bg-gray-600 transition-colors"
                >
                  초기화
                </button>
              </div>
            </div>
          )}

          {/* 상품 목록 */}
          <div className="p-6">
            {/* 데스크탑 상품 카드 그리드 */}
            <div className="hidden md:grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {paginatedProducts.map((product) => (
                <div key={product.id} className="bg-white rounded-lg shadow-md overflow-hidden hover:shadow-lg transition-shadow">
                  {/* 상품 이미지 */}
                  <div className="aspect-square bg-gray-100 relative">
                    {product.productImages && product.productImages.length > 0 ? (
                      <img
                        src={product.productImages[0]}
                        alt={product.name}
                        className="w-full h-full object-cover"
                      />
                    ) : (
                      <div className="w-full h-full flex items-center justify-center text-gray-400">
                        <Package className="w-12 h-12" />
                      </div>
                    )}
                    
                    {/* 상태 배지 */}
                    <div className="absolute top-2 right-2">
                      <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(product.status)}`}>
                        {product.status}
                      </span>
                    </div>
                  </div>

                  {/* 상품 정보 */}
                  <div className="p-4">
                    <h3 className="font-semibold text-gray-900 mb-2 line-clamp-2">
                      {product.name}
                    </h3>
                    
                    <div className="space-y-2 text-sm text-gray-600 mb-4">
                      <div className="flex items-center gap-1">
                        <Package className="w-3 h-3" />
                        <span className="truncate">{product.categoryName}</span>
                      </div>
                      <div className="flex items-center gap-1">
                        <DollarSign className="w-3 h-3" />
                        <span className="font-medium text-blue-600">
                          {product.price.toLocaleString()}원
                        </span>
                      </div>
                      <div className="flex items-center gap-1">
                        <ShoppingCart className="w-3 h-3" />
                        <span className={`font-medium ${
                          product.stock === 0 ? 'text-red-600' : 
                          product.stock === 1 ? 'text-orange-600' : 
                          product.stock <= 2 ? 'text-yellow-600' : 'text-green-600'
                        }`}>
                          재고: {product.stock}개
                        </span>
                      </div>
                    </div>

                    {/* 액션 버튼 */}
                    <button
                      onClick={() => handleQuickView(product)}
                      className="w-full bg-blue-600 text-white py-2 px-3 rounded-md hover:bg-blue-700 transition-colors flex items-center justify-center gap-1 text-sm"
                    >
                      <Eye className="w-4 h-4" />
                      상세 보기
                    </button>
                  </div>
                </div>
              ))}
            </div>

            {/* 모바일 카드형 리스트 */}
            <div className="block md:hidden space-y-4">
              {paginatedProducts.map((product, idx) => (
                <div key={product.id} className="bg-white rounded-lg shadow-md p-4">
                  <div className="flex items-start gap-4">
                    {/* 상품 이미지 */}
                    <div className="w-20 h-20 bg-gray-100 rounded-lg flex-shrink-0 relative">
                      {product.productImages && product.productImages.length > 0 ? (
                        <img
                          src={product.productImages[0]}
                          alt={product.name}
                          className="w-full h-full object-cover rounded-lg"
                        />
                      ) : (
                        <div className="w-full h-full flex items-center justify-center text-gray-400">
                          <Package className="w-8 h-8" />
                        </div>
                      )}
                      <div className="absolute top-1 right-1">
                        <span className={`px-1 py-0.5 rounded-full text-xs font-medium ${getStatusColor(product.status)}`}>
                          {product.status}
                        </span>
                      </div>
                    </div>

                    {/* 상품 정보 */}
                    <div className="flex-1 min-w-0">
                      <h3 className="font-semibold text-gray-900 mb-1 line-clamp-2">
                        {product.name}
                      </h3>
                      
                      <div className="space-y-1 text-sm text-gray-600 mb-3">
                        <div className="flex items-center gap-1">
                          <Package className="w-3 h-3" />
                          <span className="truncate">{product.categoryName}</span>
                        </div>
                        <div className="flex items-center gap-1">
                          <DollarSign className="w-3 h-3" />
                          <span className="font-medium text-blue-600">
                            {product.price.toLocaleString()}원
                          </span>
                        </div>
                        <div className="flex items-center gap-1">
                          <ShoppingCart className="w-3 h-3" />
                          <span className={`font-medium ${
                            product.stock === 0 ? 'text-red-600' : 
                            product.stock === 1 ? 'text-orange-600' : 
                            product.stock <= 2 ? 'text-yellow-600' : 'text-green-600'
                          }`}>
                            재고: {product.stock}개
                          </span>
                        </div>
                      </div>

                      {/* 액션 버튼 */}
                      <button
                        onClick={() => handleQuickView(product)}
                        className="w-full bg-blue-600 text-white py-2 px-3 rounded-md hover:bg-blue-700 transition-colors flex items-center justify-center gap-1 text-sm"
                      >
                        <Eye className="w-4 h-4" />
                        상세 보기
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* 페이징 */}
            {Math.ceil(filteredProducts.length / pageSize) > 1 && (
              <div className="mt-8 flex justify-center">
                <div className="flex space-x-2">
                  <button
                    onClick={() => handlePageChange(currentPage - 1)}
                    disabled={currentPage === 1}
                    className="px-3 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                  >
                    이전
                  </button>
                  
                  {Array.from({ length: Math.min(5, Math.ceil(filteredProducts.length / pageSize)) }, (_, i) => {
                    const pageNum = Math.max(1, Math.min(Math.ceil(filteredProducts.length / pageSize) - 4, currentPage - 2)) + i;
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
                    disabled={currentPage === Math.ceil(filteredProducts.length / pageSize)}
                    className="px-3 py-2 border rounded disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50"
                  >
                    다음
                  </button>
                </div>
              </div>
            )}

            {/* 결과가 없을 때 */}
            {filteredProducts.length === 0 && (
              <div className="text-center py-12">
                <div className="text-gray-400 text-6xl mb-4">📦</div>
                <h3 className="text-lg font-medium text-gray-900 mb-2">상품을 찾을 수 없습니다</h3>
                <p className="text-gray-600 mb-4">
                  검색 조건을 변경하거나 필터를 초기화해보세요.
                </p>
                <button 
                  onClick={clearFilters}
                  className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors"
                >
                  필터 초기화
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}