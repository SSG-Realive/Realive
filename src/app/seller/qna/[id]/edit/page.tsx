'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getQnaDetail, updateQna } from '@/service/sellerQnaService';
import { SellerQnaDetailResponse, UpdateQnaRequest } from '@/types/sellerQna';
import Header from '@/components/Header';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function QnaEditPage() {
    // useSellerAuthGuard();

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
        <SellerLayout>
            <Header />
            <div className="max-w-2xl mx-auto p-6">
                <h1 className="text-2xl font-bold mb-4">QnA 수정</h1>

                <input
                    type="text"
                    name="title"
                    placeholder="제목"
                    value={form.title}
                    onChange={handleChange}
                    className="border border-gray-300 p-2 w-full mb-4"
                />
                <textarea
                    name="content"
                    placeholder="내용"
                    value={form.content}
                    onChange={handleChange}
                    className="border border-gray-300 p-2 w-full mb-4 h-40"
                />
                <button
                    onClick={handleSubmit}
                    className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded"
                >
                    수정하기
                </button>
            </div>
        </SellerLayout>
    );
}
