'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { createQna } from '@/service/sellerQnaService';
import { CreateQnaRequest } from '@/types/sellerQna';
import Header from '@/components/Header';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function QnaCreatePage() {
    // useSellerAuthGuard();
    const router = useRouter();
    const [form, setForm] = useState<CreateQnaRequest>({
        title: '',
        content: '',
    });

    const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const handleSubmit = async () => {
        await createQna(form);
        alert('질문이 등록되었습니다.');
        router.push('/seller/qna');
    };

    return (
        <SellerLayout>
            <Header />
            <div className="max-w-2xl mx-auto p-6">
                <h1 className="text-2xl font-bold mb-4">QnA 작성</h1>

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
                    등록하기
                </button>
            </div>
        </SellerLayout>
    );
}