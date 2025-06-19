import { create } from 'zustand';
import { devtools } from 'zustand/middleware';
import api from '@/app/lib/axios';

interface Auction {
  id: number;
  productId: number;
  startPrice: number;
  currentPrice: number;
  startTime: string;
  endTime: string;
  status: string;
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
  number: number;
  last: boolean;
}

interface AuctionState {
  auctions: Auction[];
  category: string;
  page: number;
  hasNext: boolean;
  loading: boolean;
  error: string | null;
  lastFetchTime: number; // 중복 요청 방지용
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
        const { category, page, hasNext, loading, lastFetchTime } = get();
        
        // 중복 요청 방지: 1초 이내 동일 요청 차단
        const now = Date.now();

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
          // API URL 구성 - 카테고리 파라미터 이름 확인 필요
          const url = category
            ? `/api/customer/auctions?page=${requestPage}&category=${encodeURIComponent(category)}`
            : `/api/customer/auctions?page=${requestPage}`;

          console.log('Store: API URL', url);

          const res = await api.get<ApiResponse<PaginatedAuctionResponse>>(url);
          
          if (res.data.status === 200) {
            const newData = res.data.data.content;
            const isLast = res.data.data.last;
            const currentPage = res.data.data.number;
            
            console.log('Store: 데이터 수신 성공', { 
              newDataCount: newData.length, 
              isLast, 
              serverPage: currentPage,
              requestedPage: requestPage
            });

            // 중복 데이터 제거
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
      name: 'auction-store', // Redux DevTools에서 확인 가능
    }
  )
);