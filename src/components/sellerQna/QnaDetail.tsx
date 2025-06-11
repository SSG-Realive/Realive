'use client';

import { SellerQnaDetailResponse } from '@/types/sellerQna';

interface Props {
    qna: SellerQnaDetailResponse;
}

export default function QnaDetail({ qna }: Props) {
    return (
        <div>
            <p><strong>판매자:</strong> {qna.sellerName} ({qna.sellerEmail})</p>
            <p className="mt-2"><strong>제목:</strong> {qna.title}</p>
            <p className="mt-2"><strong>내용:</strong> {qna.content}</p>
            <p className="mt-2"><strong>작성일:</strong> {qna.createdAt}</p>

            {qna.answer ? (
                <div className="mt-4 p-3 bg-green-100 rounded">
                    ✅ <strong>답변:</strong> {qna.answer}
                </div>
            ) : (
                <div className="mt-4 text-gray-500 italic">
                    답변 대기 중입니다.
                </div>
            )}
        </div>
    );
}
