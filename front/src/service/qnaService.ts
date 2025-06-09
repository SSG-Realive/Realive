// /service/qnaService.ts

import apiClient from '@/lib/apiClient';
import {
  SellerQnaResponseDTO,
  SellerQnaDetailResponseDTO,
  SellerQnaRequestDTO,
  SellerQnaUpdateRequestDTO,
  SellerQnaAnswerRequestDTO,
} from '@/types/qna/qnaTypes';

// ✅ QnA 목록 조회 (GET /api/seller/qna)
export const fetchQnaList = async (): Promise<SellerQnaResponseDTO[]> => {
  const res = await apiClient.get<SellerQnaResponseDTO[]>('/api/seller/qna');
  return res.data;
};

// ✅ QnA 단건 조회 (GET /api/seller/qna/{qnaId})
export const fetchQnaDetail = async (qnaId: number): Promise<SellerQnaDetailResponseDTO> => {
  const res = await apiClient.get<SellerQnaDetailResponseDTO>(`/api/seller/qna/${qnaId}`);
  return res.data;
};

// ✅ QnA 작성 (POST /api/seller/new)
export const createQna = async (dto: SellerQnaRequestDTO): Promise<void> => {
  await apiClient.post('/api/seller/new', dto);
};

// ✅ QnA 수정 (PATCH /api/seller/qna/{qnaId})
export const updateQna = async (qnaId: number, dto: SellerQnaUpdateRequestDTO): Promise<void> => {
  await apiClient.patch(`/api/seller/qna/${qnaId}`, dto);
};

// ✅ QnA 삭제 (DELETE /api/seller/qna/{qnaId})
export const deleteQna = async (qnaId: number): Promise<void> => {
  await apiClient.delete(`/api/seller/qna/${qnaId}`);
};

// ✅ 관리자 QnA 답변 등록 (PATCH /api/admin/qna/{qnaId}/answer) → 필요 시 사용
export const answerQna = async (qnaId: number, dto: SellerQnaAnswerRequestDTO): Promise<void> => {
  await apiClient.patch(`/api/admin/qna/${qnaId}/answer`, dto);
};
