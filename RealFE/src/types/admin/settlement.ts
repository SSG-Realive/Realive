// 관리자 정산관리 관련 타입들

// 정산 검색 조건
export interface AdminPayoutSearchCondition {
  sellerName?: string;
  periodStart?: string;
  periodEnd?: string;
}

// 정산 목록 응답
export interface AdminPayoutResponse {
  payoutId: number;
  sellerId: number;
  sellerName: string;
  sellerEmail: string;
  periodStart: string;
  periodEnd: string;
  totalSales: number;
  totalCommission: number;
  payoutAmount: number;
  processedAt: string;
}

// 정산 상세 응답
export interface SalesDetailResponse {
  salesLogId: number;
  orderItemId: number;
  productId: number;
  quantity: number;
  unitPrice: number;
  totalPrice: number;
  soldAt: string;
}

export interface CommissionDetailResponse {
  commissionLogId: number;
  salesLogId: number;
  commissionRate: number;
  commissionAmount: number;
  recordedAt: string;
}

export interface AdminPayoutDetailResponse {
  payoutId: number;
  sellerId: number;
  sellerName: string;
  sellerEmail: string;
  periodStart: string;
  periodEnd: string;
  totalSales: number;
  totalCommission: number;
  payoutAmount: number;
  processedAt: string;
  salesDetails: SalesDetailResponse[];
  commissionDetails: CommissionDetailResponse[];
}

// 정산 통계 응답
export interface AdminSettlementStatisticsResponse {
  totalPayouts: number;
  recentPayouts: number;
  totalPayoutAmount: number;
  recentPayoutAmount: number;
}

// 일별 정산 요약 응답
export interface DailyPayoutSummaryResponse {
  date: string;
  totalAmount: number;
}

// 월별 정산 요약 응답
export interface MonthlyPayoutSummaryResponse {
  yearMonth: string;
  totalPayouts: number;
  totalAmount: number;
}

// 월별 정산 상세 응답
export interface MonthlyPayoutDetailResponse {
  yearMonth: string;
  totalPayouts: number;
  totalAmount: number;
  totalSales: number;
  totalCommission: number;
  payoutDetails: AdminPayoutResponse[];
}

// 페이징 응답
export interface AdminSettlementPageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  currentPage: number;
  size: number;
} 