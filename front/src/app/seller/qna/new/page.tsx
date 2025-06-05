// QnA 작성
'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { createQna } from '@/service/sellerQnaService';
import { CreateQnaRequest } from '@/types/sellerQna';

export default function QnaCreatePage() {
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
        <div className="max-w-2xl mx-auto p-4">
            <h1 className="text-2xl font-bold mb-4">QnA 작성</h1>

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
                placeholder="질문 내용을 입력하세요"
                value={form.content}
                onChange={handleChange}
                className="w-full border p-2 h-40 rounded"
            />
            <button
                onClick={handleSubmit}
                className="mt-4 px-4 py-2 bg-blue-600 text-white rounded"
            >
                등록
            </button>
        </div>
    );
}