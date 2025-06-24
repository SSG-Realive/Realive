'use client';

import { useState, useEffect } from 'react';
import { Search, Filter, Package, DollarSign, Calendar, Eye } from 'lucide-react';
import apiClient from '@/lib/apiClient';

interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  status: string;
  categoryName: string;
  createdAt: string;
  thumbnailUrl?: string;
  purchasePrice?: number;
  purchasedAt?: string;
  isAuctioned?: boolean;
}

interface Category {
  id: number;
  name: string;
}

export default function AdminOwnedProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // 필터 상태
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [priceRange, setPriceRange] = useState({ min: '', max: '' });
  const [auctionStatus, setAuctionStatus] = useState<string>('all');

  useEffect(() => {
    fetchCategories();
    fetchProducts();
  }, []);

  const fetchCategories = async () => {
    try {
      const token = localStorage.getItem('adminToken');
      const response = await apiClient.get('/admin/categories', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setCategories(response.data);
    } catch (error) {
      console.error('카테고리 조회 실패:', error);
    }
  };

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const token = localStorage.getItem('adminToken');
      
      // 검색 조건 구성
      const params = new URLSearchParams();
      if (searchKeyword) params.append('keyword', searchKeyword);
      if (selectedCategory) params.append('categoryId', selectedCategory.toString());
      if (priceRange.min) params.append('minPrice', priceRange.min);
      if (priceRange.max) params.append('maxPrice', priceRange.max);
      if (auctionStatus !== 'all') params.append('isAuctioned', auctionStatus === 'true' ? 'true' : 'false');
      params.append('size', '100'); // 충분한 데이터 로드

      const response = await apiClient.get(`/admin/owned-products?${params}`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      
      setProducts(response.data.dtoList || []);
      setError(null);
    } catch (error: any) {
      console.error('매입 상품 조회 실패:', error);
      setError('매입 상품 목록을 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = () => {
    fetchProducts();
  };

  const clearFilters = () => {
    setSearchKeyword('');
    setSelectedCategory(null);
    setPriceRange({ min: '', max: '' });
    setAuctionStatus('all');
    fetchProducts();
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ACTIVE': return 'bg-green-100 text-green-800';
      case 'INACTIVE': return 'bg-red-100 text-red-800';
      case 'SOLD_OUT': return 'bg-gray-100 text-gray-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const getAuctionStatusColor = (isAuctioned: boolean | undefined) => {
    return isAuctioned 
      ? 'bg-blue-100 text-blue-800' 
      : 'bg-yellow-100 text-yellow-800';
  };

  const filtered = products.filter(product => {
    if (auctionStatus !== 'all') {
      const isAuctioned = auctionStatus === 'true';
      if (product.isAuctioned !== isAuctioned) return false;
    }
    return true;
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 p-6">
        <div className="max-w-7xl mx-auto">
          <div className="animate-pulse">
            <div className="h-8 bg-gray-200 rounded w-1/4 mb-6"></div>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {[...Array(8)].map((_, i) => (
                <div key={i} className="bg-white rounded-lg shadow p-6">
                  <div className="h-4 bg-gray-200 rounded w-3/4 mb-4"></div>
                  <div className="h-3 bg-gray-200 rounded w-1/2 mb-2"></div>
                  <div className="h-3 bg-gray-200 rounded w-1/3"></div>
                </div>
              ))}
            </div>
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
          <h1 className="text-3xl font-bold text-gray-900 mb-2">관리자 매입 상품</h1>
          <p className="text-gray-600">관리자가 매입한 상품들을 조회하고 관리할 수 있습니다.</p>
        </div>

        {/* 필터 섹션 */}
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <div className="flex items-center gap-2 mb-4">
            <Filter className="w-5 h-5 text-gray-500" />
            <h2 className="text-lg font-semibold text-gray-900">필터</h2>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-4">
            {/* 검색어 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">검색어</label>
              <div className="relative">
                <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
                <input
                  type="text"
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                  placeholder="상품명 검색..."
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>

            {/* 카테고리 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">카테고리</label>
              <select
                value={selectedCategory || ''}
                onChange={(e) => setSelectedCategory(e.target.value ? Number(e.target.value) : null)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">전체 카테고리</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.name}
                  </option>
                ))}
              </select>
            </div>

            {/* 가격 범위 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">가격 범위</label>
              <div className="flex gap-2">
                <input
                  type="number"
                  value={priceRange.min}
                  onChange={(e) => setPriceRange(prev => ({ ...prev, min: e.target.value }))}
                  placeholder="최소"
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
                <span className="self-center text-gray-500">~</span>
                <input
                  type="number"
                  value={priceRange.max}
                  onChange={(e) => setPriceRange(prev => ({ ...prev, max: e.target.value }))}
                  placeholder="최대"
                  className="flex-1 px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>
            </div>

            {/* 경매 상태 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">경매 상태</label>
              <select
                value={auctionStatus}
                onChange={(e) => setAuctionStatus(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="all">전체</option>
                <option value="true">경매 등록됨</option>
                <option value="false">경매 미등록</option>
              </select>
            </div>
          </div>

          {/* 필터 버튼 */}
          <div className="flex gap-3">
            <button
              onClick={handleSearch}
              className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700 transition-colors flex items-center gap-2"
            >
              <Search className="w-4 h-4" />
              검색
            </button>
            <button
              onClick={clearFilters}
              className="bg-gray-500 text-white px-4 py-2 rounded-md hover:bg-gray-600 transition-colors"
            >
              필터 초기화
            </button>
          </div>
        </div>

        {/* 결과 통계 */}
        <div className="mb-6">
          <div className="bg-white rounded-lg shadow p-4">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-gray-600">총 매입 상품: </span>
                <span className="font-semibold text-lg text-blue-600">{filtered.length}개</span>
              </div>
              <div className="flex gap-4 text-sm text-gray-600">
                <span>경매 등록: {filtered.filter(p => p.isAuctioned === true).length}개</span>
                <span>경매 미등록: {filtered.filter(p => p.isAuctioned === false).length}개</span>
              </div>
            </div>
          </div>
        </div>

        {/* 상품 목록 */}
        {error ? (
          <div className="text-center py-12">
            <div className="text-red-500 text-6xl mb-4">⚠️</div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">오류가 발생했습니다</h3>
            <p className="text-gray-600">{error}</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filtered.map((product) => (
              <div key={product.id} className="bg-white rounded-lg shadow overflow-hidden hover:shadow-lg transition-shadow">
                {/* 상품 이미지 */}
                <div className="aspect-square bg-gray-200 relative">
                  {product.thumbnailUrl ? (
                    <img
                      src={product.thumbnailUrl}
                      alt={product.name}
                      className="w-full h-full object-cover"
                    />
                  ) : (
                    <div className="w-full h-full flex items-center justify-center">
                      <Package className="w-12 h-12 text-gray-400" />
                    </div>
                  )}
                  <div className="absolute top-2 right-2">
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getAuctionStatusColor(product.isAuctioned)}`}>
                      {product.isAuctioned === true ? '경매 등록' : '미등록'}
                    </span>
                  </div>
                </div>

                {/* 상품 정보 */}
                <div className="p-4">
                  <h3 className="font-semibold text-gray-900 mb-2 line-clamp-2">{product.name}</h3>
                  
                  <div className="space-y-2 mb-4">
                    <div className="flex items-center gap-2 text-sm">
                      <Package className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-600">{product.categoryName}</span>
                    </div>
                    
                    <div className="flex items-center gap-2 text-sm">
                      <DollarSign className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-600">매입가:</span>
                      <span className="font-medium text-blue-600">
                        {product.purchasePrice ? product.purchasePrice.toLocaleString() : 'N/A'}원
                      </span>
                    </div>
                    
                    <div className="flex items-center gap-2 text-sm">
                      <Calendar className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-600">매입일:</span>
                      <span className="font-medium">
                        {product.purchasedAt ? new Date(product.purchasedAt).toLocaleDateString() : 'N/A'}
                      </span>
                    </div>
                  </div>

                  <div className="flex gap-2">
                    <button
                      className="flex-1 bg-blue-600 text-white py-2 px-3 rounded-md hover:bg-blue-700 transition-colors flex items-center justify-center gap-1 text-sm"
                    >
                      <Eye className="w-4 h-4" />
                      상세 보기
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* 결과가 없을 때 */}
        {filtered.length === 0 && !loading && !error && (
          <div className="text-center py-12">
            <div className="text-gray-400 text-6xl mb-4">📦</div>
            <h3 className="text-lg font-medium text-gray-900 mb-2">매입한 상품이 없습니다</h3>
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
  );
} 