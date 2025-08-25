'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import { createQna } from '@/service/seller/sellerQnaService';

import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { SellerCreateQnaRequest } from '@/types/seller/sellerqna/sellerQnaRequest';

export default function QnaCreatePage() {
    useSellerAuthGuard();
    const router = useRouter();
    const [form, setForm] = useState<SellerCreateQnaRequest>({
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
        <>
        <SellerHeader/>
        <SellerLayout>
            
            <div className="max-w-2xl mx-auto p-4 sm:p-6">
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
                    <h1 className="text-xl sm:text-2xl font-bold mb-6 text-gray-900">QnA 작성</h1>

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
                        
                        <div className="flex justify-end">
                            <button
                                onClick={handleSubmit}
                                className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium transition-colors"
                            >
                                등록하기
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </SellerLayout>
        </>
    );
}