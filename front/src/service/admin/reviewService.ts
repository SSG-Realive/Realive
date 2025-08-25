import { adminApi } from "@/lib/apiClient";
import {
  AdminReview,
  AdminReviewListRequest,
  AdminReviewListResponse,
  AdminReviewReport,
  AdminReviewReportListRequest,
  AdminReviewReportListResponse,
  AdminReviewReportProcessRequest,
  AdminReviewQna,
  AdminReviewQnaListRequest,
  AdminReviewQnaListResponse,
  AdminReviewQnaAnswerRequest,
  AdminReviewQnaDetail,
} from "@/types/admin/review";
import { paramsSerializer } from '@/lib/utils';

// 리뷰 목록 조회
export const getAdminReviewList = async (params: AdminReviewListRequest): Promise<AdminReviewListResponse> => {
  console.log('리뷰 목록 API 호출:', '/admin/seller-reviews', params);
  const response = await adminApi.get('/admin/seller-reviews', { 
    params,
    paramsSerializer,
  });
  console.log('리뷰 목록 API 응답:', response.data);
  return response.data.data || response.data;
};

// 리뷰 상세 조회
export const getAdminReview = async (reviewId: number): Promise<AdminReview> => {
  console.log('리뷰 상세 API 호출:', `/admin/seller-reviews/${reviewId}`);
  const response = await adminApi.get(`/admin/seller-reviews/${reviewId}`);
  console.log('리뷰 상세 API 응답:', response.data);
  return response.data.data || response.data;
};

// 리뷰 상태 변경
export const updateAdminReview = async (reviewId: number, isHidden: boolean): Promise<void> => {
  await adminApi.patch(`/admin/seller-reviews/${reviewId}`, { isHidden });
};

// 리뷰 신고 목록 조회
export const getAdminReviewReportList = async (params: AdminReviewReportListRequest): Promise<AdminReviewReportListResponse> => {
  console.log('리뷰 신고 목록 API 호출:', '/admin/reviews-reports/reports', params);
  const response = await adminApi.get('/admin/reviews-reports/reports', { 
    params,
    paramsSerializer,
  });
  console.log('리뷰 신고 목록 API 응답:', response.data);
  return response.data.data || response.data;
};

// 리뷰 신고 상세 조회
export const getAdminReviewReport = async (reportId: number): Promise<AdminReviewReport> => {
  console.log('리뷰 신고 상세 API 호출:', `/admin/reviews-reports/reports/${reportId}`);
  const response = await adminApi.get(`/admin/reviews-reports/reports/${reportId}`);
  console.log('리뷰 신고 상세 API 응답:', response.data);
  return response.data.data || response.data;
};

// 리뷰 신고 처리
export const processAdminReviewReport = async (reportId: number, request: AdminReviewReportProcessRequest): Promise<void> => {
  await adminApi.put(`/admin/reviews-reports/reports/${reportId}/action`, request);
};

// 리뷰 Q&A 목록 조회
export const getAdminReviewQnaList = async (params: AdminReviewQnaListRequest): Promise<AdminReviewQnaListResponse> => {
  console.log('Q&A 목록 API 호출:', '/admin/qna/customer', params);
  const response = await adminApi.get('/admin/qna/customer', { 
    params,
    paramsSerializer,
  });
  console.log('Q&A 목록 API 응답:', response.data);
  return response.data.data || response.data;
};

// 리뷰 Q&A 상세 조회
export const getAdminReviewQna = async (qnaId: number): Promise<AdminReviewQnaDetail> => {
  console.log('Q&A 상세 API 호출:', `/admin/qna/customer/${qnaId}`);
  const response = await adminApi.get(`/admin/qna/customer/${qnaId}`);
  console.log('Q&A 상세 API 응답:', response.data);
  return response.data.data || response.data;
};

// 리뷰 Q&A 답변 등록
export const answerAdminReviewQna = async (qnaId: number, request: AdminReviewQnaAnswerRequest): Promise<void> => {
  console.log('Q&A 답변 등록 API 호출:', `/admin/qna/customer/${qnaId}`, request);
  const response = await adminApi.patch(`/admin/qna/customer/${qnaId}`, request);
  console.log('Q&A 답변 등록 API 응답:', response.data);
};

// Q&A 삭제
export const deleteAdminReviewQna = async (qnaId: number): Promise<void> => {
  await adminApi.delete(`/admin/qna/customer/${qnaId}`);
};

// 리뷰 Q&A 상태 변경
export const updateAdminReviewQnaStatus = async (qnaId: number, isHidden: boolean): Promise<void> => {
  await adminApi.patch(`/admin/qna/customer/${qnaId}`, { isHidden });
}; 