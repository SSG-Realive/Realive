// app/customer/reviews/my/page.tsx
import React from 'react';
import Navbar from "@/components/customer/Navbar";
import Sidebar from "@/components/Sidebar";
import Link from 'next/link';
// ReviewResponseDTO는 MyReviewListResponse 안에 포함되므로 직접 임포트할 필요 없을 수도 있음.
// 하지만 renderStars, formatDateTime 등에서 ReviewResponseDTO 필드를 직접 참조한다면 임포트.
import { ReviewResponseDTO } from "@/types/reviews/reviewResponseDTO";
import {MyReviewListResponse} from "@/types/reviews/myReviewListResponseDTO";


async function MyReviewsPage({
                                 searchParams
                             }: {
    searchParams: { [key: string]: string | string[] | undefined };
}) {
    const page = typeof searchParams.page === 'string' ? parseInt(searchParams.page, 10) : 0;
    const size = typeof searchParams.size === 'string' ? parseInt(searchParams.size, 10) : 10;

    const query = new URLSearchParams({
        page: page.toString(),
        size: size.toString(),
    }).toString();

    let myReviewListData: MyReviewListResponse | null = null; // MyReviewListResponse 사용
    let error: string | null = null;
    let authError: boolean = false;

    try {
        const res = await fetch(
            `http://localhost:8080/api/customer/reviews/my?${query}`,
            {
                next: { revalidate: 60 },
                // headers: { 'Authorization': `Bearer ${yourAuthToken}` }
            }
        );

        if (res.status === 401) {
            authError = true;
            throw new Error("로그인이 필요합니다. 리뷰를 보려면 로그인해주세요.");
        }

        if (!res.ok) {
            const errorData = await res.json();
            throw new Error(errorData.message || `내가 작성한 리뷰 목록을 불러오는 데 실패했습니다: ${res.status}`);
        }
        myReviewListData = await res.json();
        console.log('불러온 내 리뷰 목록 데이터:', myReviewListData);

    } catch (err: any) {
        console.error('내 리뷰 목록 패치 오류:', err);
        error = err.message || '내가 작성한 리뷰 목록을 불러오는 중 알 수 없는 오류가 발생했습니다.';
    }

    if (error) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Navbar />
                <div className="flex-1 p-5 flex flex-col items-center justify-center">
                    <p className="text-red-600 text-xl">{error}</p>
                    {authError && (
                        <p className="text-gray-700 mt-2">로그인 페이지로 이동하시려면 아래 버튼을 클릭해주세요.</p>
                    )}
                </div>
            </div>
        );
    }

    // myReviewListData.content.length 로 변경 (Spring Page 객체에 맞춤)
    if (!myReviewListData || myReviewListData.content.length === 0) {
        return (
            <div className="flex flex-col min-h-screen bg-gray-100">
                <Navbar />
                <div className="flex flex-1">
                    <Sidebar />
                    <div className="flex-1 p-5 flex flex-col items-center bg-white m-5 rounded-lg shadow-md">
                        <h1 className="text-3xl font-bold text-gray-800 mb-8">내가 작성한 리뷰</h1>
                        <div className="bg-gray-100 p-6 rounded-lg w-full max-w-4xl text-center">
                            <p className="text-gray-600 text-lg">아직 작성하신 리뷰가 없습니다.</p>
                        </div>
                    </div>
                </div>
            </div>
        );
    }

    // totalPages, currentPage 필드명 변경 (Spring Page 객체에 맞춤)
    const totalPages = myReviewListData.totalPages;
    const currentPage = myReviewListData.number; // Spring Page 객체는 'number'가 0-based 현재 페이지

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
                    <h1 className="text-3xl font-bold text-gray-800 mb-8">내가 작성한 리뷰</h1>

                    <div className="bg-gray-200 p-4 rounded-md w-full max-w-4xl text-center mb-6">
                        <span className="text-xl font-semibold text-gray-800">내가 작성한 리뷰 목록</span>
                    </div>

                    <div className="w-full max-w-4xl flex flex-col gap-4">
                        {myReviewListData.content.map((review, index) => ( // myReviewListData.content 사용
                            <Link href={`/customer/reviews/${review.reviewId}`} key={review.reviewId} passHref>
                                <div className="bg-gray-100 p-6 rounded-lg shadow-sm hover:shadow-md transition-shadow cursor-pointer flex items-center">
                                    <span className="text-2xl font-bold text-gray-700 mr-4">
                                        {/* 페이지 번호는 myReviewListData.number 사용 */}
                                        {myReviewListData.number * myReviewListData.size + index + 1}.
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
                                href={`/customer/reviews/my?page=${currentPage - 1}&size=${size}`}
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
                                    href={`/customer/reviews/my?page=${i}&size=${size}`}
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
                                href={`/customer/reviews/my?page=${currentPage + 1}&size=${size}`}
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

export default MyReviewsPage;