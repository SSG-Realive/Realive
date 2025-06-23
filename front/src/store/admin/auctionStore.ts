import { create } from 'zustand';
import { adminAuctionService } from '@/service/admin/auctionService';
import { AuctionResponseDTO } from '@/types/admin/auction';

interface AuctionStoreState {
    auctions: AuctionResponseDTO[];
    auctionDetail: AuctionResponseDTO | null;
    auctionDetailLoading: boolean;
    loading: boolean;
    fetchAuctions: () => Promise<void>;
    fetchAuctionById: (auctionId: number) => Promise<void>;
}

export const useAuctionStore = create<AuctionStoreState>((set) => ({
    auctions: [],
    auctionDetail: null,
    auctionDetailLoading: false,
    loading: false,
    fetchAuctions: async () => {
        set({ loading: true });
        const res = await adminAuctionService.getAuctions({ page: 0, size: 20 });
        set({ auctions: res.content, loading: false });
    },
    fetchAuctionById: async (auctionId: number) => {
        set({ auctionDetailLoading: true });
        const detail = await adminAuctionService.getAuctionById(auctionId);
        set({ auctionDetail: detail, auctionDetailLoading: false });
    },
}));