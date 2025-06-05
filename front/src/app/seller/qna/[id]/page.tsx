'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import { getQnaDetail } from '@/service/sellerQnaService';
import { SellerQnaDetailResponse } from '@/types/sellerQna';
import QnaDetail from '@/components/sellerQna/QnaDetail';

export default function QnaDetailPage() {
    const { id } = useParams();

    const [qna, setQna] = useState<SellerQnaDetailResponse | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    // QnA 상세 정보 조회
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

    if (loading) return <div className="text-center text-gray-500">로딩 중...</div>;
    if (error) return <div className="text-center text-red-500">{error}</div>;
    if (!qna) return <div className="text-center text-gray-500">QnA 정보가 없습니다.</div>;

    return (
        <div className="max-w-3xl mx-auto">
            <h1 className="text-2xl font-bold mb-4">QnA 상세</h1>

            <QnaDetail qna={qna} />
        </div>
    );
}
