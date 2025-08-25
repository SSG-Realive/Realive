"use client";
import React, { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { getAdminReviewQna, answerAdminReviewQna, deleteAdminReviewQna } from "@/service/admin/reviewService";
import { AdminReviewQnaDetail } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";
import { useGlobalDialog } from "@/app/context/dialogContext";

export default function QnaDetailPage() {
  const router = useRouter();
  const params = useParams();
  const { accessToken } = useAdminAuthStore();
  const qnaId = Number(params.id);

  const [qna, setQna] = useState<AdminReviewQnaDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const {show} = useGlobalDialog();
  const [answer, setAnswer] = useState('');
  
  const fetchQnaDetail = async () => {
    if (!accessToken || isNaN(qnaId)) return;
    try {
      setLoading(true);
      setError(null);
      const data = await getAdminReviewQna(qnaId);
      const d = data as any;
      setQna({
        ...data,
        isAnswered: d.isAnswered ?? d.is_answered ?? d.answered ?? false,
        answeredAt: d.answeredAt ?? d.answered_at,
        createdAt: d.createdAt ?? d.created_at,
      });
    } catch (err: any) {
      console.error("Q&A 상세 조회 실패:", err);
      setError(err.message || "Q&A 정보를 불러오는데 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (accessToken) {
      fetchQnaDetail();
    } else {
      router.replace('/admin/login');
    }
  }, [accessToken, qnaId]);

  const handleDelete = async () => {
    if (confirm("정말로 이 Q&A를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) {
      try {
        await deleteAdminReviewQna(qnaId);
        show("Q&A가 삭제되었습니다.");
        router.push("/admin/review-management/seller-qna");
      } catch (err: any) {
        console.error("Q&A 삭제 실패:", err);
        show(err.message || "삭제에 실패했습니다.");
      }
    }
  };

  const handleAnswer = async () => {
    if (!qna || !answer.trim()) {
      show("답변을 입력해주세요.");
      return;
    }
    
    try {
      await answerAdminReviewQna(qnaId, { answer });
      show("답변이 등록되었습니다.");
      
      // 답변 등록 후 즉시 상태 업데이트
      if (qna) {
        setQna({
          ...qna,
          answer: answer,
          isAnswered: true,
          answeredAt: new Date().toISOString()
        });
      }
      
      // 답변 입력 필드 초기화
      setAnswer('');
      
      // 백그라운드에서 상세 정보 새로고침 (최신 데이터 확인)
      setTimeout(() => {
        fetchQnaDetail();
      }, 100);
    } catch (err: any) {
      console.error("답변 등록 실패:", err);
      show(err.message || "답변 등록에 실패했습니다.");
    }
  };

  if (loading) return <div className="p-8 text-center">로딩 중...</div>;
  if (error) return <div className="p-8 text-center text-red-500">{error}</div>;
  if (!qna) return <div className="p-8">Q&A 정보를 찾을 수 없습니다.</div>;

  return (
    <div className="p-8 max-w-4xl mx-auto">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold">Q&A 상세</h1>
        <div>
          <button 
            onClick={() => router.back()}
            className="bg-gray-500 text-white px-4 py-2 rounded !hover:bg-gray-500 !hover:shadow-none !hover:text-inherit mr-2"
          >
            목록으로
          </button>
          <button 
            onClick={handleDelete}
            className="bg-red-500 text-white px-4 py-2 rounded !hover:bg-red-500 !hover:shadow-none !hover:text-inherit"
          >
            삭제
          </button>
        </div>
      </div>

      <div className="bg-white rounded-xl shadow p-6 mb-6 !shadow-sm !hover:shadow-none">
        <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center mb-4">
          <div>
            <div className="text-lg font-bold text-gray-800 mb-1">{qna.title}</div>
            <div className="text-sm text-gray-500">작성일: {new Date(qna.createdAt).toLocaleString()}</div>
          </div>
          <div className="text-right">
            <div className="font-bold">{qna.userName}</div>
            <div className="text-xs text-gray-400">ID: {qna.customerId}</div>
          </div>
        </div>
        <div className="text-base text-gray-700 whitespace-pre-line mb-4">{qna.content}</div>
        <div className="flex items-center gap-2 mb-2">
          <span className={`inline-flex px-3 py-1 text-xs font-semibold rounded-full shadow-sm ${qna.isAnswered ? 'bg-green-100 text-green-800 border border-green-300' : 'bg-red-100 text-red-800 border border-red-300'}`}>{qna.isAnswered ? "답변완료" : "미답변"}</span>
          {qna.answeredAt && <span className="text-xs text-gray-400">답변일: {new Date(qna.answeredAt).toLocaleString()}</span>}
        </div>
      </div>

      {!qna.isAnswered ? (
        <div className="bg-gray-50 rounded-xl shadow p-6 mb-6 !shadow-sm !hover:shadow-none">
          <div className="font-bold mb-2">답변 작성</div>
          <textarea className="w-full border rounded p-2 mb-2" rows={4} value={answer} onChange={e => setAnswer(e.target.value)} placeholder="답변을 입력하세요..." />
          <button className="bg-blue-600 text-white px-4 py-2 rounded !hover:bg-blue-600 !hover:shadow-none !hover:text-inherit" onClick={handleAnswer}>답변 등록</button>
        </div>
      ) : (
        <div className="bg-green-50 rounded-xl shadow p-6 mb-6 !shadow-sm !hover:shadow-none">
          <div className="font-bold mb-2 text-green-800">답변</div>
          <div className="text-base text-gray-700 whitespace-pre-line">{qna.answer}</div>
        </div>
      )}
    </div>
  );
} 