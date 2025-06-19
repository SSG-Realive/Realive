"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
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

export default function ProductManagementPage() {
  const [filter, setFilter] = useState("");
  const [selected, setSelected] = useState<Product | null>(null);
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState({
    productId: "",
    purchasePrice: ""
  });
  const [submitting, setSubmitting] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        setLoading(true);
        const token = localStorage.getItem('adminToken');
        const res = await apiClient.get('/admin/products', {
          headers: { Authorization: `Bearer ${token}` }
        });
        setProducts(res.data.dtoList || []); // 실제 응답 구조에 따라 조정
      } catch (err) {
        setProducts([]);
      } finally {
        setLoading(false);
      }
    };
    fetchProducts();
  }, []);

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filtered = products.filter(p => p.name.includes(filter) || p.category.includes(filter));

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
      <div className="mb-4">
        <input
          type="text"
          placeholder="상품명/카테고리 검색"
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="border rounded px-3 py-2"
        />
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
                <td className="px-4 py-2">{p.category}</td>
                <td className="px-4 py-2">{p.price.toLocaleString()}원</td>
                <td className="px-4 py-2">{p.stock}</td>
                <td className="px-4 py-2">{p.createdAt}</td>
                <td className="px-4 py-2">{p.status}</td>
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
            <p><b>카테고리:</b> {selected.category}</p>
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