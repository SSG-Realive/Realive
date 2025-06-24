
export interface ReviewReportViewDTO {
    reportId: number;
    sellerReviewId?: number;
    reporterId: number;
    reason: string;
    originalReviewContent?: string;
}