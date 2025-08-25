// app/customer/qna/write/page.tsx
'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter, useSearchParams } from 'next/navigation';
import { createCustomerQna } from '@/service/customer/customerQnaService';
import { CustomerQnaRequest } from '@/types/customer/qna/customerQnaRequest';
import { useGlobalDialog } from '@/app/context/dialogContext';
import { fetchProductDetail } from '@/service/customer/productService';
import { ProductDetail } from '@/types/seller/product/product';

export default function WriteQnaPage() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const { show } = useGlobalDialog();

    const [productId, setProductId] = useState<number | null>(null);
    const [productName, setProductName] = useState<string>('');
    const [productThumbnail, setProductThumbnail] = useState<string>('');
    const [title, setTitle] = useState<string>('');
    const [content, setContent] = useState<string>('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const pid = searchParams.get('productId');
        if (pid) {
            const parsedPid = Number(pid);
            if (!isNaN(parsedPid)) {
                setProductId(parsedPid);
                fetchProductDetail(parsedPid)
                    .then(data => {
                        setProductName(data.name);
                        setProductThumbnail(data.imageThumbnailUrl || '/default-thumbnail.png');
                    })
                    .catch(err => {
                        console.error('상품 정보 로드 실패:', err);
                        show('상품 정보를 불러오는 데 실패했습니다.');
                    });
            } else {
                setError('유효하지 않은 상품 ID입니다.');
            }
        } else {
            setError('상품 ID가 필요합니다.');
        }
    }, [searchParams, show]);

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();

        if (!productId) {
            show('상품 ID가 유효하지 않습니다.');
            return;
        }
        if (!title.trim() || !content.trim()) {
            show('제목과 내용을 모두 입력해주세요.');
            return;
        }

        setLoading(true);
        setError(null);

        const qnaData: CustomerQnaRequest = {
            title,
            content,
            productId,
            // ⭐⭐ customerId 필드를 제거합니다.
        };

        try {
            const response = await createCustomerQna(qnaData);
            show('QnA가 성공적으로 작성되었습니다!');
            router.push(`/main/products/${productId}`);
        } catch (err: any) {
            console.error('QnA 작성 실패:', err);
            setError(err.response?.data?.message || 'QnA 작성에 실패했습니다.');
            show(err.response?.data?.message || 'QnA 작성에 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    if (error) {
        return <div className="max-w-xl mx-auto p-6 text-red-600">{error}</div>;
    }
    if (!productId) {
        return <div className="max-w-xl mx-auto p-6">상품 ID를 불러오는 중입니다...</div>;
    }

    return (
        <div className="max-w-2xl mx-auto px-4 my-8">
            {/* 상품 정보 요약 */}
            {productName && (
                <div className="flex items-center space-x-4 mb-6">
                    <img src={productThumbnail} alt={productName} className="w-25 h-25 object-contain rounded" />
                    <div>
                        <p className="font-medium text-gray-900">{productName}</p>
                        <p className="text-sm text-gray-600">ID: {productId}</p>
                    </div>
                </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
                <div>
                    <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
                        제목
                    </label>
                    <input
                        type="text"
                        id="title"
                        value={title}
                        onChange={(e) => setTitle(e.target.value)}
                        className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-none shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                        placeholder="QnA 제목을 입력해주세요 (최대 100자)"
                        maxLength={100}
                        required
                    />
                </div>

                <div>
                    <label htmlFor="content" className="block text-sm font-medium text-gray-700 mb-1">
                        내용
                    </label>
                    <textarea
                        id="content"
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        rows={8}
                        className="mt-1 block w-full px-4 py-2 border border-gray-300 rounded-none shadow-sm focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                        placeholder="QnA 내용을 입력해주세요 (최대 1000자)"
                        maxLength={1000}
                        required
                    ></textarea>
                </div>

                <div className="flex justify-end space-x-3">
                    <button
                        type="button"
                        onClick={() => router.back()}
                        className="px-6 py-2 border border-gray-300 rounded-none text-sm font-light text-gray-700 hover:bg-gray-100 transition duration-150 ease-in-out"
                        disabled={loading}
                    >
                        취소
                    </button>
                    <button
                        type="submit"
                        className="px-6 py-2 bg-black text-white rounded-none text-sm font-light hover:bg-gray-900 transition duration-150 ease-in-out"
                        disabled={loading}
                    >
                        {loading ? '작성 중...' : '등록'}
                    </button>
                </div>
            </form>
        </div>
    );
}