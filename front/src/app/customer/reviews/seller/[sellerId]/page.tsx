// app/customer/reviews/seller/[sellerId]/page.tsx

import React from 'react';
import Navbar from "@/components/customer/common/Navbar";
import Sidebar from "@/components/seller/SellerSidebar";
import Link from 'next/link';
import {ReviewListResponseDTO} from "@/types/reviews/reviewListResponseDTO";
import {ReviewResponseDTO} from "@/types/reviews/reviewResponseDTO";

async function SellerReviewPage({
                                    params,
                                    searchParams
                                }: {
    params: { sellerId: string }; // URL 경로에서 sellerId를 받음
    searchParams: { [key: string]: string | string[] | undefined }; // 쿼리 파라미터는 여전히 searchParams로 받음
}) {
    const sellerId = params.sellerId; // URL 경로에서 sellerId를 가져옴

    // 페이지네이션을 위한 쿼리 파라미터
    const page = typeof searchParams.page === 'string' ? parseInt(searchParams.page, 10) : 0;
    const size = typeof searchParams.size === 'string' ? parseInt(searchParams.size, 10) : 10;

    const query = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    }).toString();

    let reviewListData: ReviewListResponseDTO | null = null;
    let error: string | null = null;

    if (!sellerId) {
        error = "판매자 ID가 제공되지 않았습니다.";
    } else {
        try {
            // API 경로: sellerId가 경로 변수로 들어가도록 구성
            const res = await fetch(
                `http://localhost:8080/api/customer/reviews/seller/${sellerId}?${query}`,
                { next: { revalidate: 60 } }
            );

            if (!res.ok) {
                const errorData = await res.json();
                throw new Error(errorData.message || `리뷰 목록을 불러오는 데 실패했습니다: ${res.status}`);
            }
            reviewListData = await res.json();
            console.log('불러온 리뷰 목록 데이터:', reviewListData);

        } catch (err: any) {
            console.error('리뷰 목록 패치 오류:', err);
            error = err.message || '리뷰 목록을 불러오는 중 알 수 없는 오류가 발생했습니다.';
        }
    }


    if (error) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Navbar />
                <div className="flex-1 p-5 flex flex-col items-center justify-center">
                    <p className="text-red-600 text-xl">{error}</p>
                </div>
            </div>
        );
    }

    if (!reviewListData || reviewListData.reviews.length === 0) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Navbar />
                <div className="flex flex-1">
                    <Sidebar />
                    <div className="flex-1 p-5 flex flex-col items-center bg-white m-5 rounded-lg shadow-md">
                        <h1 className="text-3xl font-bold text-gray-800 mb-8">리뷰 목록</h1>
                        <div className="bg-gray-100 p-6 rounded-lg w-full max-w-4xl text-center">
                            <p className="text-gray-600 text-lg">아직 작성된 리뷰가 없습니다.</p>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    const totalPages = Math.ceil(reviewListData.totalCount / reviewListData.size);
    const currentPage = reviewListData.page;

    const renderStars = (rating: number) => {
        let stars = '';
        for (let i = 0; i < rating; i++) {
            stars += '⭐';
        }
        return stars;
    };

    const formatDateTime = (isoString: string) => {
        try {
            const date = new Date(isoString);
            return date.toLocaleDateString('ko-KR');
        } catch (e) {
            return isoString;
        }
    };

    return (
        <div className="flex flex-col min-h-screen bg-gray-100">
            <Navbar />
            <div className="flex flex-1">
                <Sidebar />
                <div className="flex-1 p-5 flex flex-col items-center bg-white m-5 rounded-lg shadow-md">
                    <h1 className="text-3xl font-bold text-gray-800 mb-8">리뷰 목록</h1>

                    <div className="bg-gray-200 p-4 rounded-md w-full max-w-4xl text-center mb-6">
                        <span className="text-xl font-semibold text-gray-800">리뷰 목록</span>
                    </div>

                    <div className="w-full max-w-4xl flex flex-col gap-4">
                        {reviewListData.reviews.map((review, index) => (
                            <Link href={`/customer/reviews/${review.reviewId}`} key={review.reviewId} passHref>
                                <div className="bg-gray-100 p-6 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer flex items-center">
                                    <span className="text-2xl font-bold text-gray-700 mr-4">
                                        {reviewListData.page * reviewListData.size + index + 1}.
                                    </span>
                                    <div className="flex-1">
                                        <h3 className="text-xl font-semibold text-gray-900 mb-2 truncate">
                                            {review.productName}
                                        </h3>
                                        <div className="flex items-center text-gray-600 text-sm">
                                            <span className="text-yellow-500 mr-2">{renderStars(review.rating)}</span>
                                            <span>{formatDateTime(review.createdAt)}</span>
                                        </div>
                                    </div>
                                </div>
                            </Link>
                        ))}
                    </div>

                    {/* 페이지네이션 컨트롤 */}
                    {totalPages > 1 && (
                        <div className="flex justify-center items-center mt-8 space-x-2">
                            <Link
                                href={`/customer/reviews/seller/${sellerId}?page=${currentPage - 1}&size=${size}`}
                                passHref
                            >
                                <button
                                    className={`px-4 py-2 rounded-md transition-colors ${
                                        currentPage === 0
                                            ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
                                            : 'bg-blue-500 text-white hover:bg-blue-600'
                                    }`}
                                    disabled={currentPage === 0}
                                >
                                    이전
                                </button>
                            </Link>
                            {Array.from({ length: totalPages }, (_, i) => (
                                <Link
                                    href={`/customer/reviews/seller/${sellerId}?page=${i}&size=${size}`}
                                    key={i}
                                    passHref
                                >
                                    <button
                                        className={`px-4 py-2 rounded-md transition-colors ${
                                            currentPage === i
                                                ? 'bg-blue-700 text-white font-bold'
                                                : 'bg-gray-200 text-gray-800 hover:bg-gray-300'
                                        }`}
                                    >
                                        {i + 1}
                                    </button>
                                </Link>
                            ))}
                            <Link
                                href={`/customer/reviews/seller/${sellerId}?page=${currentPage + 1}&size=${size}`}
                                passHref
                            >
                                <button
                                    className={`px-4 py-2 rounded-md transition-colors ${
                                        currentPage >= totalPages - 1
                                            ? 'bg-gray-300 text-gray-600 cursor-not-allowed'
                                            : 'bg-blue-500 text-white hover:bg-blue-600'
                                    }`}
                                    disabled={currentPage >= totalPages - 1}
                                >
                                    다음
                                </button>
                            </Link>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}

export default SellerReviewPage;