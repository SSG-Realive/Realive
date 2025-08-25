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

// 페이지 응답 타입 추가
export interface PageResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    numberOfElements: number;
    empty: boolean;
}

// 정산 상세 정보를 위한 새로운 타입들
export interface PayoutLogDetailResponse {
    payoutInfo: PayoutLogInfo;
    salesDetails: SalesWithCommissionDetail[];
}

export interface PayoutLogInfo {
    id: number;
    sellerId: number;
    periodStart: string;
    periodEnd: string;
    totalSales: number;
    totalCommission: number;
    payoutAmount: number;
    processedAt: string;
}

export interface SalesWithCommissionDetail {
    salesLog: SalesLogDetail;
    commissionLog: CommissionLogDetail;
}

export interface SalesLogDetail {
    id: number;
    orderItemId: number;
    productId: number;
    sellerId: number;
    customerId: number;
    quantity: number;
    unitPrice: number;
    totalPrice: number;
    soldAt: string;
    productName?: string;  // 상품명 추가
    customerName?: string; // 고객명 추가
}

export interface CommissionLogDetail {
    id: number;
    salesLogId: number;
    commissionRate: number;
    commissionAmount: number;
    recordedAt: string;
}

// 판매일별 정산 데이터 타입
export interface DailySettlementItem {
    id: string; // 고유 ID (originalPayoutId_date_index 형식)
    originalPayoutId: number; // 원본 정산 ID
    sellerId: number;
    date: string; // 판매일 (YYYY-MM-DD 형식)
    periodStart: string; // 정산 기간 시작일 (판매일과 동일)
    periodEnd: string; // 정산 기간 종료일 (판매일과 동일)
    totalSales: number; // 해당 판매건의 매출
    totalCommission: number; // 해당 판매건의 수수료
    payoutAmount: number; // 해당 판매건의 지급액
    processedAt: string; // 정산 처리일시
    salesCount: number; // 개별 건이므로 항상 1
    salesDetails: SalesWithCommissionDetail[]; // 해당 판매건의 상세 내역
    // 개별 주문 정보
    productId?: number; // 상품 ID
    customerId?: number; // 고객 ID
    quantity?: number; // 수량
    unitPrice?: number; // 단가
    orderItemId?: number; // 주문 항목 ID
    soldAt?: string; // 판매 일시
}