// QnA 수정
'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getQnaDetail, updateQna } from '@/service/sellerQnaService';
import { SellerQnaDetailResponse, UpdateQnaRequest } from '@/types/sellerQna';

export default function QnaEditPage() {
    const { id } = useParams();
    const router = useRouter();
    const [form, setForm] = useState<UpdateQnaRequest>({
        title: '',
        content: '',
    });

    useEffect(() => {
        if (!id) return;
        getQnaDetail(Number(id)).then((qna: SellerQnaDetailResponse) => {
            if (qna.isAnswered) {
                alert('이미 답변된 QnA는 수정할 수 없습니다.');
                router.push('/seller/qna');
            } else {
                setForm({
                    title: qna.title,
                    content: qna.content,
                });
            }
        });
    }, [id]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async () => {
        await updateQna(Number(id), form);
        alert('질문이 수정되었습니다.');
        router.push('/seller/qna');
    };

    return (
        <div className="max-w-2xl mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">QnA 수정</h1>

            <input
                type="text"
                name="title"
                placeholder="제목"
                value={form.title}
                onChange={handleChange}
                className="w-full border p-2 mb-3 rounded"
            />
            <textarea
                name="content"
                placeholder="내용"
                value={form.content}
                onChange={handleChange}
                className="w-full border p-2 h-40 rounded"
            />
            <button
                onClick={handleSubmit}
                className="mt-4 px-4 py-2 bg-green-600 text-white rounded"
            >
                수정 완료
            </button>
        </div>
    );
}