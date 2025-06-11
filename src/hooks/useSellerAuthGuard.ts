'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getProfile } from '@/service/sellerService';

export default function useSellerAuthGuard() : boolean {
  const router = useRouter();
  const [checking, setChecking] = useState(true);

    useEffect(() => {
    const validate = async () => {
      const token = localStorage.getItem('accessToken');

      if (!token) {
        router.replace('/seller/login');
        return;
      }

      try {
        await getProfile(); // 유효성 검증
        setChecking(false); // 정상 통과
      } catch (err: any) {
        console.error('인증 실패', err);
        localStorage.removeItem('accessToken');
        router.replace('/seller/login');
      }
    };

    validate();
  }, [router]);

  return checking;
}
