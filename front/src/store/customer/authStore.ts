import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AuthState {
  id: number | null;
  accessToken: string | null;
  refreshToken: string | null;
  email: string | null;
  userName: string | null;
  isTemporaryUser: boolean;
  hydrated: boolean;

  // 액션
  setAuth: (p: {
    id: number;
    accessToken: string;
    refreshToken: string | null;
    email: string;
    userName: string;
    temporaryUser: boolean;
  }) => void;
  setTokens: (a: string | null, r: string | null) => void;
  setUserName: (name: string | null) => void;
  logout: () => void;

  // 인증 여부 체크
  isAuthenticated: () => boolean;
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      id: null,
      accessToken: null,
      refreshToken: null,
      email: null,
      userName: null,
      isTemporaryUser: false,
      hydrated: false,

      setAuth: ({ id, accessToken, refreshToken, email, userName, temporaryUser }) =>
        set({
          id,
          accessToken,
          refreshToken,
          email,
          userName,
          isTemporaryUser: temporaryUser,
        }),

      setTokens: (a, r) => set({ accessToken: a, refreshToken: r }),

      setUserName: (name) => set({ userName: name }),

      logout: () =>
        set({
          id: null,
          accessToken: null,
          refreshToken: null,
          email: null,
          userName: null,
          isTemporaryUser: false,
        }),

      isAuthenticated: () => !!get().accessToken && get().hydrated,
    }),
    {
      name: 'auth-storage', // localStorage key
      partialize: (s) => ({
        id: s.id,
        accessToken: s.accessToken,
        refreshToken: s.refreshToken,
        email: s.email,
        userName: s.userName,
        isTemporaryUser: s.isTemporaryUser,
      }),
      onRehydrateStorage: () => (state) => {
        if (state) {
          state.hydrated = true;
        }
      },
    }
  )
);
