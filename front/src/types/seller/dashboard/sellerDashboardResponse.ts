export interface SellerDashboardResponse{
    totalProductCount : number;
    todayProductCount : number;
    totalQnaCount : number;
    unansweredQnaCount : number;
    inProgressOrderCount : number;
    salesStats: SellerSalesStatsDTO;
    totalCustomers: number;
    averageRating: number;
    totalReviews: number;
    sellerRating?: 'RED' | 'YELLOW' | 'GREEN';
}

export interface SellerSalesStatsDTO {
    totalOrders: number;
    totalRevenue: number;
    totalFees: number;
    dailySalesTrend: DailySalesDTO[];
    monthlySalesTrend: MonthlySalesDTO[];
}

export interface DailySalesDTO {
    date: string;
    orderCount: number;
    revenue: number;
}

export interface MonthlySalesDTO {
    yearMonth: string;
    orderCount: number;
    revenue: number;
}