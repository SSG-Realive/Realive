'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getQnaDetail, updateQna, deleteQna } from '@/service/seller/sellerQnaService';

import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { SellerQnaUpdateRequest } from '@/types/seller/sellerqna/sellerQnaRequest';
import { SellerQnaDetailResponse } from '@/types/seller/sellerqna/sellerQnaResponse';

export default function QnaEditPage() {
    useSellerAuthGuard();

    const { id } = useParams();
    const router = useRouter();
    const [form, setForm] = useState<SellerQnaUpdateRequest>({
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

    const handleDelete = async () => {
        const confirmed = window.confirm('정말로 이 QnA를 삭제하시겠습니까? 삭제 시 복구할 수 없습니다.');
        if (!confirmed) return;

        await deleteQna(Number(id));
        alert('QnA가 삭제되었습니다.');
        router.push('/seller/qna');
    };

    return (
        <>
        <SellerHeader />
        
        <SellerLayout>
            
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
                <div className="flex gap-2">
                    <button
                        onClick={handleSubmit}
                        className="bg-blue-500 hover:bg-blue-600 text-white px-4 py-2 rounded"
                    >
                        수정하기
                    </button>
                    <button
                        onClick={handleDelete}
                        className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded"
                    >
                        삭제하기
                    </button>
                </div>
            </div>
        </SellerLayout>
        </>
    );
}
