// src/store/customer/useAuthStore.ts (최종 수정본)

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
    isAuthenticated: () => boolean;

    setAuth: (p: {
        id: number; // ✨ 1. 여기에 id 타입 추가
        accessToken: string;
        refreshToken: string;
        email: string;
        userName: string;
        temporaryUser: boolean;
    }) => void;
    setTokens: (a: string | null, r: string | null) => void;
    setUserName: (name: string | null) => void;
    logout: () => void;
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
            isAuthenticated: () => !!get().accessToken && get().hydrated,

            /* 전체 세팅 */
            // ✨ 2. 파라미터로 id를 받고, set 할 때도 id를 포함
            setAuth: ({ id, accessToken, refreshToken, email, userName, temporaryUser }) =>
                set({ id, accessToken, refreshToken, email, userName, isTemporaryUser: temporaryUser }),

            /* 토큰만 세팅 */
            setTokens: (a, r) => set({ accessToken: a, refreshToken: r }),

            setUserName: (name) => set({ userName: name }),

            /* 로그아웃 */
            logout: () =>
                set({
                    id: null, // ✨ 3. 로그아웃 시 id도 null로 초기화
                    accessToken: null,
                    refreshToken: null,
                    email: null,
                    userName: null,
                    isTemporaryUser: false,
                }),
        }),
        {
            name: 'auth-storage',
            // 로컬 스토리지에 저장/복원할 상태를 지정
            partialize: (s) => ({
                id: s.id, // ✨ 4. 로컬 스토리지에 id도 함께 저장
                accessToken: s.accessToken,
                refreshToken: s.refreshToken,
                email: s.email,
                userName: s.userName,
                isTemporaryUser: s.isTemporaryUser,
            }),
            // 스토리지에서 상태를 복원한 후 실행되는 함수
            onRehydrateStorage: () => (state) => {
                if (state) {
                    state.hydrated = true;
                }
            },
        }
    )
);