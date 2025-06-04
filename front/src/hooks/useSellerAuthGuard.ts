'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';

export default function useSellerAuthGuard() : boolean {
  const router = useRouter();
  const [checking, setChecking] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');

    if (!token) {
      alert('로그인이 필요합니다.');
      router.push('/seller/login');
    } else {
        setChecking(false);
    }
  }, [router]);

  return checking;
}
