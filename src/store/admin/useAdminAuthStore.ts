// adminAuthStore.ts
import { create } from 'zustand';

interface AdminAuthState {
    accessToken: string | null;          // adminToken -> accessToken 으로 변경
    refreshToken: string | null;         // adminRefreshToken -> refreshToken 으로 변경
    setTokens: (accessToken: string, refreshToken: string) => void;
    logout: () => void;
}

export const useAdminAuthStore = create<AdminAuthState>((set) => ({
    accessToken: null,
    refreshToken: null,
    setTokens: (accessToken, refreshToken) => {
        localStorage.setItem('adminToken', accessToken);
        localStorage.setItem('adminRefreshToken', refreshToken);
        set({ accessToken, refreshToken });
    },
    logout: () => {
        localStorage.removeItem('adminToken');
        localStorage.removeItem('adminRefreshToken');
        set({ accessToken: null, refreshToken: null });
    },
}));