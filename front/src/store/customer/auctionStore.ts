import customerApi from '@/lib/apiClient';
import { publicAuctionService, customerAuctionService } from '@/service/customer/auctionService';
import { create } from 'zustand';
import { devtools } from 'zustand/middleware';


interface Auction {
  id: number;
  productId: number;
  startPrice: number;
  currentPrice: number;
  startTime: string;
  endTime: string;
  status: string;
  createdAt?: string;
  adminProduct: {
    productName: string | null;
    imageUrl?: string | null;
  };
}

interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

interface PaginatedAuctionResponse {
  content: Auction[];
  Pages: number;
  number: number;  // 현재 페이지(0부터 시작)
  last: boolean;   // 마지막 페이지 여부
}

interface AuctionState {
  auctions: Auction[];
  category: string;
  page: number;
  hasNext: boolean;
  loading: boolean;
  error: string | null;
  lastFetchTime: number;
  isLoggedIn: boolean;
  setCategory: (category: string) => void;
  setLoginStatus: (isLoggedIn: boolean) => void;
  reset: () => void;
  fetchAuctions: () => Promise<void>;
}

export const useAuctionStore = create<AuctionState>()(
  devtools(
    (set, get) => ({
      auctions: [],
      category: '',
      page: 0,
      hasNext: true,
      loading: false,
      error: null,
      lastFetchTime: 0,
      isLoggedIn: false,

      setCategory: (category) => {
        console.log('Store: 카테고리 설정', category);
        set({ 
          category, 
          page: 0, 
          auctions: [], 
          hasNext: true, 
          error: null,
          lastFetchTime: 0
        });
      },

      setLoginStatus: (isLoggedIn) => {
        console.log('Store: 로그인 상태 변경', isLoggedIn);
        set({ 
          isLoggedIn,
          page: 0, 
          auctions: [], 
          hasNext: true, 
          error: null,
          lastFetchTime: 0
        });
      },

      reset: () => {
        console.log('Store: 리셋');
        set({ 
          auctions: [], 
          page: 0, 
          hasNext: true, 
          error: null,
          lastFetchTime: 0
        });
      },

      fetchAuctions: async () => {
        const { category, page, hasNext, loading, isLoggedIn } = get();

        if (!hasNext || loading) {
          console.log('Store: 요청 중단', { hasNext, loading });
          return;
        }

        console.log('Store: API 호출 시작', { 
          pageToFetch: page,
          category: category || 'All',
          currentItemsCount: get().auctions.length,
          isLoggedIn
        });

        set({ loading: true });

        try {
          // 로그인 상태에 따라 다른 API 사용
          const apiParams = {
            category: category || undefined,
            page,
            size: 10
          };

          let response;
          if (isLoggedIn) {
            console.log('Store: 고객 API 사용');
            response = await customerAuctionService.getAuctions(apiParams);
          } else {
            console.log('Store: 공개 API 사용');
            response = await publicAuctionService.getPublicAuctions(apiParams);
          }

          if (response.status === 200) {
            const newData = response.data.content;
            const isLast = response.data.last;

            set(state => {
              const existingIds = new Set(state.auctions.map(a => a.id));
              const filteredNewData = newData.filter((auction: Auction) => !existingIds.has(auction.id));

              console.log('Store: 중복 제거 후', {
                기존개수: state.auctions.length,
                새데이터: newData.length,
                필터링후: filteredNewData.length
              });

              return {
                auctions: [...state.auctions, ...filteredNewData],
                page: state.page + 1,
                hasNext: !isLast,
                error: null,
              };
            });
          } else {
            console.error('Store: API 응답 오류', response);
            set({ 
              error: response.message || '데이터를 가져올 수 없습니다.', 
              hasNext: false 
            });
          }
        } catch (error) {
          console.error('Store: API 호출 실패', error);
          set({ 
            error: '서버 연결에 실패했습니다.', 
            hasNext: false 
          });
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
