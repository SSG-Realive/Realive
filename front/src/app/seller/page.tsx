// src/app/seller/page.tsx
'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore';

export default function SellerIndexPage() {
  const router = useRouter();
  
  useEffect(() => {
    const token = useSellerAuthStore.getState().token;

    if (token) {
      router.replace('/seller/dashboard');
    } else {
      router.replace('/seller/login');
    }
  }, [router]);

  return (
    <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-gray-50 flex items-center justify-center">
      <div className="text-center p-8">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
        <p className="text-gray-600 text-lg">이동 중...</p>
      </div>
    </div>
  );
}
