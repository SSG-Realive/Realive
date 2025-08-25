import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import { wonAuctionService } from '@/service/customer/wonAuctionService';
import { WonAuction, WonAuctionState } from '@/types/customer/auctions';
import { SpringPage } from '@/types/common/springPage';


// [반영] create<WonAuctionState>()를 사용하여 이 스토어의 상태 타입을 명확히 지정합니다.
export const useWonAuctionStore = create<WonAuctionState>()(
  devtools(
    (set, get) => ({
      // --- 상태(State) ---
      // [반영] '낙찰 경매' 목록이므로 category 상태는 필요 없습니다.
      auctions: [],
      page: 0,
      hasNext: true,
      loading: false,
      error: null,

      // --- 액션(Actions) ---
      reset: () => {
        set({
          auctions: [],
          page: 0,
          hasNext: true,
          error: null,
          loading: false,
        });
      },

      fetchWonAuctions: async () => {
        const { hasNext, loading, page } = get();

        if (!hasNext || loading) return;

        set({ loading: true });

        try {
          // [반영] '낙찰 경매' API는 category 파라미터가 필요 없으므로 GetAuctionsParams 타입을 사용하지 않습니다.
          const pageData: SpringPage<WonAuction> = await wonAuctionService.getWonAuctions({
            page,
            size: 10,
          });

          if (pageData) {
            const newData = pageData.content;
            const isLast = pageData.last;

            set((state) => {
              // 중복 데이터 방지
              const existingIds = new Set(state.auctions.map((a) => a.auctionId));
              const filteredNewData = newData.filter((auction) => !existingIds.has(auction.auctionId));

              return {
                auctions: [...state.auctions, ...filteredNewData],
                page: state.page + 1,
                hasNext: !isLast,
                error: null,
              };
            });
          } else {
            set({ error: '응답 데이터가 비어있습니다.', hasNext: false });
          }
        } catch (error: any) {
          const errorMessage = error.response?.data?.error?.message || '낙찰 목록을 불러오는 중 오류가 발생했습니다.';
          set({ error: errorMessage, hasNext: false });
        } finally {
          set({ loading: false });
        }
      },
    }),
    {
      name: 'won-auction-store',
    }
  )
);