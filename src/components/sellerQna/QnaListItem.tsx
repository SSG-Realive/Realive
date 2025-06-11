'use client';

import Link from 'next/link';
import { SellerQnaResponse } from '@/types/sellerQna';

interface Props {
    qna: SellerQnaResponse;
}

export default function QnaListItem({ qna }: Props) {
    return (
        <li className="mb-2 border p-3 rounded hover:bg-gray-50">
            <Link href={`/seller/qna/${qna.id}`}>
                <div className="font-medium">{qna.title}</div>
                <div className="text-sm text-gray-600">
                    {qna.isAnswered ? '✅ 답변 완료' : '❌ 미답변'}
                </div>
            </Link>
        </li>
    );
}

