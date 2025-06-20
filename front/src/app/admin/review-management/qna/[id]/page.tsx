"use client";
import React, { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { getAdminReviewQna, deleteAdminReviewQna } from "@/service/admin/reviewService";
import { AdminReviewQnaDetail } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";

export default function QnaDetailPage() {
  const router = useRouter();
  const params = useParams();
  const { accessToken } = useAdminAuthStore();
  const qnaId = Number(params.id);

  const [qna, setQna] = useState<AdminReviewQnaDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const fetchQnaDetail = async () => {
    if (!accessToken || isNaN(qnaId)) return;
    try {
      setLoading(true);
      setError(null);
      const data = await getAdminReviewQna(qnaId);
      setQna(data);
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
        alert("Q&A가 삭제되었습니다.");
        router.push("/admin/review-management/qna");
      } catch (err: any) {
        console.error("Q&A 삭제 실패:", err);
        alert(err.message || "삭제에 실패했습니다.");
      }
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
            className="bg-gray-500 text-white px-4 py-2 rounded hover:bg-gray-600 mr-2"
          >
            목록으로
          </button>
          <button 
            onClick={handleDelete}
            className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
          >
            삭제
          </button>
        </div>
      </div>

      <div className="bg-white rounded-lg shadow-lg p-6 space-y-6">
        {/* 질문 정보 */}
        <div>
          <h2 className="text-lg font-semibold border-b pb-2 mb-4">질문 정보</h2>
          <div className="space-y-3">
            <div>
              <span className="font-medium">Q&A ID:</span>
              <span className="ml-2">{qna.id}</span>
            </div>
            <div>
              <span className="font-medium">제목:</span>
              <span className="ml-2">{qna.title}</span>
            </div>
            {qna.userName && (
              <div>
                <span className="font-medium">작성자:</span>
                <span className="ml-2">{qna.userName}</span>
              </div>
            )}
            <div>
              <span className="font-medium">작성일:</span>
              <span className="ml-2">{new Date(qna.createdAt).toLocaleString()}</span>
            </div>
             <div>
                <span className="font-medium">답변상태:</span>
                <span className={`ml-2 font-semibold ${qna.isAnswered ? 'text-green-600' : 'text-red-600'}`}>
                  {qna.isAnswered ? '답변완료' : '미답변'}
                </span>
              </div>
          </div>
        </div>
        
        {/* 질문 내용 */}
        <div>
          <h2 className="text-lg font-semibold border-b pb-2 mb-4">질문 내용</h2>
          <div className="p-4 bg-gray-50 rounded min-h-[100px]">
            <p className="whitespace-pre-wrap">{qna.content}</p>
          </div>
        </div>

        {qna.isAnswered && (
          <div className="border-t pt-6">
            <h2 className="text-lg font-semibold border-b pb-2 mb-4">답변 내용</h2>
            <div className="p-4 bg-blue-50 rounded min-h-[100px]">
              <p className="whitespace-pre-wrap">{qna.answer}</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
} 