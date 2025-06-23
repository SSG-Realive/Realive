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

  return <div className="p-8">이동 중...</div>;
}
