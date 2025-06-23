import  { sellerApi } from '@/lib/apiClient';
import { SellerSettlementResponse } from '@/types/seller/sellersettlement/sellerSettlement';

/**
 * 판매자 정산 내역 전체 조회
 */
export async function getSellerSettlementList(): Promise<SellerSettlementResponse[]> {
    const res = await sellerApi.get('/seller/settlements');
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