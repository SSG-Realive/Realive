'use client';

import { useEffect, useState } from 'react';
import { fetchQnaList } from '@/service/qnaService';
import { SellerQnaResponseDTO } from '@/types/qna/qnaTypes';
import Link from 'next/link';
import SellerLayout from '@/components/layouts/SellerLayout';
import Header from '@/components/Header';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function QnaListPage() {
  const checking = useSellerAuthGuard();
  const [qnaList, setQnaList] = useState<SellerQnaResponseDTO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (checking) return;

    const loadQnaList = async () => {
      try {
        const data = await fetchQnaList();
        // 안전하게 데이터 확인
        if (Array.isArray(data)) {
          setQnaList(data);
        } else {
          console.error('Unexpected QnA data:', data);
          setQnaList([]);
        }
      } catch (err) {
        console.error('QnA 목록 조회 실패', err);
        setQnaList([]); // fallback 설정 → component tree 안전 유지
      } finally {
        setLoading(false);
      }
    };

    loadQnaList();
  }, [checking]);

  // 인증 확인 중
  if (checking) return <div className="p-8">인증 확인 중...</div>;

  // 로딩 중
  if (loading)
    return (
      <>
        <Header />
        <SellerLayout>
          <div className="p-8">로딩 중...</div>
        </SellerLayout>
      </>
    );

  // 정상 렌더링
  return (
    <>
      <Header />
      <SellerLayout>
        <div className="p-8">
          <h1 className="text-2xl font-bold mb-6">고객문의 확인 (QnA)</h1>

          <div className="space-y-4">
            {qnaList.length === 0 && <p>등록된 문의가 없습니다.</p>}

            {qnaList.map((qna) => (
              <Link key={qna.id} href={`/seller/contacts/${qna.id}`}>
                <div className="p-4 border rounded hover:bg-gray-100 cursor-pointer">
                  <h2 className="text-lg font-semibold">{qna.title}</h2>
                  <p className="text-sm text-gray-600 mt-1">
                    {qna.isAnswered ? '✅ 답변 완료' : '❌ 미답변'}
                  </p>
                  <p className="text-xs text-gray-500 mt-1">작성일: {qna.createdAt}</p>
                </div>
              </Link>
            ))}
          </div>
        </div>
      </SellerLayout>
    </>
  );
}
