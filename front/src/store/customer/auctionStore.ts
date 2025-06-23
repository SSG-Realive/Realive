import customerApi from '@/lib/apiClient';
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
  totalPages: number;
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
  setCategory: (category: string) => void;
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
        const { category, page, hasNext, loading } = get();

        if (!hasNext || loading) {
          console.log('Store: 요청 중단', { hasNext, loading });
          return;
        }

        const requestPage = page + 1;
        console.log('Store: API 호출 시작', { 
          requestPage, 
          category: category || 'All',
          currentItemsCount: get().auctions.length 
        });

        set({ loading: true });

        try {
          // API URL 구성 - 공개 API 사용
          const url = category
            ? `/api/public/auctions?page=${requestPage}&category=${encodeURIComponent(category)}`
            : `/api/public/auctions?page=${requestPage}`;

          console.log('Store: API URL', url);

          const res = await customerApi.get<ApiResponse<PaginatedAuctionResponse>>(url);
          
          if (res.data.status === 200) {
            const newData = res.data.data.content;
            const isLast = res.data.data.last;

            set(state => {
              const existingIds = new Set(state.auctions.map(a => a.id));
              const filteredNewData = newData.filter(auction => !existingIds.has(auction.id));

              console.log('Store: 중복 제거 후', {
                기존개수: state.auctions.length,
                새데이터: newData.length,
                필터링후: filteredNewData.length
              });

              return {
                auctions: [...state.auctions, ...filteredNewData],
                page: requestPage,
                hasNext: !isLast,
                error: null,
              };
            });
          } else {
            console.error('Store: API 응답 오류', res.data);
            set({ 
              error: res.data.message || '데이터를 가져올 수 없습니다.', 
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
