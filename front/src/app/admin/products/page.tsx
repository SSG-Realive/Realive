"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { Search, Filter } from "lucide-react";
import apiClient from "@/lib/apiClient";

interface Product {
  id: number;
  name: string;
  category: string;
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
}

interface FilterOptions {
  category: string;
  status: string;
  priceRange: string;
  stockRange: string;
}

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
  const router = useRouter();

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem('adminToken');
        
        // 상품 목록과 카테고리 목록을 병렬로 가져오기
        const [productsRes, categoriesRes] = await Promise.all([
          apiClient.get('/admin/products', {
            headers: { Authorization: `Bearer ${token}` }
          }),
          apiClient.get('/admin/categories', {
            headers: { Authorization: `Bearer ${token}` }
          })
        ]);
        
        setProducts(productsRes.data.dtoList || []);
        setCategories(categoriesRes.data || []);
        
        console.log('Products:', productsRes.data);
        console.log('Categories:', categoriesRes.data);
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
    return category ? category.name : categoryId;
  };

  // 필터링 로직
  const filtered = products.filter(p => {
    const matchesSearch = p.name.toLowerCase().includes(filter.toLowerCase()) || 
                         getCategoryName(p.category).toLowerCase().includes(filter.toLowerCase());
    
    const matchesCategory = !filterOptions.category || p.category === filterOptions.category;
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
      const [min, max] = filterOptions.stockRange.split('-').map(Number);
      if (max) {
        matchesStock = p.stock >= min && p.stock <= max;
      } else {
        matchesStock = p.stock >= min;
      }
    }
    
    return matchesSearch && matchesCategory && matchesStatus && matchesPrice && matchesStock;
  });

  // 상품에서 사용되는 카테고리 ID 목록
  const productCategories = [...new Set(products.map(p => p.category))];

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
    setFilterOptions({ ...filterOptions, [e.target.name]: e.target.value });
  };
  
  const clearFilters = () => {
    setFilterOptions({
      category: "",
      status: "",
      priceRange: "",
      stockRange: ""
    });
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
              <select
                name="category"
                value={filterOptions.category}
                onChange={handleFilterChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-transparent"
              >
                <option value="">전체 카테고리</option>
                {productCategories.map(categoryId => {
                  const categoryName = getCategoryName(categoryId);
                  return (
                    <option key={categoryId} value={categoryId}>{categoryName}</option>
                  );
                })}
              </select>
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
                <option value="">전체 재고</option>
                <option value="0-10">10개 이하</option>
                <option value="10-50">10개 - 50개</option>
                <option value="50-100">50개 - 100개</option>
                <option value="100-">100개 이상</option>
              </select>
            </div>
          </div>
        )}
      </div>

      {loading ? (
        <div className="text-center py-8">로딩 중...</div>
      ) : (
        <table className="min-w-full border">
          <thead>
            <tr className="bg-gray-100">
              <th className="px-4 py-2">상품 사진</th>
              <th className="px-4 py-2">상품명</th>
              <th className="px-4 py-2">카테고리</th>
              <th className="px-4 py-2">가격</th>
              <th className="px-4 py-2">재고</th>
              <th className="px-4 py-2">등록일</th>
              <th className="px-4 py-2">상태</th>
              <th className="px-4 py-2">상세</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map(p => (
              <tr key={p.id}>
                <td className="px-4 py-2"><img src={p.productImage} alt="product" className="w-10 h-10 rounded object-cover" /></td>
                <td className="px-4 py-2">{p.name}</td>
                <td className="px-4 py-2">{getCategoryName(p.category)}</td>
                <td className="px-4 py-2">{p.price.toLocaleString()}원</td>
                <td className="px-4 py-2">{p.stock}</td>
                <td className="px-4 py-2">{p.createdAt}</td>
                <td className="px-4 py-2">
                  <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${getStatusColor(p.status)}`}>
                    {p.status}
                  </span>
                </td>
                <td className="px-4 py-2">
                  <button className="text-blue-600 underline" onClick={() => router.push(`/admin/products/${p.id}`)}>
                    상세
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
      
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[300px]">
            <h2 className="text-xl font-bold mb-4">상품 상세</h2>
            <img src={selected.productImage} alt="product" className="w-16 h-16 rounded object-cover mb-4" />
            <p><b>상품명:</b> {selected.name}</p>
            <p><b>카테고리:</b> {getCategoryName(selected.category)}</p>
            <p><b>가격:</b> {selected.price.toLocaleString()}원</p>
            <p><b>재고:</b> {selected.stock}</p>
            <p><b>등록일:</b> {selected.createdAt}</p>
            <p><b>상태:</b> {selected.status}</p>
            <div className="flex gap-2 mt-6">
              <button className="px-4 py-2 bg-blue-500 text-white rounded" onClick={() => router.push(`/admin/products/edit/${selected.id}`)}>수정</button>
              <button className="px-4 py-2 bg-green-500 text-white rounded" onClick={() => router.push(`/admin/products/stock/${selected.id}`)}>재고 관리</button>
              <button className="px-4 py-2 bg-gray-700 text-white rounded" onClick={() => router.push(`/admin/products/images/${selected.id}`)}>이미지 관리</button>
            </div>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelected(null)}>
              닫기
            </button>
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