export interface SellerSettlementResponse {
    id: number;
    sellerId: number;
    periodStart: string;
    periodEnd: string;
    totalSales: number;
    totalCommission: number;
    payoutAmount: number;
    processedAt: string;
}