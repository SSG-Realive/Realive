// src/store/admin/useAdminAuthStore.ts
import { create } from 'zustand';

interface AdminAuthState {
    accessToken?: string;
    refreshToken?: string;
    logout: () => void;
}

export const useAdminAuthStore = create<AdminAuthState>((set) => ({
    accessToken: undefined,
    refreshToken: undefined,
    logout: () => set({ accessToken: undefined, refreshToken: undefined }),
}));
