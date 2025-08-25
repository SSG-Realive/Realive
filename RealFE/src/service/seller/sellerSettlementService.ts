import  { sellerApi } from '@/lib/apiClient';
import { SellerSettlementResponse, PayoutLogDetailResponse, PageResponse } from '@/types/seller/sellersettlement/sellerSettlement';

/**
 * 판매자 정산 내역 전체 조회
 */
export async function getSellerSettlementList(): Promise<SellerSettlementResponse[]> {
    const res = await sellerApi.get('/seller/settlements');
    return res.data;
}

/**
 * 판매자 정산 내역 페이징 조회
 */
export async function getSellerSettlementListWithPaging(page: number = 0, size: number = 10): Promise<PageResponse<SellerSettlementResponse>> {
    const res = await sellerApi.get(`/seller/settlements?page=${page}&size=${size}`);
    return res.data;
}

/**
 * 특정 날짜 기준 정산 내역 조회 (periodStart ~ periodEnd 포함 여부 기준)
 * @param date YYYY-MM-DD 형식의 날짜 문자열
 */
export async function getSellerSettlementListByDate(date: string): Promise<SellerSettlementResponse[]> {
    const res = await sellerApi.get(`/seller/settlements/by-date?date=${date}`);
    return res.data;
}

/**
 * 기간별 정산 내역 조회
 * @param from 시작 날짜 (YYYY-MM-DD)
 * @param to 종료 날짜 (YYYY-MM-DD)
 */
export async function getSellerSettlementListByPeriod(from: string, to: string): Promise<SellerSettlementResponse[]> {
    const url = `/seller/settlements/by-period?from=${from}&to=${to}`;
    console.log('🌐 API 요청 시작:', {
        url,
        from,
        to,
        timestamp: new Date().toISOString()
    });
    
    try {
        const res = await sellerApi.get(url);
        console.log('🌐 API 응답 성공:', {
            url,
            dataLength: res.data?.length || 0,
            status: res.status,
            data: res.data
        });
    return res.data;
    } catch (error: any) {
        console.error('🌐 API 요청 실패:', {
            url,
            error: error.message,
            status: error.response?.status,
            data: error.response?.data
        });
        throw error;
    }
}

/**
 * 정산 요약 정보 조회
 * @param from 시작 날짜 (YYYY-MM-DD)
 * @param to 종료 날짜 (YYYY-MM-DD)
 */
export async function getSellerSettlementSummary(from: string, to: string): Promise<{
    totalPayoutAmount: number;
    totalCommission: number;
    totalSales: number;
    payoutCount: number;
}> {
    const res = await sellerApi.get(`/seller/settlements/summary?from=${from}&to=${to}`);
    return res.data;
}

/**
 * 정산 상세 정보 조회
 * @param payoutLogId 정산 로그 ID
 */
export async function getSellerSettlementDetail(payoutLogId: number): Promise<PayoutLogDetailResponse> {
    const url = `/seller/settlements/${payoutLogId}/detail`;
    console.log('정산 상세 API 호출:', url);
    
    try {
        const res = await sellerApi.get(url);
        console.log('정산 상세 API 응답:', res.data);
    return res.data;
    } catch (error: any) {
        console.error('정산 상세 API 에러:', {
            url,
            payoutLogId,
            error: error.response?.data || error.message
        });
        throw error;
    }
}