"use client";
import { useRouter, useParams } from "next/navigation";
import { useEffect } from "react";

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