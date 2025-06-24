"use client";

import React, { useState, useEffect, FormEvent } from 'react';
import { useRouter } from 'next/router';
import { ReviewCreateRequestDTO } from "@/types/reviews/reviewCreateRequestDTO";
import { ReviewResponseDTO } from "@/types/reviews/reviewResponseDTO";
import Navbar from "@/components/customer/Navbar";
import Sidebar from "@/components/Sidebar";

const NewReviewPage = () => {
    const router = useRouter();
    const { orderId } = router.query;

    const [reviewContent, setReviewContent] = useState<string>('');
    const [reviewRating, setReviewRating] = useState<number>(5);
    const [sellerId, setSellerId] = useState<number | null>(null); // sellerId 상태 추가

    const [isLoading, setIsLoading] = useState<boolean>(true); // 초기 로딩 상태 true로 변경
    const [error, setError] = useState<string | null>(null);
    const [success, setSuccess] = useState<boolean>(false);
    const [hasReviewed, setHasReviewed] = useState<boolean>(false); // 리뷰 작성 여부 상태

    useEffect(() => {
        if (!orderId || Array.isArray(orderId)) {
            setIsLoading(false);
            return;
        }

        const fetchOrderAndReviewStatus = async () => {
            setIsLoading(true);
            setError(null);
            try {
                // 1. 주문 상세 정보를 가져와 sellerId 획득
                // 이 API 경로는 실제 백엔드의 주문 상세 조회 API와 일치해야 합니다.
                // 이 예시에서는 /api/customer/orders/{orderId}가 sellerId를 포함한 주문 정보를 반환한다고 가정합니다.
                const orderResponse = await fetch(`/api/customer/orders/${orderId}`);
                if (!orderResponse.ok) {
                    throw new Error('주문 정보를 불러오는 데 실패했습니다.');
                }
                const orderData = await orderResponse.json();
                if (orderData.sellerId) {
                    setSellerId(orderData.sellerId);
                } else {
                    throw new Error('주문 정보에서 판매자 ID를 찾을 수 없습니다.');
                }

                // 2. 이미 리뷰를 작성했는지 체크
                // 백엔드의 checkReviewExistence 엔드포인트는 customerId를 @AuthenticationPrincipal로 받으므로
                // 프론트에서 customerId를 쿼리 파라미터로 보내지 않아도 됩니다.
                const reviewCheckResponse = await fetch(`/api/customer/reviews/check?orderId=${orderId}`);
                if (!reviewCheckResponse.ok) {
                    // 에러 응답 시, 로그인 필요 등의 메시지를 처리할 수 있습니다.
                    const errorData = await reviewCheckResponse.json();
                    if (reviewCheckResponse.status === 401) {
                        // 로그인 필요 처리 또는 리다이렉트
                        setError(errorData.message || '로그인이 필요합니다.');
                        return;
                    }
                    throw new Error(errorData.message || '리뷰 작성 여부 확인에 실패했습니다.');
                }
                const reviewCheckData = await reviewCheckResponse.json();
                setHasReviewed(reviewCheckData.hasReview);

            } catch (err: any) {
                console.error('데이터 로딩 오류:', err);
                setError(err.message || '데이터를 불러오는 중 오류가 발생했습니다.');
            } finally {
                setIsLoading(false);
            }
        };

        fetchOrderAndReviewStatus();
    }, [orderId, router]);


    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setIsLoading(true);
        setError(null);
        setSuccess(false);

        if (!reviewContent.trim()) {
            setError('내용을 입력해주세요.');
            setIsLoading(false);
            return;
        }

        if (Array.isArray(orderId) || !orderId) {
            setError('주문 ID를 찾을 수 없습니다.');
            setIsLoading(false);
            return;
        }

        if (sellerId === null) {
            setError('판매자 정보를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.');
            setIsLoading(false);
            return;
        }

        if (hasReviewed) {
            setError('이미 이 주문에 대한 리뷰를 작성하셨습니다.');
            setIsLoading(false);
            return;
        }

        try {
            const requestBody: ReviewCreateRequestDTO = {
                orderId: Number(orderId),
                sellerId: sellerId, // 이제 상태에서 가져온 sellerId 사용
                rating: reviewRating,
                content: reviewContent,
                imageUrls: [], // 이미지 업로드 로직이 있다면 여기에 추가
            };

            const response = await fetch('/api/customer/reviews', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    // 'Authorization': `Bearer ${token}`, // 인증 토큰이 필요하다면 추가
                },
                body: JSON.stringify(requestBody),
            });

            if (!response.ok) {
                const errorData = await response.json();
                throw new Error(errorData.message || '리뷰 작성에 실패했습니다.');
            }

            const data: ReviewResponseDTO = await response.json();
            console.log('리뷰 작성 성공:', data);
            setSuccess(true);
            setHasReviewed(true); // 성공 시 리뷰 작성 상태 업데이트

            alert('리뷰가 성공적으로 작성되었습니다.');
            router.push(`/customer/orders/${orderId}`); // 리뷰 작성 후 주문 상세 페이지로 이동
        } catch (err: any) {
            console.error('리뷰 작성 오류:', err);
            setError(err.message || '알 수 없는 오류가 발생했습니다.');
        } finally {
            setIsLoading(false);
        }
    };

    // 로딩 중 UI
    if (isLoading) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Navbar />
                <div className="flex-1 p-5 flex flex-col items-center justify-center">
                    <p className="text-xl text-gray-700">정보를 불러오는 중...</p>
                </div>
            </div>
        );
    }

    // 에러 발생 또는 이미 리뷰 작성 완료 UI
    if (error || hasReviewed) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Navbar />
                <div className="flex-1 p-5 flex flex-col items-center justify-center">
                    {error && <p className="text-red-600 text-xl mb-4">{error}</p>}
                    {hasReviewed && !error && (
                        <p className="text-xl text-gray-700 mb-4">이미 이 주문에 대한 리뷰를 작성하셨습니다.</p>
                    )}
                    <button
                        onClick={() => router.push(`/customer/orders/${orderId}`)}
                        className="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
                    >
                        주문 상세 보기
                    </button>
                </div>
            </div>
        );
    }

    // 리뷰 작성 폼 UI
    return (
        <div className="flex flex-col min-h-screen bg-gray-100">
            <Navbar />
            <div className="flex flex-1">
                <Sidebar />
                <div className="flex-1 p-5 flex flex-col items-center bg-white m-5 rounded-lg shadow-md">
                    <h1 className="text-2xl font-semibold text-gray-800 mb-8">리뷰 작성</h1>
                    <form onSubmit={handleSubmit} className="w-full max-w-2xl flex flex-col gap-5">
                        {/* 별점 입력 UI */}
                        <div className="flex items-center gap-2 mb-4">
                            <label htmlFor="reviewRating" className="text-lg font-medium text-gray-700">별점:</label>
                            <select
                                id="reviewRating"
                                className="p-2 border border-gray-300 rounded-md bg-white text-gray-800"
                                value={reviewRating}
                                onChange={(e) => setReviewRating(Number(e.target.value))}
                                disabled={isLoading}
                            >
                                <option value={5}>⭐⭐⭐⭐⭐</option>
                                <option value={4}>⭐⭐⭐⭐</option>
                                <option value={3}>⭐⭐⭐</option>
                                <option value={2}>⭐⭐</option>
                                <option value={1}>⭐</option>
                            </select>
                        </div>

                        <div>
                            <label htmlFor="reviewContent" className="sr-only">리뷰 작성칸</label>
                            <textarea
                                id="reviewContent"
                                className="w-full p-4 border border-gray-300 rounded-md text-base bg-gray-200 text-gray-800 min-h-[400px] resize-y"
                                placeholder="리뷰 작성칸"
                                value={reviewContent}
                                onChange={(e) => setReviewContent(e.target.value)}
                                rows={10}
                                disabled={isLoading}
                            />
                        </div>

                        {success && <p className="text-green-600 text-sm mt-2 text-center">리뷰 작성이 완료되었습니다!</p>}

                        <button
                            type="submit"
                            className="self-end px-8 py-4 bg-blue-600 text-white font-bold rounded-lg text-lg cursor-pointer hover:bg-blue-700 transition-colors mt-5 disabled:bg-blue-300 disabled:cursor-not-allowed"
                            disabled={isLoading}
                        >
                            {isLoading ? '등록 중...' : '등록'}
                        </button>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default NewReviewPage;