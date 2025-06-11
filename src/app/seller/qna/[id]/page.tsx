'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { getQnaDetail } from '@/service/sellerQnaService';
import { SellerQnaDetailResponse } from '@/types/sellerQna';
import QnaDetail from '@/components/sellerQna/QnaDetail';
import Header from '@/components/Header';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function QnaDetailPage() {
    // useSellerAuthGuard();
    const { id } = useParams();
    const [qna, setQna] = useState<SellerQnaDetailResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (!id) return;
        const fetchDetail = async () => {
            try {
                setLoading(true);
                const qna = await getQnaDetail(Number(id));
                setQna(qna);
                setError(null);
            } catch (err) {
                console.error('QnA 상세 조회 실패', err);
                setError('QnA 정보를 불러올 수 없습니다.');
            } finally {
                setLoading(false);
            }
        };
        fetchDetail();
    }, [id]);

    return (
        <SellerLayout>
            <Header />
            <div className="max-w-3xl mx-auto p-6">
                {loading ? (
                    <p>로딩 중...</p>
                ) : error ? (
                    <p className="text-red-500">{error}</p>
                ) : qna ? (
                    <QnaDetail qna={qna} />
                ) : null}
            </div>
        </SellerLayout>
    );
}