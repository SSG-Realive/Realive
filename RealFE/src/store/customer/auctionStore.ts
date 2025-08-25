// src/store/customer/auctionStore.ts


import { publicAuctionService } from '@/service/customer/publicAuctionService';
import { Auction, AuctionState, GetAuctionsParams } from '@/types/customer/auctions';
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';



export const useAuctionStore = create<AuctionState>()(
  devtools(
    (set, get) => ({
      // --- 상태(State) ---
      auctions: [],
      category: '',
      page: 0,
      hasNext: true,
      loading: false,
      error: null,
      lastFetchTime: 0,

      // --- 액션(Actions) ---
      setCategory: (category) => {
        console.log('Store: 카테고리 설정', category);
        set({
          category,
          page: 0,
          auctions: [],
          hasNext: true,
          error: null,
          lastFetchTime: 0,
        });
      },

      reset: () => {
        console.log('Store: 리셋');
        set({
          auctions: [],
          page: 0,
          category: '',
          hasNext: true,
          error: null,
          lastFetchTime: 0,
        });
      },

      fetchAuctions: async () => {
        const { category, page, hasNext, loading } = get();

        if (!hasNext || loading) {
          return;
        }

        set({ loading: true });

        try {
          const apiParams: GetAuctionsParams = {
            category: category || undefined,
            page,
            size: 10,
          };

          const response = await publicAuctionService.getPublicAuctions(apiParams);

          if (response.status === 200 && response.data.success) {
            const pageData = response.data.data;

            // ✅ [수정] pageData가 null이 아닌지 확인하는 안전장치 추가
            if (pageData) {
              const newData = pageData.content;
              const isLast = pageData.last;

              // ✅ [수정] state의 타입을 명시적으로 지정하여 암시적 'any' 오류 해결
              set((state: AuctionState) => {
                const existingIds = new Set(state.auctions.map((a) => a.id));
                const filteredNewData = newData.filter(
                  (auction) => !existingIds.has(auction.id)
                );

                return {
                  auctions: [...state.auctions, ...filteredNewData],
                  page: state.page + 1,
                  hasNext: !isLast,
                  error: null,
                };
              });
            } else {
              // pageData가 null 또는 undefined인 경우의 처리
              set({ error: '응답 데이터가 비어있습니다.', hasNext: false });
            }
          } else {
            // API가 200 응답을 줬지만, success: false인 경우
            const errorMessage = response.data.error?.message || '데이터를 가져오는 데 실패했습니다.';
            console.error('Store: API 응답 오류', errorMessage);
            set({ error: errorMessage, hasNext: false });
          }
        } catch (error: any) {
          // 네트워크 오류 또는 서버가 500 등 에러 코드를 반환한 경우
          const errorMessage = error.response?.data?.error?.message || '서버에 연결할 수 없습니다.';
          console.error('Store: API 호출 실패', error);
          set({ error: errorMessage, hasNext: false });
        } finally {
          set({ loading: false });
        }
      },
    }),
    {
      name: 'auction-store',
    }
  )
);