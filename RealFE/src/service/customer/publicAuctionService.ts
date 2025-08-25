// service/public/publicAuctionService.ts

import customerApi from '@/lib/apiClient';

// ✅ 기존 타입을 그대로 사용합니다.
import type { Auction, PaginatedAuctionResponse, ApiResponse as OldApiResponse, GetAuctionsParams } from '@/types/customer/auctions';
import { BackendApiResponse, SpringPage } from '@/types/public/auctions';
// ✅ 백엔드의 실제 응답 구조를 임시로 정의합니다. (이 파일 안에서만 사용)
; 

/**
 * 공개된 경매 목록을 가져와서, 프론트엔드가 사용하던 PaginatedAuctionResponse 형태로 변환하여 반환합니다.
 */
const fetchPublicActiveAuctions = async (): Promise<PaginatedAuctionResponse> => {
    // 1. 서버에 요청할 때는, 실제 백엔드 응답 타입(BackendApiResponse<SpringPage<Auction>>)을 기대합니다.
    const response = await customerApi.get<BackendApiResponse<SpringPage<Auction>>>(
        '/public/auctions?status=PROCEEDING&size=10&sort=endTime,asc'
    );
     console.log('1. 서버에서 받은 원본 응답:', response.data);

    // 2. 응답이 성공적이고 데이터가 존재할 때
    if (response.data && response.data.data && response.data.data.content)  {
        const backendPageData = response.data.data; // 실제 Spring Page 객체

        // ✅ 3. 기존 PaginatedAuctionResponse 형태에 맞게 데이터를 "재조립"하여 반환합니다.
        return {
            content: backendPageData.content,
            totalPages: backendPageData.totalPages,
            number: backendPageData.number,
            last: backendPageData.last,
        };
    }
    
    // 4. 실패했거나 데이터가 없을 경우, 기본 빈 값 반환
    return {
        content: [],
        totalPages: 0,
        number: 0,
        last: true,
    };
};

/**
 * 인기 경매 목록을 가져옵니다 (입찰 수 기준)
 */
const fetchPopularAuctions = async (): Promise<PaginatedAuctionResponse> => {
    try {
        const response = await customerApi.get<BackendApiResponse<SpringPage<Auction>>>(
            '/public/auctions?status=PROCEEDING&size=10&sort=bidCount,desc'
        );
        
        if (response.data && response.data.data && response.data.data.content) {
            const backendPageData = response.data.data;
            return {
                content: backendPageData.content,
                totalPages: backendPageData.totalPages,
                number: backendPageData.number,
                last: backendPageData.last,
            };
        }
        
        return {
            content: [],
            totalPages: 0,
            number: 0,
            last: true,
        };
    } catch (error) {
        console.error('인기 경매 목록 조회 실패:', error);
        return {
            content: [],
            totalPages: 0,
            number: 0,
            last: true,
        };
    }
};

/**
 * 마감 임박 경매 목록을 가져옵니다 (종료 시간 기준)
 */
const fetchEndingSoonAuctions = async (): Promise<PaginatedAuctionResponse> => {
    try {
        const response = await customerApi.get<BackendApiResponse<SpringPage<Auction>>>(
            '/public/auctions?status=PROCEEDING&size=10&sort=endTime,asc'
        );
        
        if (response.data && response.data.data && response.data.data.content) {
            const backendPageData = response.data.data;
            return {
                content: backendPageData.content,
                totalPages: backendPageData.totalPages,
                number: backendPageData.number,
                last: backendPageData.last,
            };
        }
        
        return {
            content: [],
            totalPages: 0,
            number: 0,
            last: true,
        };
    } catch (error) {
        console.error('마감 임박 경매 목록 조회 실패:', error);
        return {
            content: [],
            totalPages: 0,
            number: 0,
            last: true,
        };
    }
};

/**
 * 경매 예정 목록을 가져옵니다 (시작 시간 기준)
 */
const fetchScheduledAuctions = async (): Promise<PaginatedAuctionResponse> => {
    try {
        const response = await customerApi.get<BackendApiResponse<SpringPage<Auction>>>(
            '/public/auctions?status=SCHEDULED&size=10&sort=startTime,asc'
        );
        
        if (response.data && response.data.data && response.data.data.content) {
            const backendPageData = response.data.data;
            return {
                content: backendPageData.content,
                totalPages: backendPageData.totalPages,
                number: backendPageData.number,
                last: backendPageData.last,
            };
        }
        
        return {
            content: [],
            totalPages: 0,
            number: 0,
            last: true,
        };
    } catch (error) {
        console.error('경매 예정 목록 조회 실패:', error);
        return {
            content: [],
            totalPages: 0,
            number: 0,
            last: true,
        };
    }
};

// =================================================================
// ✨ Zustand 스토어를 위해 새로 추가하는 함수
// =================================================================
/**
 * [신규 함수] Zustand 스토어의 무한 스크롤을 위해 사용됩니다.
 * 카테고리, 페이지, 사이즈 등 동적인 파라미터를 받아 API를 호출합니다.
 * 스토어에서 직접 상태(status)와 데이터(data)를 처리할 수 있도록, Axios 응답 객체 원본을 그대로 반환합니다.
 * @param params - API 요청에 필요한 동적 파라미터 (category, page, size 등)
 */
const getPublicAuctions = (params: GetAuctionsParams) => {
  console.log('Service (스토어용): 공개 경매 API 호출 시작', params);

  // Axios 응답 Promise 자체를 반환
  return customerApi.get<BackendApiResponse<SpringPage<Auction>>>('/public/auctions', {
    params, // 예: /public/auctions?category=ALL&page=0&size=10
  });
};



export const publicAuctionService = {
    fetchPublicActiveAuctions,
    fetchPopularAuctions,
    fetchEndingSoonAuctions,
    fetchScheduledAuctions,
    getPublicAuctions
};



