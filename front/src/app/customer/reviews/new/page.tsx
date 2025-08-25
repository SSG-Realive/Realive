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

    // 토큰을 안전하게 가져오고 파싱하는 함수
    const getAccessToken = () => {
        let pureAccessToken = null;
        try {
            const storedData = localStorage.getItem('auth-storage'); // 'auth-storage' 키 사용
            console.log('localStorage에서 가져온 raw 데이터 (auth-storage):', storedData);

            if (storedData) {
                const parsedData = JSON.parse(storedData); // JSON 문자열 파싱
                // JSON 구조가 {"state":{"accessToken":"..."}} 형태라고 가정합니다.
                pureAccessToken = parsedData?.state?.accessToken;
            }
        } catch (e) {
            console.error('localStorage 데이터 파싱 오류:', e);
            pureAccessToken = null; // 파싱 실패 시 토큰 없음으로 처리
        }
        console.log('추출된 순수 JWT Access Token:', pureAccessToken);
        return pureAccessToken;
    };

    // 초기 로딩 및 리뷰 중복 체크
    useEffect(() => {
        if (orderIdParam && sellerIdParam) {
            const parsedOrderId = Number(orderIdParam);
            const parsedSellerId = Number(sellerIdParam);
            setOrderId(parsedOrderId);
            setSellerId(parsedSellerId);

            // Access Token 가져오기
            const accessToken = getAccessToken(); // 함수 호출

            const headers: Record<string, string> = {
                'Content-Type': 'application/json', // 명시적 Content-Type
            };

            if (accessToken) {
                headers.Authorization = `Bearer ${accessToken}`; // 순수 Access Token 사용
            }
            console.log('최종 요청 헤더:', headers);

            // 중복 체크 API 호출
            axios
                .get('http://localhost:8080/api/customer/reviews/check-exists', {
                    params: {
                        orderId: parsedOrderId,
                        sellerId: parsedSellerId,
                    },
                    withCredentials: true,
                    headers: headers,
                })
                .then((res) => {
                    setAlreadyReviewed(res.data.exists);
                })
                .catch((err) => {
                    console.error('중복 체크 실패:', err);
                    setAlreadyReviewed(null);
                    console.error('상세 에러 객체:', err);
                    // 401 Unauthorized 에러라면 로그인 페이지로 리디렉션 등을 고려할 수 있습니다.
                    if (axios.isAxiosError(err) && err.response?.status === 401) {
                        alert('로그인이 필요하거나 세션이 만료되었습니다.');
                        router.push('/login'); // 로그인 페이지 경로로 변경하세요.
                    }
                });
        }
    }, [orderIdParam, sellerIdParam, router]); // router를 의존성 배열에 추가

    const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!e.target.files) return;
        setImages(Array.from(e.target.files));
    };

    const uploadImages = async () => {
        const urls: string[] = [];
        const accessToken = getAccessToken(); // 이미지 업로드 시에도 토큰 필요

        for (const file of images) {
            const formData = new FormData();
            formData.append('file', file);
            try {
                const uploadHeaders: Record<string, string> = {
                    'Content-Type': 'multipart/form-data',
                };
                if (accessToken) {
                    uploadHeaders.Authorization = `Bearer ${accessToken}`;
                }

                // 주의: /api/uploads가 Next.js rewrites 규칙에 의해 백엔드로 전달되거나
                // Next.js API Route로 직접 처리되는지 확인해야 합니다.
                // 만약 백엔드로 직접 보내야 한다면 'http://localhost:8080/uploads'로 변경하세요.
                const res = await axios.post('/api/uploads', formData, {
                    headers: uploadHeaders,
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

            const accessToken = getAccessToken(); // 리뷰 등록 시에도 토큰 필요

            const submitHeaders: Record<string, string> = {
                'Content-Type': 'application/json',
            };
            if (accessToken) {
                submitHeaders.Authorization = `Bearer ${accessToken}`;
            }

            // 주의: /api/reviews가 Next.js rewrites 규칙에 의해 백엔드로 전달되거나
            // Next.js API Route로 직접 처리되는지 확인해야 합니다.
            // 이전 에러에서 /api/reviews/check-exists를 'http://localhost:8080/api/reviews/check-exists'로 변경하여 해결했으므로,
            // 여기도 'http://localhost:8080/api/reviews'로 변경하는 것이 일관성 있습니다.
            await axios.post('http://localhost:8080/api/customer/reviews', payload, { // <-- 이 부분을 직접 백엔드 주소로 변경
                headers: submitHeaders,
            });
            setMessage('리뷰가 성공적으로 등록되었습니다.');
            router.push('/customer/reviews/my');
        } catch (error) {
            console.error('리뷰 등록 실패:', error);
            setMessage('리뷰 등록 중 오류가 발생했습니다.');
            if (axios.isAxiosError(error) && error.response) {
                console.error('리뷰 등록 백엔드 응답 데이터:', error.response.data);
                if (error.response.status === 401) {
                    alert('로그인이 필요하거나 세션이 만료되었습니다.');
                    router.push('/login'); // 로그인 페이지 경로로 변경하세요.
                }
            }
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