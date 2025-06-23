"use client";
import { useRouter, useParams } from "next/navigation";
import { useEffect } from "react";

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

  useEffect(() => {
    // 관리자는 상품 상세 페이지 대신 상품 리스트 페이지에서 모달로 상세 보기를 하므로
    // 이 페이지에 접근하면 상품 리스트 페이지로 리다이렉트
    router.replace('/admin/products');
  }, [router]);

  return (
    <div className="p-8 flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p className="text-gray-600">상품 리스트로 이동 중...</p>
      </div>
    </div>
  );
} 