// src/store/customer/useAuthStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  /* 토큰 */
  accessToken: string | null;
  refreshToken: string | null;

  /* 프로필 정보 */
  email: string | null;
  userName: string | null;
  isTemporaryUser: boolean;

  /* 액션 */
  setAuth: (p: {
    accessToken: string;
    refreshToken: string;
    email: string;
    userName: string;
    temporaryUser: boolean;
  }) => void;
  setTokens: (a: string | null, r: string | null) => void;
  setUserName: (name: string | null) => void;   // ✅ 추가
  logout: () => void;

  /* 헬퍼 */
  hydrated: boolean;          // 로컬스토리지 → 메모리 복원 여부
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      accessToken: null,
      refreshToken: null,

      email: null,
      userName: null,
      isTemporaryUser: false,

      hydrated: false,

      /* 전체 세팅 */
      setAuth: ({ accessToken, refreshToken, email, userName, temporaryUser }) =>
        set({ accessToken, refreshToken, email, userName, isTemporaryUser: temporaryUser }),

      /* 토큰만 세팅 */
      setTokens: (a, r) => set({ accessToken: a, refreshToken: r }),

      /* 인증 여부 */
      isAuthenticated: () => !!get().accessToken && get().hydrated,

      setUserName: (name) => set({ userName: name }),

      /* 로그아웃 */
      logout: () =>
        set({
          accessToken: null,
          refreshToken: null,
          email: null,
          userName: null,
          isTemporaryUser: false,
        }),
    }),
    {
      name: 'auth-storage',         // localStorage key
      partialize: (s) => ({
        accessToken: s.accessToken,
        refreshToken: s.refreshToken,
        email: s.email,
        userName: s.userName,
        isTemporaryUser: s.isTemporaryUser,
      }),
      onRehydrateStorage: () => (state) => {
        if (state) (state as any).hydrated = true; // 복원 완료 표시
      },
    }
  )
);
