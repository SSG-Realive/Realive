export interface AdminDashboardDTO {
  queryDate: string;
  periodType: string;
  pendingSellerCount: number;
  productLog: ProductLogDTO;
  penaltyLogs: PenaltyLogDTO[];
  memberSummaryStats: MemberSummaryStatsDTO;
  salesSummaryStats: SalesSummaryStatsDTO;
  auctionSummaryStats: AuctionSummaryStatsDTO;
  reviewSummaryStats: ReviewSummaryStatsDTO;
}

export interface ProductLogDTO {
  salesWithCommissions: SalesWithCommissionDTO[];
  payoutLogs: PayoutLogDTO[];
}

export interface SalesWithCommissionDTO {
  salesLog: SalesLogDTO;
  commissionLog: CommissionLogDTO;
}

export interface SalesLogDTO {
  id: number;
  orderItemId: number;
  productId: number;
  sellerId: number;
  customerId: number;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  soldAt: string;
}

export interface CommissionLogDTO {
  id: number;
  salesLogId: number;
  commissionRate: number;
  commissionAmount: number;
  recordedAt: string;
}

export interface PayoutLogDTO {
  id: number;
  sellerId: number;
  periodStart: string;
  periodEnd: string;
  totalSales: number;
  totalCommission: number;
  payoutAmount: number;
  processedAt: string;
}

export interface PenaltyLogDTO {
  id: number;
  customerId: number; // Long 타입 (백엔드에서 Long으로 변경됨)
  reason: string;
  points: number;
  description: string;
  createdAt: string;
}

export interface MemberSummaryStatsDTO {
  // 회원 통계
  totalMembers: number;      // 전체 회원 수 (고객 + 판매자)
  activeMembers: number;     // 활성 회원 수
  inactiveMembers: number;   // 비활성 회원 수
  
  // 판매자 통계
  totalSellers: number;      // 전체 판매자 수
  activeSellers: number;     // 활성 판매자 수
  inactiveSellers: number;   // 비활성 판매자 수
}

export interface SalesSummaryStatsDTO {
  totalOrdersInPeriod: number;
  totalRevenueInPeriod: number;
  totalFeesInPeriod: number;
}

export interface AuctionSummaryStatsDTO {
  totalAuctionsInPeriod: number;
  totalBidsInPeriod: number;
  averageBidsPerAuctionInPeriod: number;
}

export interface ReviewSummaryStatsDTO {
  totalReviewsInPeriod: number;
  newReviewsInPeriod: number;
  averageRatingInPeriod: number;
  deletionRate: number;
} 