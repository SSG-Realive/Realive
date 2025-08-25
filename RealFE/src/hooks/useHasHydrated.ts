// src/hooks/useHasHydrated.ts (수정된 최종본)

import { useState, useEffect } from 'react';
// 💡 Seller 스토어를 사용하도록 경로를 명확히 합니다.
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore'; 

/**
 * Zustand-persist 스토어가 localStorage로부터 상태를 모두 불러왔는지(rehydrated)
 * '구독(subscribe)'하여 확인하는 커스텀 훅입니다.
 * @returns {boolean} 스토어가 성공적으로 rehydrate되었으면 true, 아니면 false를 반환합니다.
 */
export const useHasHydrated = () => {
  const [hydrated, setHydrated] = useState(false);

  useEffect(() => {
    // onFinishHydration은 하이드레이션이 완료될 때 실행될 콜백 함수를 등록하고,
    // 등록을 해제하는 unsubscribe 함수를 반환합니다.
    const unsub = useSellerAuthStore.persist.onFinishHydration(() => {
      // ✅ 하이드레이션이 완료되면 이 부분이 호출되고, 상태를 true로 변경합니다.
      setHydrated(true);
    });

    // useEffect가 실행되는 시점에는 이미 하이드레이션이 완료되었을 수도 있으므로,
    // 즉시 한 번 더 확인합니다.
    if (useSellerAuthStore.persist.hasHydrated()) {
      setHydrated(true);
    }
    
    // 컴포넌트가 언마운트될 때 구독을 해제하여 메모리 누수를 방지합니다.
    return () => {
      unsub();
    };
  }, []); // 이 useEffect는 컴포넌트 마운트 시 단 한 번만 실행되어 구독을 설정합니다.

  return hydrated;
};