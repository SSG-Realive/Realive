"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Search, Filter, Eye, Package, DollarSign, Calendar, User, ShoppingCart, X, ChevronDown, Sparkles, ChevronLeft, ChevronRight } from "lucide-react";
import { adminApi } from "@/lib/apiClient";
import ProductDetailModal from "@/components/admin/ProductDetailModal";

// CSS 스타일 추가
const styles = `
  .line-clamp-2 {
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
  
  .card-hover {
    transition: all 0.2s ease-in-out;
  }
  
  .card-hover:hover {
    transform: translateY(-2px);
    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  }
  
  .status-badge {
    backdrop-filter: blur(8px);
    background-color: rgba(255, 255, 255, 0.9);
  }
`;

interface Product {
  id: number;
  name: string;
  categoryId: number;
  categoryName: string;
  price: number;
  stock: number;
  createdAt: string;
  status: "상" | "중" | "하";
  imageThumbnailUrl?: string;  // 백엔드 응답과 일치하도록 수정
  imageUrls?: string[];        // 백엔드 응답과 일치하도록 수정
  description?: string;
  sellerName?: string;
  width?: number;
  depth?: number;
  height?: number;
}

interface Category {
  id: number;
  name: string;
  xparentId: number | null;
  parent?: Category | null;
}

interface FilterOptions {
  category: string;
  status: string;
  priceRange: string;
  stockRange: string;
}

// 카테고리 계층 구조 생성 함수
const buildCategoryHierarchy = (categories: Category[]) => {
  const categoryMap = new Map<number, Category>();
  const rootCategories: Category[] = [];
  const childCategories = new Map<number, Category[]>();

  categories.forEach(category => {
    categoryMap.set(category.id, category);
  });

  categories.forEach(category => {
    if (category.xparentId === null) {
      rootCategories.push(category);
    } else {
      const parentId = category.xparentId;
      if (!childCategories.has(parentId)) {
        childCategories.set(parentId, []);
      }
      childCategories.get(parentId)!.push(category);
    }
  });

  return {
    rootCategories,
    childCategories,
    categoryMap
  };
};

// 재고 범위 옵션 추가
const stockRangeOptions = [
  { value: "", label: "전체" },
  { value: "1", label: "단일 상품 (1개)" },
  { value: "2+", label: "다중 재고 (2개 이상)" },
  { value: "0", label: "품절" }
];

const ITEMS_PER_PAGE = 12;

export default function ProductManagementPage() {
  const [products, setProducts] = useState<Product[]>([]);
  const [filtered, setFiltered] = useState<Product[]>([]);
  const [displayed, setDisplayed] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedProduct, setSelectedProduct] = useState<Product | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [search, setSearch] = useState("");
  const [selectedParentCategory, setSelectedParentCategory] = useState<number | null>(null);
  const [categoryHierarchy, setCategoryHierarchy] = useState<ReturnType<typeof buildCategoryHierarchy> | null>(null);
  const [currentPage, setCurrentPage] = useState(1);

  const [filterOptions, setFilterOptions] = useState<FilterOptions>({
    category: "",
    status: "",
    priceRange: "",
    stockRange: ""
  });

  const router = useRouter();

  // 데이터 로딩
  const fetchData = async () => {
    try {
      setLoading(true);
      const [productsRes, categoriesRes] = await Promise.all([
        adminApi.get('/admin/products?size=1000'),
        adminApi.get('/admin/categories')
      ]);

      const productsData = productsRes.data.dtoList || [];
      const categoriesData = categoriesRes.data || [];

      setProducts(productsData);
      setCategories(categoriesData);
      setCategoryHierarchy(buildCategoryHierarchy(categoriesData));
    } catch (error) {
      console.error('데이터 로딩 실패:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // 필터링 로직
  useEffect(() => {
    let filteredProducts = [...products];

    // 기본적으로 품절 상품 제외 (재고 상태 필터가 "품절"이 아닌 경우)
    if (filterOptions.stockRange !== "0") {
      filteredProducts = filteredProducts.filter(product => product.stock > 0);
    }

    // 검색 필터
    if (search.trim()) {
      filteredProducts = filteredProducts.filter(product =>
        product.name.toLowerCase().includes(search.toLowerCase()) ||
        product.categoryName.toLowerCase().includes(search.toLowerCase())
      );
    }

    // 카테고리 필터
    if (filterOptions.category) {
      filteredProducts = filteredProducts.filter(product =>
        product.categoryName === filterOptions.category
      );
    }

    // 상태 필터
    if (filterOptions.status) {
      filteredProducts = filteredProducts.filter(product =>
        product.status === filterOptions.status
      );
    }

    // 가격 범위 필터
    if (filterOptions.priceRange) {
      const [min, max] = filterOptions.priceRange.split('-').map(Number);
      filteredProducts = filteredProducts.filter(product => {
        if (max) {
          return product.price >= min && product.price < max;
        } else {
          return product.price >= min;
        }
      });
    }

    // 재고 범위 필터
    if (filterOptions.stockRange) {
      if (filterOptions.stockRange === "1") {
        filteredProducts = filteredProducts.filter(product => product.stock === 1);
      } else if (filterOptions.stockRange === "2+") {
        filteredProducts = filteredProducts.filter(product => product.stock >= 2);
      } else if (filterOptions.stockRange === "0") {
        filteredProducts = filteredProducts.filter(product => product.stock === 0);
      }
      // "" (전체)인 경우는 이미 위에서 품절 제외 처리됨
    }

    setFiltered(filteredProducts);
    setCurrentPage(1); // 필터 변경 시 첫 페이지로 이동
  }, [products, search, filterOptions]);

  // 페이징 처리
  useEffect(() => {
    const startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
    const endIndex = startIndex + ITEMS_PER_PAGE;
    setDisplayed(filtered.slice(startIndex, endIndex));
  }, [filtered, currentPage]);

  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target;
    setFilterOptions(prev => ({ ...prev, [name]: value }));
  };

  const clearFilters = () => {
    setSearch("");
    setFilterOptions({
      category: "",
      status: "",
      priceRange: "",
      stockRange: ""
    });
    setSelectedParentCategory(null);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case '상': return 'bg-green-500 text-white';
      case '중': return 'bg-yellow-500 text-white';
      case '하': return 'bg-red-500 text-white';
      default: return 'bg-gray-500 text-white';
    }
  };

  const handleQuickView = (product: Product) => {
    setSelectedProduct(product);
    setIsModalOpen(true);
  };

  const handlePurchase = async (productId: number, purchasePrice: number, quantity: number) => {
    try {
      await adminApi.post(`/admin/products/purchase`, {
        productId,
        purchasePrice
      });
      fetchData(); // 데이터 새로고침
      setIsModalOpen(false);
    } catch (error) {
      console.error('구매 처리 실패:', error);
    }
  };

  const totalPages = Math.ceil(filtered.length / ITEMS_PER_PAGE);
  const startItem = (currentPage - 1) * ITEMS_PER_PAGE + 1;
  const endItem = Math.min(currentPage * ITEMS_PER_PAGE, filtered.length);

  if (loading) {
    return (
      <div className="min-h-screen bg-white flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">상품 목록을 불러오는 중...</p>
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
                  <Package className="w-8 h-8 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-bold text-gray-800">
                  상품 관리
                </h1>
                <p className="text-gray-600 mt-1">전체 상품을 관리하고 모니터링하세요</p>
              </div>
            </div>
            <div className="flex items-center space-x-4">
              <div className="bg-gray-50 rounded-2xl px-6 py-3 border border-gray-200">
                <div className="text-gray-600 text-sm">총 상품</div>
                <div className="text-2xl font-bold text-gray-800">{products.length}개</div>
              </div>
              <div className="bg-gray-50 rounded-2xl px-6 py-3 border border-gray-200">
                <div className="text-gray-600 text-sm">재고 있음</div>
                <div className="text-2xl font-bold text-green-600">{products.filter(p => p.stock > 0).length}개</div>
              </div>
            </div>
          </div>

          {/* 검색 및 필터 섹션 */}
          <div className="bg-gray-50 rounded-3xl p-6 border border-gray-200">
            {/* 검색바 */}
            <div className="relative mb-6">
              <div className="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none">
                <Search className="w-5 h-5 text-gray-400" />
              </div>
              <input
                type="text"
                placeholder="상품명이나 카테고리로 검색..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="w-full pl-12 pr-4 py-4 bg-white border border-gray-300 rounded-2xl text-gray-800 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent transition-all duration-300"
              />
              {search && (
                <button
                  onClick={() => setSearch("")}
                  className="absolute inset-y-0 right-0 pr-4 flex items-center text-gray-400 hover:text-gray-600 transition-colors"
                >
                  <X className="w-5 h-5" />
                </button>
              )}
            </div>

            {/* 필터 섹션 */}
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              {/* 카테고리 필터 */}
              <div className="space-y-3">
                <label className="block text-sm font-semibold text-gray-700">카테고리</label>
                <div className="space-y-2">
                  <select
                    value={selectedParentCategory || ""}
                    onChange={(e) => {
                      const parentId = e.target.value ? Number(e.target.value) : null;
                      setSelectedParentCategory(parentId);
                      setFilterOptions(prev => ({ ...prev, category: "" }));
                    }}
                    className="w-full px-4 py-3 bg-white border border-gray-300 rounded-xl text-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent transition-all duration-300"
                  >
                    <option value="">전체 카테고리</option>
                    {categoryHierarchy?.rootCategories.map(category => (
                      <option key={category.id} value={category.id}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                  
                  {selectedParentCategory && categoryHierarchy?.childCategories.get(selectedParentCategory) && (
                    <select
                      name="category"
                      value={filterOptions.category}
                      onChange={handleFilterChange}
                      className="w-full px-4 py-3 bg-gray-50 border border-gray-300 rounded-xl text-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent transition-all duration-300"
                    >
                      <option value="">전체 {categoryHierarchy.categoryMap.get(selectedParentCategory)?.name}</option>
                      {categoryHierarchy.childCategories.get(selectedParentCategory)?.map(category => (
                        <option key={category.id} value={category.name}>
                          {category.name}
                        </option>
                      ))}
                    </select>
                  )}
                </div>
              </div>

              {/* 상태 필터 */}
              <div className="space-y-3">
                <label className="block text-sm font-semibold text-gray-700">품질 상태</label>
                <select
                  name="status"
                  value={filterOptions.status}
                  onChange={handleFilterChange}
                  className="w-full px-4 py-3 bg-white border border-gray-300 rounded-xl text-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent transition-all duration-300"
                >
                  <option value="">전체 상태</option>
                  <option value="상">상품질</option>
                  <option value="중">중품질</option>
                  <option value="하">하품질</option>
                </select>
              </div>

              {/* 가격 범위 필터 */}
              <div className="space-y-3">
                <label className="block text-sm font-semibold text-gray-700">가격 범위</label>
                <select
                  name="priceRange"
                  value={filterOptions.priceRange}
                  onChange={handleFilterChange}
                  className="w-full px-4 py-3 bg-white border border-gray-300 rounded-xl text-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent transition-all duration-300"
                >
                  <option value="">전체 가격</option>
                  <option value="0-100000">10만원 이하</option>
                  <option value="100000-500000">10만원 - 50만원</option>
                  <option value="500000-1000000">50만원 - 100만원</option>
                  <option value="1000000-">100만원 이상</option>
                </select>
              </div>

              {/* 재고 범위 필터 */}
              <div className="space-y-3">
                <label className="block text-sm font-semibold text-gray-700">재고 상태</label>
                <select
                  name="stockRange"
                  value={filterOptions.stockRange}
                  onChange={handleFilterChange}
                  className="w-full px-4 py-3 bg-white border border-gray-300 rounded-xl text-gray-800 focus:outline-none focus:ring-2 focus:ring-gray-400 focus:border-transparent transition-all duration-300"
                >
                  {stockRangeOptions.map(option => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* 필터 초기화 버튼 */}
            {(search || Object.values(filterOptions).some(v => v)) && (
              <div className="mt-6 flex justify-center">
                <button
                  onClick={clearFilters}
                  className="px-6 py-3 bg-gray-600 text-white rounded-xl hover:bg-gray-700 transition-all duration-300"
                >
                  필터 초기화
                </button>
              </div>
            )}
          </div>
        </div>

        {/* 결과 통계 */}
        <div className="bg-gray-50 rounded-3xl p-6 border border-gray-200 mb-8">
          <div className="flex flex-col md:flex-row justify-between items-center space-y-4 md:space-y-0">
            <div className="text-center md:text-left">
              <div className="text-2xl font-bold text-gray-800 mb-2">
                검색 결과: <span className="text-gray-600">{filtered.length}</span>개
              </div>
              {filtered.length !== products.length && (
                <div className="text-gray-600">
                  전체 {products.filter(p => p.stock > 0).length}개 중
                </div>
              )}
              {totalPages > 1 && (
                <div className="text-gray-600">
                  {startItem}-{endItem} / {filtered.length}개 표시
                </div>
              )}
            </div>
            <div className="flex flex-wrap gap-3 justify-center">
              <div className="px-4 py-2 bg-green-500 rounded-full text-white font-medium">
                상: {filtered.filter(p => p.status === '상').length}
              </div>
              <div className="px-4 py-2 bg-yellow-500 rounded-full text-white font-medium">
                중: {filtered.filter(p => p.status === '중').length}
              </div>
              <div className="px-4 py-2 bg-red-500 rounded-full text-white font-medium">
                하: {filtered.filter(p => p.status === '하').length}
              </div>
              {filterOptions.stockRange === "0" && (
                <div className="px-4 py-2 bg-gray-500 rounded-full text-white font-medium">
                  품절: {filtered.filter(p => p.stock === 0).length}
                </div>
              )}
            </div>
          </div>
        </div>

        {/* 상품 카드 그리드 */}
        {displayed.length > 0 ? (
          <>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-8">
              {displayed.map((product) => (
                <div 
                  key={product.id} 
                  className="bg-white rounded-3xl overflow-hidden border border-gray-200 shadow-lg"
                >
                  {/* 상품 이미지 */}
                  <div className="relative h-64 overflow-hidden">
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
                        <Package className="w-16 h-16 text-gray-400 mx-auto mb-2" />
                        <p className="text-gray-500 text-sm">이미지 없음</p>
                      </div>
                    </div>
                    
                    {/* 상태 배지 */}
                    <div className="absolute top-4 right-4">
                      <span className={`px-3 py-1 rounded-full text-sm font-bold shadow-lg ${getStatusColor(product.status)}`}>
                        {product.status}
                      </span>
                    </div>

                    {/* 재고 배지 */}
                    <div className="absolute top-4 left-4">
                      <span className={`px-3 py-1 rounded-full text-sm font-bold shadow-lg ${
                        product.stock === 0 ? 'bg-red-500 text-white' :
                        product.stock <= 2 ? 'bg-orange-500 text-white' :
                        'bg-green-500 text-white'
                      }`}>
                        {product.stock === 0 ? '품절' : `${product.stock}개`}
                      </span>
                    </div>

                    {/* 상세 보기 버튼 */}
                    <div className="absolute bottom-4 left-4 right-4">
                      <button
                        onClick={() => handleQuickView(product)}
                        className="w-full bg-gray-800 text-white py-3 rounded-xl font-semibold hover:bg-gray-700 transition-all duration-300"
                      >
                        <Eye className="w-5 h-5 inline mr-2" />
                        상세 보기
                      </button>
                    </div>
                  </div>

                  {/* 상품 정보 */}
                  <div className="p-6">
                    <h3 className="font-bold text-gray-800 text-lg mb-3 line-clamp-2">
                      {product.name}
                    </h3>
                    
                                      <div className="space-y-3 text-sm">
                    <div className="flex items-center text-gray-600">
                      <Package className="w-4 h-4 mr-2" />
                      <span className="truncate">{product.categoryName}</span>
                    </div>
                    <div className="flex items-center text-green-600 font-semibold">
                      <DollarSign className="w-4 h-4 mr-2" />
                      <span>{product.price.toLocaleString()}원</span>
                    </div>
                    {(product.width || product.depth || product.height) && (
                      <div className="flex items-center text-gray-500">
                        <div className="w-4 h-4 mr-2 flex items-center justify-center">
                          <div className="w-3 h-3 border border-gray-400 rounded-sm"></div>
                        </div>
                        <span className="text-xs">
                          {product.width && `${product.width}W`}
                          {product.depth && ` × ${product.depth}D`}
                          {product.height && ` × ${product.height}H`}cm
                        </span>
                      </div>
                    )}
                    <div className="flex items-center text-gray-500">
                      <Calendar className="w-4 h-4 mr-2" />
                      <span>
                        {product.createdAt ? 
                          new Date(product.createdAt).toLocaleDateString('ko-KR', {
                            year: 'numeric',
                            month: 'long',
                            day: 'numeric'
                          }) : 
                          '날짜 없음'
                        }
                      </span>
                    </div>
                  </div>
                  </div>
                </div>
              ))}
            </div>

            {/* 페이징 */}
            {totalPages > 1 && (
              <div className="flex justify-center items-center space-x-2 mb-8">
                <button
                  onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                  disabled={currentPage === 1}
                  className="px-3 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  <ChevronLeft className="w-5 h-5" />
                </button>
                
                {Array.from({ length: totalPages }, (_, i) => i + 1).map(page => (
                  <button
                    key={page}
                    onClick={() => setCurrentPage(page)}
                    className={`px-3 py-2 rounded-lg transition-colors ${
                      currentPage === page
                        ? 'bg-gray-800 text-white'
                        : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
                    }`}
                  >
                    {page}
                  </button>
                ))}
                
                <button
                  onClick={() => setCurrentPage(prev => Math.min(prev + 1, totalPages))}
                  disabled={currentPage === totalPages}
                  className="px-3 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                >
                  <ChevronRight className="w-5 h-5" />
                </button>
              </div>
            )}
          </>
        ) : (
          /* 결과가 없을 때 */
          <div className="text-center py-20">
            <div className="w-32 h-32 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-8">
              <Package className="w-16 h-16 text-gray-400" />
            </div>
            <h3 className="text-2xl font-bold text-gray-800 mb-4">상품을 찾을 수 없습니다</h3>
            <p className="text-gray-600 mb-8 text-lg">
              검색 조건을 변경하거나 필터를 초기화해보세요.
            </p>
            <button 
              onClick={clearFilters}
              className="px-8 py-4 bg-gray-800 text-white rounded-2xl font-semibold hover:bg-gray-700 transition-all duration-300"
            >
              필터 초기화
            </button>
          </div>
        )}
      </div>
      
      {/* 상품 상세 모달 */}
      <ProductDetailModal
        product={selectedProduct}
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          setSelectedProduct(null);
        }}
        onPurchase={handlePurchase}
      />
    </div>
  );
} 