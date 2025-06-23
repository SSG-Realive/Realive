"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Search, Filter } from "lucide-react";
import apiClient from "@/lib/apiClient";

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
  productImage: string;
}

interface Category {
  id: number;
  name: string;
  parentId: number | null;
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

// 재고 범위 옵션 추가
const stockRangeOptions = [
  { value: "", label: "전체" },
  { value: "1", label: "단일 상품 (1개)" },
  { value: "2+", label: "다중 재고 (2개 이상)" },
  { value: "0", label: "품절" }
];

export default function ProductManagementPage() {
  const [filter, setFilter] = useState("");
  const [selected, setSelected] = useState<Product | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [showFilters, setShowFilters] = useState(false);
  const [form, setForm] = useState({
    productId: "",
    purchasePrice: ""
  });
  const [submitting, setSubmitting] = useState(false);
  const [filterOptions, setFilterOptions] = useState<FilterOptions>({
    category: "",
    status: "",
    priceRange: "",
    stockRange: ""
  });
  
  // 계층형 카테고리 드롭다운을 위한 상태
  const [selectedParentCategory, setSelectedParentCategory] = useState<number | null>(null);
  const [categoryHierarchy, setCategoryHierarchy] = useState<{
    rootCategories: Category[];
    childCategories: Map<number, Category[]>;
    categoryMap: Map<number, Category>;
  } | null>(null);
  
  const router = useRouter();

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem('adminToken');
        
        // 상품 목록과 카테고리 목록을 병렬로 가져오기
        const [productsRes, categoriesRes] = await Promise.all([
          apiClient.get('/admin/products?size=100', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          apiClient.get('/admin/categories', {
            headers: { Authorization: `Bearer ${token}` }
          })
        ]);
        
        setProducts(productsRes.data.dtoList || []);
        setCategories(categoriesRes.data || []);
        
        // 카테고리 계층 구조 생성
        const hierarchy = buildCategoryHierarchy(categoriesRes.data || []);
        setCategoryHierarchy(hierarchy);
        
        console.log('Products:', productsRes.data);
        console.log('Categories:', categoriesRes.data);
        console.log('Category Hierarchy:', hierarchy);
        console.log('Root Categories:', hierarchy.rootCategories);
        console.log('Child Categories:', hierarchy.childCategories);
        
        // 카테고리 데이터 상세 확인
        console.log('=== 카테고리 데이터 상세 ===');
        (categoriesRes.data || []).forEach((cat: any, index: number) => {
          console.log(`${index}: ID=${cat.id}, Name=${cat.name}, ParentID=${cat.parentId}`);
        });
      } catch (err) {
        console.error('Error fetching data:', err);
        setProducts([]);
        setCategories([]);
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, []);

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  // 카테고리 ID로 이름을 찾는 함수
  const getCategoryName = (categoryId: string) => {
    const category = categories.find(cat => cat.id.toString() === categoryId);
    console.log('Looking for category:', categoryId, 'Found:', category);
    return category ? category.name : categoryId;
  };

  // 필터링 로직
  const filtered = products.filter(p => {
    const matchesSearch = p.name.toLowerCase().includes(filter.toLowerCase()) || 
                         p.categoryName.toLowerCase().includes(filter.toLowerCase());
    
    // 카테고리 필터링 로직 수정
    let matchesCategory = true;
    if (filterOptions.category && filterOptions.category !== "") {
      // 상품의 카테고리 경로에서 하위 카테고리 이름 추출
      const categoryPath = p.categoryName; // 예: "가구 > 소파"
      const categoryParts = categoryPath.split(' > ');
      const subCategoryName = categoryParts[categoryParts.length - 1]; // 마지막 부분이 하위 카테고리
      
      console.log(`상품: ${p.name}, 전체경로: ${categoryPath}, 하위카테고리: ${subCategoryName}, 선택된카테고리: ${filterOptions.category}`);
      
      matchesCategory = subCategoryName === filterOptions.category;
    }
    
    const matchesStatus = !filterOptions.status || p.status === filterOptions.status;
    
    let matchesPrice = true;
    if (filterOptions.priceRange) {
      const [min, max] = filterOptions.priceRange.split('-').map(Number);
      if (max) {
        matchesPrice = p.price >= min && p.price <= max;
      } else {
        matchesPrice = p.price >= min;
      }
    }
    
    let matchesStock = true;
    if (filterOptions.stockRange) {
      switch (filterOptions.stockRange) {
        case "0":
          matchesStock = p.stock === 0;
          break;
        case "1":
          matchesStock = p.stock === 1;
          break;
        case "2+":
          matchesStock = p.stock >= 2;
          break;
        default:
          matchesStock = true;
      }
    } else {
      // 기본적으로 재고가 0인 상품은 숨김
      matchesStock = p.stock > 0;
    }
    
    // 디버깅 로그
    if (filterOptions.category) {
      console.log(`상품: ${p.name}, 카테고리: ${p.categoryName}, 선택된 카테고리: "${filterOptions.category}", 매칭: ${matchesCategory}, 전체필터: ${matchesSearch && matchesCategory && matchesStatus && matchesPrice && matchesStock}`);
    }
    
    return matchesSearch && matchesCategory && matchesStatus && matchesPrice && matchesStock;
  });

  // 상품에서 사용되는 카테고리 이름 목록 (디버깅용)
  const productCategories = [...new Set(products.map(p => p.categoryName))];
  console.log('Product categories:', productCategories);
  console.log('Available categories:', categories);

  const handleOpenModal = () => {
    setForm({ productId: "", purchasePrice: "" });
    setShowModal(true);
  };
  
  const handleCloseModal = () => {
    setShowModal(false);
  };
  
  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };
  
  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const newValue = e.target.value;
    console.log(`필터 변경: ${e.target.name} = "${newValue}"`);
    setFilterOptions({ ...filterOptions, [e.target.name]: newValue });
  };
  
  const clearFilters = () => {
    setFilterOptions({
      category: "",
      status: "",
      priceRange: "",
      stockRange: ""
    });
    setSelectedParentCategory(null); // 상위 카테고리도 초기화
    setFilter("");
  };
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      const token = localStorage.getItem('adminToken');
      await apiClient.post('/admin/products/purchase', {
        productId: Number(form.productId),
        purchasePrice: Number(form.purchasePrice)
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setShowModal(false);
      // 상품 목록 새로고침
      setLoading(true);
      const res = await apiClient.get('/admin/products', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setProducts(res.data.dtoList || []);
    } catch (err) {
      alert('상품 등록에 실패했습니다.');
    } finally {
      setSubmitting(false);
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case '상': return 'bg-green-100 text-green-800';
      case '중': return 'bg-yellow-100 text-yellow-800';
      case '하': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="p-8">
      {/* CSS 스타일 주입 */}
      <style jsx global>{styles}</style>
      
      <div className="flex justify-between items-center mb-4">
        <h1 className="text-2xl font-bold">상품 관리</h1>
        <button
          className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          onClick={handleOpenModal}
        >
          상품 매입
        </button>
      </div>
      
      {/* 검색 및 필터 */}
      <div className="space-y-4 mb-4">
        {/* 검색바 */}
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="text"
            placeholder="상품명 또는 카테고리로 검색..."
            value={filter}
            onChange={e => setFilter(e.target.value)}
            className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded focus:ring-2 focus:ring-blue-500 focus:border-transparent"
          />
        </div>

        {/* 필터 토글 */}
        <div className="flex items-center justify-between">
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors"
          >
            <Filter className="w-5 h-5" />
            고급 필터
            {showFilters && <span className="text-blue-600">(활성화됨)</span>}
          </button>
          {(filter || Object.values(filterOptions).some(v => v)) && (
            <button
              onClick={clearFilters}
              className="text-sm text-gray-500 hover:text-gray-700 underline"
            >
              필터 초기화
            </button>
          )}
        </div>

        {/* 필터 옵션 */}
        {showFilters && (
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 p-4 bg-gray-50 rounded-lg">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">카테고리</label>
              <div className="space-y-2">
                {/* 상위 카테고리 드롭다운 */}
                <select
                  value={selectedParentCategory || ""}
                  onChange={(e) => {
                    const parentId = e.target.value ? Number(e.target.value) : null;
                    console.log('상위 카테고리 선택:', parentId, '선택된 값:', e.target.value);
                    console.log('카테고리 계층:', categoryHierarchy);
                    console.log('하위 카테고리:', categoryHierarchy?.childCategories.get(parentId || 0));
                    setSelectedParentCategory(parentId);
                    setFilterOptions(prev => ({ ...prev, category: "" })); // 하위 카테고리 선택 초기화
                  }}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
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
                    name="category"
                    value={filterOptions.category}
                    onChange={handleFilterChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-blue-50"
                  >
                    <option value="">전체 {categoryHierarchy.categoryMap.get(selectedParentCategory)?.name}</option>
                    {categoryHierarchy.childCategories.get(selectedParentCategory)?.map(category => (
                      <option key={category.id} value={category.name}>
                        {category.name}
                      </option>
                    ))}
                  </select>
                )}
                
                {/* 선택된 카테고리 표시 */}
                {filterOptions.category && (
                  <div className="text-sm text-blue-600 bg-blue-50 px-3 py-2 rounded-md">
                    선택된 카테고리: {filterOptions.category}
                  </div>
                )}
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">상태</label>
              <select
                name="status"
                value={filterOptions.status}
                onChange={handleFilterChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">전체 상태</option>
                <option value="상">상</option>
                <option value="중">중</option>
                <option value="하">하</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">가격 범위</label>
              <select
                name="priceRange"
                value={filterOptions.priceRange}
                onChange={handleFilterChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">전체 가격</option>
                <option value="0-100000">10만원 이하</option>
                <option value="100000-500000">10만원 - 50만원</option>
                <option value="500000-1000000">50만원 - 100만원</option>
                <option value="1000000-">100만원 이상</option>
              </select>
            </div>
            
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-2">재고 범위</label>
              <select
                name="stockRange"
                value={filterOptions.stockRange}
                onChange={handleFilterChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                {stockRangeOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            </div>
          </div>
        )}
      </div>

      {loading ? (
        <div className="text-center py-8">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
          <p className="mt-2 text-gray-600">상품 목록을 불러오는 중...</p>
        </div>
      ) : (
        <div className="space-y-4">
          {/* 결과 통계 */}
          <div className="flex justify-between items-center bg-white p-4 rounded-lg shadow-sm border">
            <div className="text-sm text-gray-600">
              총 <span className="font-semibold text-blue-600">{filtered.length}</span>개의 상품
              {filtered.length !== products.length && (
                <span className="ml-2 text-gray-500">
                  (전체 {products.filter(p => p.stock > 0).length}개 중)
                </span>
              )}
              {filterOptions.stockRange === "0" && (
                <span className="ml-2 text-red-500">
                  (품절 상품 포함)
                </span>
              )}
            </div>
            <div className="flex gap-2 text-sm">
              <span className="px-2 py-1 bg-green-100 text-green-800 rounded">상 上: {filtered.filter(p => p.status === '상').length}</span>
              <span className="px-2 py-1 bg-yellow-100 text-yellow-800 rounded">중 中: {filtered.filter(p => p.status === '중').length}</span>
              <span className="px-2 py-1 bg-red-100 text-red-800 rounded">하 下: {filtered.filter(p => p.status === '하').length}</span>
              {filterOptions.stockRange === "0" && (
                <span className="px-2 py-1 bg-gray-100 text-gray-800 rounded">품절: {filtered.filter(p => p.stock === 0).length}</span>
              )}
            </div>
          </div>  

          {/* 상품 카드 그리드 */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
            {filtered.map(product => (
              <div key={product.id} className="bg-white rounded-lg shadow-sm border hover:shadow-md transition-shadow">
                {/* 상품 이미지 */}
                <div className="relative">
                  <img 
                    src={product.productImage || '/images/placeholder.png'} 
                    alt={product.name}
                    className="w-full h-48 object-cover rounded-t-lg"
                    onError={(e) => {
                      e.currentTarget.src = '/images/placeholder.png';
                    }}
                  />
                  {/* 상태 배지 */}
                  <div className="absolute top-2 left-2">
                    <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(product.status)}`}>
                      {product.status}
                    </span>
                  </div>
                  {/* 재고 상태 배지 */}
                  <div className="absolute top-2 right-2">
                    {product.stock === 0 ? (
                      <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
                        품절
                      </span>
                    ) : product.stock === 1 ? (
                      <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-orange-100 text-orange-800">
                        마지막 1개
                      </span>
                    ) : product.stock <= 2 ? (
                      <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                        재고부족
                      </span>
                    ) : (
                      <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                        재고보유
                      </span>
                    )}
                  </div>
                </div>

                {/* 상품 정보 */}
                <div className="p-4">
                  <h3 className="font-semibold text-gray-900 mb-2 line-clamp-2" title={product.name}>
                    {product.name}
                  </h3>
                  
                  <div className="space-y-2 text-sm">
                    <div className="flex items-center text-gray-600">
                      <span className="font-medium">카테고리:</span>
                      <span className="ml-1 text-blue-600">{product.categoryName}</span>
                    </div>
                    
                    <div className="flex items-center justify-between">
                      <span className="font-medium text-gray-600">가격:</span>
                      <span className="font-semibold text-lg text-green-600">
                        {product.price.toLocaleString()}원
                      </span>
                    </div>
                    
                    <div className="flex items-center justify-between">
                      <span className="font-medium text-gray-600">재고:</span>
                      <span className={`font-semibold ${
                        product.stock === 0 ? 'text-red-600' : 
                        product.stock === 1 ? 'text-orange-600' : 
                        product.stock <= 2 ? 'text-yellow-600' : 'text-green-600'
                      }`}>
                        {product.stock}개
                      </span>
                    </div>
                  </div>

                  {/* 액션 버튼 */}
                  <div className="mt-4 flex gap-2">
                    <button 
                      onClick={() => router.push(`/admin/products/${product.id}`)}
                      className="flex-1 bg-blue-600 text-white px-3 py-2 rounded text-sm hover:bg-blue-700 transition-colors"
                    >
                      상세보기
                    </button>
                    <button 
                      onClick={() => setSelected(product)}
                      className="px-3 py-2 border border-gray-300 rounded text-sm hover:bg-gray-50 transition-colors"
                      title="빠른 보기"
                    >
                      👁️
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* 결과가 없을 때 */}
          {filtered.length === 0 && (
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
      )}
      
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-start mb-4">
                <h2 className="text-xl font-bold text-gray-900">상품 상세 정보</h2>
                <button 
                  onClick={() => setSelected(null)}
                  className="text-gray-400 hover:text-gray-600 text-2xl"
                >
                  ×
                </button>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                {/* 상품 이미지 */}
                <div>
                  <img 
                    src={selected.productImage || '/images/placeholder.png'} 
                    alt={selected.name}
                    className="w-full h-64 object-cover rounded-lg"
                    onError={(e) => {
                      e.currentTarget.src = '/images/placeholder.png';
                    }}
                  />
                </div>
                
                {/* 상품 정보 */}
                <div className="space-y-4">
                  <div>
                    <h3 className="text-lg font-semibold text-gray-900 mb-2">{selected.name}</h3>
                    <div className="flex gap-2 mb-3">
                      <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(selected.status)}`}>
                        {selected.status}
                      </span>
                      {selected.stock === 0 ? (
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
                          품절
                        </span>
                      ) : selected.stock === 1 ? (
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-orange-100 text-orange-800">
                          마지막 1개
                        </span>
                      ) : selected.stock <= 2 ? (
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">
                          재고부족
                        </span>
                      ) : (
                        <span className="inline-flex px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">
                          재고보유
                        </span>
                      )}
                    </div>
                  </div>
                  
                  <div className="space-y-3 text-sm">
                    <div className="flex justify-between items-center py-2 border-b border-gray-100">
                      <span className="font-medium text-gray-600">카테고리</span>
                      <span className="text-blue-600 font-medium">{selected.categoryName}</span>
                    </div>
                    
                    <div className="flex justify-between items-center py-2 border-b border-gray-100">
                      <span className="font-medium text-gray-600">가격</span>
                      <span className="font-semibold text-lg text-green-600">
                        {selected.price.toLocaleString()}원
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center py-2 border-b border-gray-100">
                      <span className="font-medium text-gray-600">재고</span>
                      <span className={`font-semibold ${
                        selected.stock === 0 ? 'text-red-600' : 
                        selected.stock === 1 ? 'text-orange-600' : 
                        selected.stock <= 2 ? 'text-yellow-600' : 'text-green-600'
                      }`}>
                        {selected.stock}개
                      </span>
                    </div>
                    
                    <div className="flex justify-between items-center py-2 border-b border-gray-100">
                      <span className="font-medium text-gray-600">등록일</span>
                      <span className="text-gray-900">{selected.createdAt}</span>
                    </div>
                  </div>
                  
                  {/* 액션 버튼 */}
                  <div className="flex gap-2 pt-4">
                    <button 
                      onClick={() => router.push(`/admin/products/edit/${selected.id}`)}
                      className="flex-1 bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 transition-colors"
                    >
                      수정
                    </button>
                    <button 
                      onClick={() => router.push(`/admin/products/stock/${selected.id}`)}
                      className="flex-1 bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition-colors"
                    >
                      재고 관리
                    </button>
                    <button 
                      onClick={() => router.push(`/admin/products/images/${selected.id}`)}
                      className="flex-1 bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700 transition-colors"
                    >
                      이미지 관리
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
      
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[350px]">
            <h2 className="text-xl font-bold mb-4">상품 매입</h2>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block mb-1 font-medium">상품 선택</label>
                <select name="productId" value={form.productId} onChange={handleChange} required className="border rounded px-3 py-2 w-full">
                  <option value="">상품을 선택하세요</option>
                  {products.map((p) => (
                    <option key={p.id} value={p.id}>{p.name} (ID: {p.id})</option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block mb-1 font-medium">매입 가격</label>
                <input name="purchasePrice" type="number" value={form.purchasePrice} onChange={handleChange} required className="border rounded px-3 py-2 w-full" step="100" />
              </div>
              <div className="flex gap-2 mt-6">
                <button type="submit" className="px-4 py-2 bg-blue-500 text-white rounded" disabled={submitting}>
                  {submitting ? '등록 중...' : '등록'}
                </button>
                <button type="button" className="px-4 py-2 bg-gray-500 text-white rounded" onClick={handleCloseModal}>
                  닫기
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
} 