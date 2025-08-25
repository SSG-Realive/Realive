// adminAuthStore.ts
import { create } from 'zustand';
import { persist } from 'zustand/middleware';

interface AdminAuthState {
    accessToken: string | null;
    refreshToken: string | null;
    hydrated: boolean;
    setTokens: (accessToken: string, refreshToken: string) => void;
    logout: () => void;
    initialize: () => void;
}

export const useAdminAuthStore = create<AdminAuthState>()(
    persist(
        (set, get) => ({
            accessToken: null,
            refreshToken: null,
            hydrated: false,
            setTokens: (accessToken, refreshToken) => {
                // localStorage에도 저장 (호환성 유지)
                if (typeof window !== 'undefined') {
                    localStorage.setItem('adminToken', accessToken);
                    localStorage.setItem('adminRefreshToken', refreshToken);
                }
                set({ accessToken, refreshToken });
            },
            logout: () => {
                if (typeof window !== 'undefined') {
                    localStorage.removeItem('adminToken');
                    localStorage.removeItem('adminRefreshToken');
                }
                set({ accessToken: null, refreshToken: null });
            },
            initialize: () => {
                if (typeof window !== 'undefined') {
                    const adminToken = localStorage.getItem('adminToken');
                    const adminRefreshToken = localStorage.getItem('adminRefreshToken');
                    if (adminToken) {
                        set({ 
                            accessToken: adminToken, 
                            refreshToken: adminRefreshToken,
                            hydrated: true 
                        });
                    } else {
                        set({ hydrated: true });
                    }
                }
            },
        }),
        {
            name: 'admin-auth-storage',
            onRehydrateStorage: () => (state) => {
                if (state) {
                    state.hydrated = true;
                }
            },
        }
    )
);