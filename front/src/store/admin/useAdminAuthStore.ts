// src/store/admin/useAdminAuthStore.ts
import { create } from 'zustand';

interface AdminAuthState {
    accessToken: string | null;
    refreshToken: string | null;
    logout: () => void;
}

export const useAdminAuthStore = create<AdminAuthState>((set) => ({
    accessToken: null,
    refreshToken: null,
    logout: () => set({ accessToken: null, refreshToken: null }),
}));

