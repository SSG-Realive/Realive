'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';

export default function useSellerAuthGuard() {
  const router = useRouter();

  useEffect(() => {
    const token = localStorage.getItem('accessToken');

    if (!token) {
      alert('로그인이 필요합니다.');
      router.push('/seller/login');
    }
  }, [router]);
}
