"use client";
import { useRouter, useParams } from "next/navigation";
import React from "react";

const dummyProducts = [
  { id: 1, name: "무선 마우스", category: "전자기기", price: 25000, stock: 12, createdAt: "2024-03-01", status: "판매중", productImage: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=facearea&w=80&h=80" },
  { id: 2, name: "유선 키보드", category: "전자기기", price: 18000, stock: 0, createdAt: "2024-03-02", status: "품절", productImage: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=facearea&w=80&h=80" },
  { id: 3, name: "텀블러", category: "생활용품", price: 12000, stock: 30, createdAt: "2024-03-03", status: "판매중", productImage: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=facearea&w=80&h=80" },
  { id: 4, name: "노트북", category: "전자기기", price: 1200000, stock: 5, createdAt: "2024-03-04", status: "판매중", productImage: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=facearea&w=80&h=80" },
  { id: 5, name: "운동화", category: "패션", price: 69000, stock: 8, createdAt: "2024-03-05", status: "판매중", productImage: "https://images.unsplash.com/photo-1519125323398-675f0ddb6308?auto=format&fit=facearea&w=80&h=80" },
  { id: 6, name: "에코백", category: "패션", price: 15000, stock: 0, createdAt: "2024-03-06", status: "품절", productImage: "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=facearea&w=80&h=80" },
  { id: 7, name: "샴푸", category: "생활용품", price: 9000, stock: 40, createdAt: "2024-03-07", status: "판매중", productImage: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=facearea&w=80&h=80" },
  { id: 8, name: "스마트워치", category: "전자기기", price: 250000, stock: 3, createdAt: "2024-03-08", status: "숨김", productImage: "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=facearea&w=80&h=80" },
  { id: 9, name: "블루투스 스피커", category: "전자기기", price: 45000, stock: 10, createdAt: "2024-03-09", status: "판매중", productImage: "https://images.unsplash.com/photo-1519389950473-47ba0277781c?auto=format&fit=facearea&w=80&h=80" },
  { id: 10, name: "커피머신", category: "전자기기", price: 350000, stock: 2, createdAt: "2024-03-10", status: "판매중", productImage: "https://images.unsplash.com/photo-1465101046530-73398c7f28ca?auto=format&fit=facearea&w=80&h=80" },
];

export default function ProductDetailPage() {
  const router = useRouter();
  const params = useParams();
  const id = Number(params.id);
  const product = dummyProducts.find(p => p.id === id);

  if (!product) return <div className="p-8">상품을 찾을 수 없습니다.</div>;

  return (
    <div className="p-8 max-w-lg mx-auto">
      <h2 className="text-2xl font-bold mb-4">상품 상세</h2>
      <img src={product.productImage} alt="product" className="w-24 h-24 rounded object-cover mb-4" />
      <p><b>상품명:</b> {product.name}</p>
      <p><b>카테고리:</b> {product.category}</p>
      <p><b>가격:</b> {product.price.toLocaleString()}원</p>
      <p><b>재고:</b> {product.stock}</p>
      <p><b>등록일:</b> {product.createdAt}</p>
      <p><b>상태:</b> {product.status}</p>
      <div className="flex gap-2 mt-6">
        <button className="px-4 py-2 bg-blue-500 text-white rounded" onClick={() => router.push(`/admin/products/edit/${product.id}`)}>수정</button>
      </div>
      <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => router.push('/admin/products')}>
        목록으로
      </button>
    </div>
  );
} 