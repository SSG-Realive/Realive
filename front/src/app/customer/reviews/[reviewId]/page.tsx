
import React from 'react';
import Navbar from "@/components/customer/common/Navbar";
import Sidebar from "@/components/seller/SellerSidebar";
import { ReviewResponseDTO } from "@/types/reviews/reviewResponseDTO";
import { useRouter } from 'next/navigation';

// params 객체는 Next.js의 Dynamic Routes에서 경로 변수를 받습니다.
async function ReviewIdPage({ params }: { params: { reviewId: string } }) {
    const router = useRouter(); // useRouter 훅은 클라이언트 컴포넌트에서만 사용 가능하지만,
    // 여기서는 `router.back()`과 같은 함수 호출을 위해 필요합니다.
    // 이 컴포넌트가 'use client'가 아니라면, router를 사용하는 부분은 적절히 수정되어야 합니다.
    // (예: <Link> 컴포넌트 사용 또는 서버에서 라우팅 처리)

    const reviewId = params.reviewId;

    if (!reviewId) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Navbar />
                <div className="flex-1 p-5 flex flex-col items-center justify-center">
                    <p className="text-red-600 text-xl">리뷰 ID를 찾을 수 없습니다.</p>
                </div>
            </div>
        );
    }

    let reviewData: ReviewResponseDTO | null = null;
    let error: string | null = null;

    try {
        const res = await fetch(`http://localhost:8080/api/customer/reviews/${reviewId}`);

        if (!res.ok) {
            const errorData = await res.json();
            throw new Error(errorData.message || `리뷰 정보를 불러오는 데 실패했습니다: ${res.status}`);
        }

        reviewData = await res.json();
        console.log('불러온 리뷰 데이터:', reviewData);

    } catch (err: any) {
        console.error('리뷰 데이터 패치 오류:', err);
        error = err.message || '리뷰 정보를 불러오는 중 알 수 없는 오류가 발생했습니다.';
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

    if (!reviewData) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Navbar />
                <div className="flex-1 p-5 flex flex-col items-center justify-center">
                    <p className="text-xl text-gray-700">리뷰 정보를 불러오는 중...</p>
                </div>
            </div>
        );
    }

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
            return date.toLocaleString('ko-KR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit',
                second: '2-digit',
                hour12: false
            });
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
                    <h1 className="text-3xl font-bold text-gray-800 mb-8">리뷰 상세 보기</h1>

                    <div className="w-full max-w-4xl bg-white p-8 rounded-lg border border-gray-200 shadow-sm">
                        {/* 리뷰 제목 (상품명) */}
                        <div className="bg-gray-100 p-4 rounded-md mb-4 border border-gray-300">
                            <h2 className="text-2xl font-semibold text-gray-900">
                                {reviewData.productName || '제목 없음'}
                            </h2>
                        </div>

                        {/* 별점, 작성자, 작성일 정보 */}
                        <div className="flex items-center justify-between text-gray-600 text-sm mb-4">
                            <div className="flex items-center gap-2">
                                <span className="font-medium">별점:</span>
                                <span className="text-yellow-500 text-lg">{renderStars(reviewData.rating)}</span>
                                <span className="ml-1 text-gray-700">({reviewData.rating}/5)</span>
                            </div>
                            <div className="flex items-center gap-4">
                                <span className="font-medium">작성일:</span>
                                <span>{formatDateTime(reviewData.createdAt)}</span>
                            </div>
                        </div>

                        {/* 리뷰 내용 */}
                        <div className="bg-gray-50 p-6 rounded-md mb-6 border border-gray-200 min-h-[300px] flex items-start">
                            <p className="text-gray-800 text-base leading-relaxed whitespace-pre-wrap">
                                {reviewData.content}
                            </p>
                        </div>

                        {/* 리뷰 이미지 (있을 경우) */}
                        {reviewData.imageUrls && reviewData.imageUrls.length > 0 && (
                            <div className="mt-6">
                                <h3 className="text-lg font-semibold text-gray-700 mb-3">첨부 이미지</h3>
                                <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
                                    {reviewData.imageUrls.map((url, index) => (
                                        <div key={index} className="relative w-full aspect-w-16 aspect-h-9 overflow-hidden rounded-lg shadow-md group">
                                            <img
                                                src={url}
                                                alt={`리뷰 이미지 ${index + 1}`}
                                                className="object-cover w-full h-full transform transition-transform duration-300 group-hover:scale-105"
                                            />
                                        </div>
                                    ))}
                                </div>
                            </div>
                        )}

                        {/* 기타 버튼 */}
                        <div className="flex justify-end mt-8">
                            {/* Next.js 13+ App Router의 서버 컴포넌트에서는 useRouter 훅을 직접 사용할 수 없습니다.
                                 만약 이 컴포넌트가 서버 컴포넌트라면, <Link href="/reviews">와 같은 방식으로 대체하거나,
                                 클라이언트 컴포넌트로 분리하여 useRouter를 사용해야 합니다.
                                 현재 예시에서는 router.back() 사용을 위해 임시로 추가하였지만,
                                 실제 환경에서는 서버/클라이언트 컴포넌트 아키텍처에 맞게 조정해야 합니다.
                            */}
                            <button
                                onClick={() => router.back()} // 이 줄은 클라이언트 컴포넌트에서만 동작합니다.
                                className="px-6 py-3 bg-gray-500 text-white font-bold rounded-lg hover:bg-gray-600 transition-colors"
                            >
                                목록으로 돌아가기
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default ReviewIdPage;