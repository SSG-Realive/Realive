// src/hooks/useSellerAuthGuard.ts
'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore';


/**
 * 판매자 페이지 인증 가드
 * 로컬스토리지 → 메모리 복원(hydrated) 완료 후 accessToken 존재 여부를 체크한다.
 * 토큰이 없으면 /seller/login 으로 리다이렉트.
 * @returns 로딩(검사) 중이면 true, 검사 완료면 false
 */
export default function useSellerAuthGuard() {
  const router = useRouter();
  const { accessToken, hydrated } = useSellerAuthStore();   // ✅ 올바른 필드 사용

  useEffect(() => {
    if (!hydrated) return;          // 스토어 복원 전에는 대기
    if (!accessToken) {
      router.replace('/seller/login');
    }
  }, [hydrated, accessToken, router]);

  return !hydrated;                 // true = 아직 검사(로딩) 중
}
