'use client';

import { useState, useEffect } from 'react';
import { Search, Filter, Package, DollarSign, Calendar, Eye, TrendingUp } from 'lucide-react';
import apiClient from '@/lib/apiClient';
import OwnedProductDetailModal from '@/components/admin/OwnedProductDetailModal';

interface Product {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  status: string;
  categoryName: string;
  createdAt: string;
  imageThumbnailUrl?: string;  // 백엔드 응답과 일치하도록 수정
  imageUrls?: string[];        // 백엔드 응답과 일치하도록 수정
  purchasePrice?: number;
  purchasedAt?: string;
  isAuctioned?: boolean;
  width?: number;
  depth?: number;
  height?: number;
}

interface Category {
  id: number;
  name: string;
  parentId?: number | null;
  parent?: Category | null;
}

// 카테고리 계층 구조 생성 함수
const buildCategoryHierarchy = (categories: Category[]) => {
  const categoryMap = new Map<number, Category>();
  const rootCategories: Category[] = [];
  const childCategories = new Map<number, Category[]>();

  // 모든 카테고리를 맵에 저장
  categories.forEach(category => {
    categoryMap.set(category.id, category);
  });

  // 상위/하위 카테고리 분류
  categories.forEach(category => {
    if (category.parent === null || category.parent === undefined) {
      rootCategories.push(category);
    } else {
      const parentId = category.parent.id;
      if (!childCategories.has(parentId)) {
        childCategories.set(parentId, []);
      }
      childCategories.get(parentId)!.push(category);
    }
  });

  return { rootCategories, childCategories, categoryMap };
};

export default function AdminOwnedProductsPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  // 필터 상태
  const [searchKeyword, setSearchKeyword] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<number | null>(null);
  const [auctionStatus, setAuctionStatus] = useState<string>('all');
  
  // 계층형 카테고리 드롭다운을 위한 상태
  const [selectedParentCategory, setSelectedParentCategory] = useState<number | null>(null);
  const [categoryHierarchy, setCategoryHierarchy] = useState<{
    rootCategories: Category[];
    childCategories: Map<number, Category[]>;
    categoryMap: Map<number, Category>;
  } | null>(null);

  // 모달 상태
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

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
      
      // 카테고리 계층 구조 생성
      const hierarchy = buildCategoryHierarchy(response.data || []);
      setCategoryHierarchy(hierarchy);
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
    setAuctionStatus('all');
    setSelectedParentCategory(null);
    fetchProducts();
  };

  const handleProductClick = (product: Product) => {
    setSelectedProduct(product);
    setIsModalOpen(true);
  };

  const handleModalClose = () => {
    setIsModalOpen(false);
    setSelectedProduct(null);
  };

  const handleAuctionCreated = () => {
    // 경매 등록 후 상품 목록 새로고침
    fetchProducts();
  };

  const getAuctionStatusColor = (isAuctioned: boolean | undefined) => {
    return isAuctioned 
      ? 'bg-blue-500 text-white' 
      : 'bg-yellow-500 text-white';
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
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">매입 상품을 불러오는 중...</p>
          <div className="mt-4 flex justify-center space-x-2">
            <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce"></div>
            <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }}></div>
            <div className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-white">
      <div className="max-w-7xl mx-auto p-6">
        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-4">
              <div className="relative">
                <div className="w-16 h-16 bg-gray-800 rounded-2xl flex items-center justify-center shadow-lg">
                  <TrendingUp className="w-8 h-8 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-bold text-gray-800">
                  관리자 매입 상품
                </h1>
                <p className="text-gray-600 text-lg mt-2">
                  관리자가 매입한 상품들을 조회하고 관리할 수 있습니다.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* 필터 섹션 */}
        <div className="bg-gray-50 rounded-3xl border border-gray-200 p-6 mb-8">
          <div className="flex items-center gap-2 mb-6">
            <Filter className="w-5 h-5 text-gray-600" />
            <h2 className="text-lg font-semibold text-gray-800">필터</h2>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
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
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-xl focus:ring-2 focus:ring-gray-400 focus:border-transparent"
                />
              </div>
            </div>

            {/* 카테고리 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">카테고리</label>
              <div className="space-y-2">
                {/* 상위 카테고리 드롭다운 */}
                <select
                  value={selectedParentCategory || ""}
                  onChange={(e) => {
                    const parentId = e.target.value ? Number(e.target.value) : null;
                    setSelectedParentCategory(parentId);
                    setSelectedCategory(null); // 하위 카테고리 선택 초기화
                  }}
                  className="w-full px-3 py-2 border border-gray-300 rounded-xl focus:ring-2 focus:ring-gray-400 focus:border-transparent"
                >
                  <option value="">전체 카테고리</option>
                  {categoryHierarchy?.rootCategories.map(category => (
                    <option key={category.id} value={category.id}>
                      {category.name}
                    </option>
                  ))}
                </select>
                
                {/* 하위 카테고리 드롭다운 (상위 카테고리 선택 시에만 표시) */}
                {selectedParentCategory && categoryHierarchy?.childCategories.get(selectedParentCategory) && (
                  <select
                    value={selectedCategory || ""}
                    onChange={(e) => setSelectedCategory(e.target.value ? Number(e.target.value) : null)}
                    className="w-full px-3 py-2 border border-gray-300 rounded-xl focus:ring-2 focus:ring-gray-400 focus:border-transparent bg-gray-100"
                  >
                    <option value="">전체 {categoryHierarchy.categoryMap.get(selectedParentCategory)?.name}</option>
                    {categoryHierarchy.childCategories.get(selectedParentCategory)?.map(category => (
                      <option key={category.id} value={category.id}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                )}
                
                {/* 선택된 카테고리 표시 */}
                {selectedCategory && (
                  <div className="text-sm text-gray-600 bg-gray-100 px-3 py-2 rounded-xl">
                    선택된 카테고리: {categories.find(c => c.id === selectedCategory)?.name}
                  </div>
                )}
              </div>
            </div>

            {/* 경매 상태 */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">경매 상태</label>
              <select
                value={auctionStatus}
                onChange={(e) => setAuctionStatus(e.target.value)}
                className="w-full px-3 py-2 border border-gray-300 rounded-xl focus:ring-2 focus:ring-gray-400 focus:border-transparent"
              >
                <option value="all">전체</option>
                <option value="true">등록</option>
                <option value="false">미등록</option>
              </select>
            </div>
          </div>


          {/* 필터 버튼 */}
          <div className="flex gap-3">
            <button
              onClick={handleSearch}
              className="bg-gray-800 text-white px-6 py-2 rounded-xl hover:bg-gray-700 transition-colors flex items-center gap-2"
            >
              <Search className="w-4 h-4" />
              검색
            </button>
            <button
              onClick={clearFilters}
              className="bg-gray-200 text-gray-700 px-6 py-2 rounded-xl hover:bg-gray-300 transition-colors"
            >
              필터 초기화
            </button>
          </div>
        </div>

        {/* 결과 통계 */}
        <div className="mb-8">
          <div className="bg-gray-50 rounded-3xl border border-gray-200 p-6">
            <div className="flex items-center justify-between">
              <div>
                <span className="text-gray-600">총 매입 상품: </span>
                <span className="font-bold text-2xl text-gray-800">{filtered.length}개</span>
              </div>
              <div className="flex gap-6 text-sm text-gray-600">
                <span>경매 등록: {filtered.filter(p => p.isAuctioned === true).length}개</span>
                <span>경매 미등록: {filtered.filter(p => p.isAuctioned === false).length}개</span>
              </div>
            </div>
          </div>
        </div>

        {/* 상품 목록 */}
        {error ? (
          <div className="text-center py-12">
            <div className="w-32 h-32 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-8">
              <Package className="w-16 h-16 text-red-500" />
            </div>
            <h3 className="text-2xl font-bold text-gray-800 mb-4">오류가 발생했습니다</h3>
            <p className="text-gray-600 text-lg">{error}</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {filtered.map((product) => (
              <div key={product.id} className="bg-gray-50 rounded-3xl border border-gray-200 overflow-hidden hover:shadow-lg transition-shadow">
                {/* 상품 이미지 */}
                <div className="aspect-square bg-gray-200 relative">
                  {product.imageThumbnailUrl ? (
                    <img
                      src={product.imageThumbnailUrl}
                      alt={product.name}
                      className="w-full h-full object-cover"
                      onError={(e) => {
                        // 이미지 로드 실패 시 기본 박스로 대체
                        e.currentTarget.style.display = 'none';
                        e.currentTarget.nextElementSibling?.classList.remove('hidden');
                      }}
                    />
                  ) : null}
                  {/* 기본 이미지 박스 */}
                  <div className={`w-full h-full bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center ${product.imageThumbnailUrl ? 'hidden' : ''}`}>
                    <div className="text-center">
                      <Package className="w-12 h-12 text-gray-400 mx-auto mb-2" />
                      <p className="text-gray-500 text-xs">이미지 없음</p>
                    </div>
                  </div>
                  <div className="absolute top-3 right-3">
                    <span className={`px-3 py-1 rounded-full text-xs font-bold ${getAuctionStatusColor(product.isAuctioned)}`}>
                      {product.isAuctioned === true ? '등록' : '미등록'}
                    </span>
                  </div>
                </div>

                {/* 상품 정보 */}
                <div className="p-6">
                  <h3 className="font-bold text-gray-800 mb-3 line-clamp-2 text-lg">{product.name}</h3>
                  
                  <div className="space-y-3 mb-6">
                    <div className="flex items-center gap-3 text-sm">
                      <Package className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-600 w-16">카테고리:</span>
                      <span className="font-medium text-gray-800">{product.categoryName}</span>
                    </div>
                    
                    <div className="flex items-center gap-3 text-sm">
                      <DollarSign className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-600 w-16">매입가:</span>
                      <span className="font-bold text-green-600">
                        {product.purchasePrice ? product.purchasePrice.toLocaleString() : 'N/A'}원
                      </span>
                    </div>
                    
                    <div className="flex items-center gap-3 text-sm">
                      <Calendar className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-600 w-16">매입일:</span>
                      <span className="font-medium text-gray-800">
                        {product.purchasedAt ? new Date(product.purchasedAt).toLocaleDateString() : 'N/A'}
                      </span>
                    </div>

                    {(product.width || product.depth || product.height) && (
                      <div className="flex items-center gap-3 text-sm">
                        <div className="w-4 h-4 flex items-center justify-center">
                          <div className="w-3 h-3 border border-gray-400 rounded-sm"></div>
                        </div>
                        <span className="text-gray-600 w-16">사이즈:</span>
                        <span className="font-medium text-gray-800">
                          {product.width && `${product.width}W`}
                          {product.depth && ` × ${product.depth}D`}
                          {product.height && ` × ${product.height}H`}cm
                        </span>
                      </div>
                    )}
                  </div>

                  <div className="flex gap-3">
                    <button
                      onClick={() => handleProductClick(product)}
                      className="flex-1 bg-gray-800 text-white py-3 px-4 rounded-xl hover:bg-gray-700 transition-colors flex items-center justify-center gap-2 font-medium"
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
            <div className="w-32 h-32 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-8">
              <Package className="w-16 h-16 text-gray-400" />
            </div>
            <h3 className="text-2xl font-bold text-gray-800 mb-4">매입한 상품이 없습니다</h3>
            <p className="text-gray-600 text-lg mb-8">
              검색 조건을 변경하거나 필터를 초기화해보세요.
            </p>
            <button 
              onClick={clearFilters}
              className="bg-gray-800 text-white px-8 py-3 rounded-xl hover:bg-gray-700 transition-colors font-medium"
            >
              필터 초기화
            </button>
          </div>
        )}

        {/* 상품 상세 모달 */}
        <OwnedProductDetailModal
          product={selectedProduct}
          isOpen={isModalOpen}
          onClose={handleModalClose}
          onAuctionCreated={handleAuctionCreated}
        />
      </div>
    </div>
  );
} 