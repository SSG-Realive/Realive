// src/app/404.tsx
'use client';

import { Suspense } from "react";
import { useSearchParams } from "next/navigation";

function NotFoundInner() {
  const params = useSearchParams(); // CSR 훅 사용
  return (
    <div className="text-center py-20">
      <h1 className="text-2xl font-bold">페이지를 찾을 수 없습니다.</h1>
      <p className="mt-2 text-gray-500">URL을 확인해 주세요.</p>
      <p className="text-xs mt-4">쿼리 파라미터: {params.toString()}</p>
    </div>
  );
}

export default function NotFound() {
  return (
    <Suspense fallback={<div className="py-20 text-center">로딩 중...</div>}>
      <NotFoundInner />
    </Suspense>
  );
}
