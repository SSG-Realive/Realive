'use client';

import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { useRouter, useSearchParams } from 'next/navigation';
import Navbar from '@/components/customer/common/Navbar';

function ReviewPage() {
    const router = useRouter();
    const searchParams = useSearchParams();

    const orderIdParam = searchParams.get('orderId');
    const sellerIdParam = searchParams.get('sellerId');

    const [orderId, setOrderId] = useState<number | null>(null);
    const [sellerId, setSellerId] = useState<number | null>(null);

    const [rating, setRating] = useState<number>(5);
    const [content, setContent] = useState('');
    const [images, setImages] = useState<File[]>([]);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState('');

    const [alreadyReviewed, setAlreadyReviewed] = useState<boolean | null>(null); // 중복 여부

    // 초기 로딩 및 리뷰 중복 체크
    useEffect(() => {
        if (orderIdParam && sellerIdParam) {
            const parsedOrderId = Number(orderIdParam);
            const parsedSellerId = Number(sellerIdParam);
            setOrderId(parsedOrderId);
            setSellerId(parsedSellerId);

            // 중복 체크 API 호출
            axios
                .get('/api/reviews/check-exists', {
                    params: {
                        orderId: parsedOrderId,
                        sellerId: parsedSellerId,
                    },
                })
                .then((res) => {
                    setAlreadyReviewed(res.data.exists);
                })
                .catch((err) => {
                    console.error('중복 체크 실패:', err);
                    setAlreadyReviewed(null); // 오류 시 판단 유보
                });
        }
    }, [orderIdParam, sellerIdParam]);

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files) return;
        setImages(Array.from(e.target.files));
    };

    const uploadImages = async () => {
        const urls: string[] = [];
        for (const file of images) {
            const formData = new FormData();
            formData.append('file', file);
            try {
                const res = await axios.post('/api/uploads', formData, {
                    headers: { 'Content-Type': 'multipart/form-data' },
                });
                urls.push(res.data.url);
            } catch (err) {
                console.error('이미지 업로드 실패:', err);
            }
        }
        return urls;
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        if (!orderId || !sellerId) {
            setMessage('주문 ID 또는 판매자 ID가 누락되었습니다.');
            return;
        }
        setLoading(true);
        try {
            const uploadedUrls = await uploadImages();
            const payload = {
                orderId,
                sellerId,
                rating,
                content,
                imageUrls: uploadedUrls,
            };
            await axios.post('/api/reviews', payload);
            setMessage('리뷰가 성공적으로 등록되었습니다.');
            router.push('/customer/reviews/my');
        } catch (error) {
            console.error('리뷰 등록 실패:', error);
            setMessage('리뷰 등록 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    // 중복 여부 확인 중
    if (alreadyReviewed === null) {
        return <p className="text-center py-10">리뷰 정보를 확인 중입니다...</p>;
    }

    // 이미 리뷰 작성된 경우
    if (alreadyReviewed) {
        return (
            <div className="container max-w-xl mx-auto py-10">
                <Navbar/>
                <h1 className="text-2xl font-bold mb-4">리뷰 작성 불가</h1>
                <p>이미 이 주문에 대한 리뷰를 작성하셨습니다.</p>
                <button
                    className="mt-4 bg-gray-500 text-white px-4 py-2 rounded"
                    onClick={() => router.push('/customer/reviews/my')}
                >
                    내 리뷰 보기
                </button>
            </div>
        );
    }

    // 리뷰 작성 폼
    return (
        <div className="container max-w-xl mx-auto py-10">
            <Navbar/>
            <h1 className="text-2xl font-bold mb-6">리뷰 작성</h1>
            <form onSubmit={handleSubmit} className="space-y-4">
                <div>
                    <label className="block mb-1">평점 (0.5 단위)</label>
                    <select
                        value={rating}
                        onChange={(e) => setRating(Number(e.target.value))}
                        className="w-full border rounded px-3 py-2"
                        required
                    >
                        {[...Array(9)].map((_, i) => {
                            const val = (i + 2) / 2;
                            return (
                                <option key={val} value={val}>
                                    {val}점
                                </option>
                            );
                        })}
                    </select>
                </div>
                <div>
                    <label className="block mb-1">내용</label>
                    <textarea
                        value={content}
                        onChange={(e) => setContent(e.target.value)}
                        className="w-full border rounded px-3 py-2 h-32"
                        required
                    ></textarea>
                </div>
                <div>
                    <label className="block mb-1">이미지 첨부</label>
                    <input
                        type="file"
                        accept="image/*"
                        multiple
                        onChange={handleImageChange}
                        className="block"
                    />
                </div>
                <button
                    type="submit"
                    disabled={loading}
                    className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
                >
                    {loading ? '등록 중...' : '리뷰 등록'}
                </button>
                {message && <p className="mt-2 text-red-500">{message}</p>}
            </form>
        </div>
    );
}

export default ReviewPage;
