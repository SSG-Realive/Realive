// src/store/seller/useSellerAuthStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface SellerAuthState {
   /* 토큰 */
  accessToken: string | null;
  refreshToken: string | null;

  /* 프로필 */
  sellerName: string | null;

  /* 상태 */
  hydrated: boolean;

  /* 액션 */
  setTokens: (a: string, r: string) => void;
  setSellerName: (name: string | null) => void;   // ✅ 인터페이스에 추가
  logout: () => void;
}

export const useSellerAuthStore = create<SellerAuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,
      sellerName: null,             // 초기값

      hydrated: false,

      setTokens: (a, r) => set({ accessToken: a, refreshToken: r }),
      setSellerName: (name : string | null) => set({ sellerName: name }),  // ✅
      logout: () =>
        set({ accessToken: null, refreshToken: null, sellerName: null }),
    }),
    {
      name: 'seller-auth-storage',
      partialize: (s) => ({
        accessToken: s.accessToken,
        refreshToken: s.refreshToken,
        sellerName: s.sellerName,   // ✅ 함께 저장
      }),
      onRehydrateStorage: () => (state) => {
        if (state) (state as any).hydrated = true;
      },
    }
  )
);
