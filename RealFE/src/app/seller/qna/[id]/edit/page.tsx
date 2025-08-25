'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getQnaDetail, updateQna, deleteQna } from '@/service/seller/sellerQnaService';

import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { SellerQnaUpdateRequest } from '@/types/seller/sellerqna/sellerQnaRequest';
import { SellerQnaDetailResponse } from '@/types/seller/sellerqna/sellerQnaResponse';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function QnaEditPage() {
    useSellerAuthGuard();

    const { id } = useParams();
    const router = useRouter();
    const [form, setForm] = useState<SellerQnaUpdateRequest>({
        title: '',
        content: '',
    });
    const {show} = useGlobalDialog();
    useEffect(() => {
        if (!id) return;

        getQnaDetail(Number(id)).then((qna: SellerQnaDetailResponse) => {
            if (qna.isAnswered) {
                show('이미 답변된 QnA는 수정할 수 없습니다.');
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
        await show('질문이 수정되었습니다.');
        router.push('/seller/admin-qna');
    };

    const handleDelete = async () => {
        const confirmed = window.confirm('정말로 이 QnA를 삭제하시겠습니까? 삭제된 QnA는 목록에서 숨겨집니다.');
        if (!confirmed) return;

        await deleteQna(Number(id));
        await show('QnA가 삭제되었습니다.');
        router.push('/seller/admin-qna');
    };

    return (
        <>
        <SellerHeader />
        
        <SellerLayout>
            
            <div className="max-w-2xl mx-auto p-4 sm:p-6">
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
                    <h1 className="text-xl sm:text-2xl font-bold mb-6 text-gray-900">QnA 수정</h1>

                    <div className="space-y-4">
                        <div>
                            <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-2">
                                제목
                            </label>
                            <input
                                id="title"
                                type="text"
                                name="title"
                                placeholder="제목을 입력하세요"
                                value={form.title}
                                onChange={handleChange}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                            />
                        </div>
                        
                        <div>
                            <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-2">
                                내용
                            </label>
                            <textarea
                                id="content"
                                name="content"
                                placeholder="질문 내용을 입력하세요"
                                value={form.content}
                                onChange={handleChange}
                                rows={8}
                                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                            />
                        </div>
                        
                        <div className="flex flex-col sm:flex-row gap-3 pt-4">
                            <button
                                onClick={handleSubmit}
                                className="flex-1 bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium transition-colors"
                            >
                                수정하기
                            </button>
                            <button
                                onClick={handleDelete}
                                className="flex-1 bg-red-600 hover:bg-red-700 text-white px-6 py-3 rounded-lg font-medium transition-colors"
                            >
                                삭제하기
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </SellerLayout>
        </>
    );
}
