import apiClient from '@/lib/apiClient';
import { PageResponse } from '@/types/common';
import { SellerSettlementResponse } from '@/types/sellerSettlement';

interface PageParams {
    page: number;
    size: number;
}

export async function getSellerSettlementList(
    params: PageParams
): Promise<PageResponse<SellerSettlementResponse>> {
    const response = await apiClient.get('/seller/payouts', {
        params: {
            page: params.page - 1,
            size: params.size,
        },
    });
    return response.data;
}