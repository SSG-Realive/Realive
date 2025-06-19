"use client";
import { useRouter, useParams } from "next/navigation";
import React, { useState } from "react";

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

export default function ProductEditPage() {
  const router = useRouter();
  const params = useParams();
  const id = Number(params.id);
  const product = dummyProducts.find(p => p.id === id);

  const [name, setName] = useState(product?.name || "");
  const [category, setCategory] = useState(product?.category || "");
  const [price, setPrice] = useState(product?.price || 0);
  const [stock, setStock] = useState(product?.stock || 0);
  const [status, setStatus] = useState(product?.status || "판매중");
  const [image, setImage] = useState(product?.productImage || "");
  const [imageFile, setImageFile] = useState<File | null>(null);

  if (!product) return <div className="p-8">상품을 찾을 수 없습니다.</div>;

  const handleSave = (e: React.FormEvent) => {
    e.preventDefault();
    // 실제 저장 로직 대신 목록으로 이동
    router.push("/admin/products");
  };

  return (
    <div className="p-8 max-w-lg mx-auto">
      <h2 className="text-2xl font-bold mb-4">상품 수정</h2>
      <form onSubmit={handleSave} className="space-y-4">
        <div>
          <label className="block mb-1 font-medium">상품명</label>
          <input className="border rounded px-3 py-2 w-full" value={name} onChange={e => setName(e.target.value)} required />
        </div>
        <div>
          <label className="block mb-1 font-medium">카테고리</label>
          <input className="border rounded px-3 py-2 w-full" value={category} onChange={e => setCategory(e.target.value)} required />
        </div>
        <div>
          <label className="block mb-1 font-medium">가격</label>
          <input type="number" className="border rounded px-3 py-2 w-full" value={price} onChange={e => setPrice(Number(e.target.value))} required />
        </div>
        <div>
          <label className="block mb-1 font-medium">재고</label>
          <input type="number" className="border rounded px-3 py-2 w-full" value={stock} onChange={e => setStock(Number(e.target.value))} required />
        </div>
        <div>
          <label className="block mb-1 font-medium">상태</label>
          <select className="border rounded px-3 py-2 w-full" value={status} onChange={e => setStatus(e.target.value as any)}>
            <option value="판매중">판매중</option>
            <option value="품절">품절</option>
            <option value="숨김">숨김</option>
          </select>
        </div>
        <div>
          <label className="block mb-1 font-medium">상품 이미지</label>
          <input type="file" accept="image/*" onChange={e => {
            if (e.target.files && e.target.files[0]) {
              setImageFile(e.target.files[0]);
              setImage(URL.createObjectURL(e.target.files[0]));
            }
          }} />
          {image && <img src={image} alt="미리보기" className="w-24 h-24 rounded object-cover mt-2" />}
        </div>
        <button type="submit" className="w-full bg-blue-500 text-white py-2 rounded hover:bg-blue-600">저장</button>
      </form>
      <button className="mt-4 px-4 py-2 bg-gray-500 text-white rounded" onClick={() => router.push('/admin/products')}>
        목록으로
      </button>
    </div>
  );
} 