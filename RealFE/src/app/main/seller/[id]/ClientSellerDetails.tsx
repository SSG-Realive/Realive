"use client";

import React, { useState, useEffect, useRef, useCallback } from 'react';
import Link from 'next/link';
import { ReviewResponseDTO } from '@/types/reviews/reviewResponseDTO';
import { ProductListDTO } from '@/types/seller/product/product';
import {
    getSellerReviews as fetchSellerReviewsService,
    getSellerProducts as fetchSellerProductsService
} from '@/service/seller/sellerService';
import ImageWithFallback from '@/components/common/imageWithFallback';
import { getTrafficLightBgClass, getTrafficLightEmoji, getTrafficLightText } from '@/types/admin/review';

interface ClientSellerDetailsProps {
    sellerId: number;
    initialReviews: ReviewResponseDTO[] | null;
    initialHasMoreReviews: boolean;
    initialProducts: ProductListDTO[] | null;
    initialHasMoreProducts: boolean;
}

const REVIEW_SUMMARY_LENGTH = 100;

export default function ClientSellerDetails({
    sellerId,
    initialReviews,
    initialHasMoreReviews,
    initialProducts,
    initialHasMoreProducts
}: ClientSellerDetailsProps) {
    const [reviews, setReviews] = useState<ReviewResponseDTO[]>(initialReviews || []);
    const [products, setProducts] = useState<ProductListDTO[]>(initialProducts || []);
    const [reviewPage, setReviewPage] = useState<number>(0); // 0-based indexing
    const [productPage, setProductPage] = useState<number>(0); // 0-based indexing
    const [hasMoreReviews, setHasMoreReviews] = useState<boolean>(initialHasMoreReviews);
    const [hasMoreProducts, setHasMoreProducts] = useState<boolean>(initialHasMoreProducts);
    const [loadingReviews, setLoadingReviews] = useState<boolean>(false);
    const [loadingProducts, setLoadingProducts] = useState<boolean>(false);
    const [expandedReviews, setExpandedReviews] = useState<Record<number, boolean>>({});
    
    // 별도의 observer refs for reviews and products
    const reviewObserverTarget = useRef<HTMLDivElement>(null);
    const productObserverTarget = useRef<HTMLDivElement>(null);

    const toggleReviewExpansion = (reviewId: number) => {
        setExpandedReviews(prev => ({
            ...prev,
            [reviewId]: !prev[reviewId]
        }));
    };

    const loadMoreReviews = useCallback(async () => {
        if (loadingReviews || !hasMoreReviews) return;
        
        setLoadingReviews(true);
        try {
            const nextPage = reviewPage + 1;
            const { reviews: newReviews, hasMore } = await fetchSellerReviewsService(sellerId, nextPage);

            if (newReviews && newReviews.length > 0) {
                setReviews(prev => {
                    // 중복 제거: 기존 리뷰 ID들을 Set으로 만들어서 체크
                    const existingIds = new Set(prev.map(r => r.reviewId));
                    const uniqueNewReviews = newReviews.filter(review => !existingIds.has(review.reviewId));
                    
                    // 중복이 제거된 새 리뷰가 있을 때만 추가
                    if (uniqueNewReviews.length > 0) {
                        return [...prev, ...uniqueNewReviews];
                    }
                    return prev;
                });
                setReviewPage(nextPage);
                setHasMoreReviews(hasMore);
            } else {
                setHasMoreReviews(false);
            }
        } catch (err) {
            console.error("리뷰 추가 로드 실패:", err);
            setHasMoreReviews(false);
        } finally {
            setLoadingReviews(false);
        }
    }, [sellerId, reviewPage, loadingReviews, hasMoreReviews]);


    const loadMoreProducts = useCallback(async () => {
        if (loadingProducts || !hasMoreProducts) return;
        
        setLoadingProducts(true);
        try {
            const nextPage = productPage + 1;
            const { products: newProducts, hasMore } = await fetchSellerProductsService(sellerId, nextPage);

            if (newProducts && newProducts.length > 0) {
                setProducts(prev => {
                    // 중복 제거: 기존 상품 ID들을 Set으로 만들어서 체크
                    const existingIds = new Set(prev.map(p => p.id));
                    const uniqueNewProducts = newProducts.filter(product => !existingIds.has(product.id));
                    
                    // 중복이 제거된 새 상품이 있을 때만 추가
                    if (uniqueNewProducts.length > 0) {
                        return [...prev, ...uniqueNewProducts];
                    }
                    return prev;
                });
                setProductPage(nextPage);
                setHasMoreProducts(hasMore);
            } else {
                setHasMoreProducts(false);
            }
        } catch (err) {
            console.error("상품 추가 로드 실패:", err);
            setHasMoreProducts(false);
        } finally {
            setLoadingProducts(false);
        }
    }, [sellerId, productPage, loadingProducts, hasMoreProducts]);

    // 리뷰 무한 스크롤 observer
    useEffect(() => {
        const reviewObserver = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && hasMoreReviews && !loadingReviews) {
                loadMoreReviews();
            }
        }, { 
            threshold: 0.1,
            rootMargin: '50px'
        });

        if (reviewObserverTarget.current) {
            reviewObserver.observe(reviewObserverTarget.current);
        }

        return () => {
            if (reviewObserverTarget.current) {
                reviewObserver.unobserve(reviewObserverTarget.current);
            }
        };
    }, [loadMoreReviews, hasMoreReviews, loadingReviews]);

    // 상품 무한 스크롤 observer
    useEffect(() => {
        const productObserver = new IntersectionObserver((entries) => {
            if (entries[0].isIntersecting && hasMoreProducts && !loadingProducts) {
                loadMoreProducts();
            }
        }, { 
            threshold: 0.1,
            rootMargin: '50px'
        });

        if (productObserverTarget.current) {
            productObserver.observe(productObserverTarget.current);
        }

        return () => {
            if (productObserverTarget.current) {
                productObserver.unobserve(productObserverTarget.current);
            }
        };
    }, [loadMoreProducts, hasMoreProducts, loadingProducts]);



    return (
        <>
            <div className="mb-12 p-6 bg-black-50 rounded-2xl shadow-lg border border-blue-100">
            <h2 className="text-sm font-light text-blue-900 mb-6 border-b border-blue-200 pb-2">
                판매자 리뷰
            </h2>

            {reviews.length === 0 && !loadingReviews ? (
                <p className="text-gray-500">아직 작성된 리뷰가 없습니다.</p>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 gap-1 sm:gap-5">
                {/* gap: 모바일에서 1, sm 이상 5로 */}
                {reviews.map((review) => {
                    const isExpanded = expandedReviews[review.reviewId];
                    const displayContent = isExpanded
                    ? review.content
                    : review.content.length > REVIEW_SUMMARY_LENGTH
                    ? review.content.substring(0, REVIEW_SUMMARY_LENGTH) + '...'
                    : review.content;

                    return (
                    <div
                        key={review.reviewId}
                        className="rounded-xl bg-white p-3 shadow-md border border-gray-100 sm:p-4 sm:mb-2"
                    >
                        <div className="flex justify-end mb-2">
                        <div
                            className={`flex items-center gap-1 px-2 py-0.5 rounded-full border ${getTrafficLightBgClass(
                            review.rating
                            )} bg-white`}
                        >
                            <span className="text-sm sm:text-base">{getTrafficLightEmoji(review.rating)}</span>
                            <span className="text-xs sm:text-sm text-gray-700">{getTrafficLightText(review.rating)}</span>
                        </div>
                        </div>

                        <p className="text-gray-800 text-xs sm:text-sm mb-2 leading-relaxed">
                        {displayContent}
                        {review.content.length > REVIEW_SUMMARY_LENGTH && (
                            <button
                            onClick={() => toggleReviewExpansion(review.reviewId)}
                            className="text-indigo-600 hover:underline ml-1 text-xs"
                            >
                            {isExpanded ? '간략히' : '더보기'}
                            </button>
                        )}
                        </p>

                        {review.imageUrls?.length > 0 && (
                        <div className="flex flex-wrap gap-2 mb-2">
                            {review.imageUrls.map((url, idx) => (
                            <ImageWithFallback
                                key={idx}
                                src={url}
                                alt={`리뷰 이미지 ${idx + 1}`}
                                className="w-12 h-12 sm:w-16 sm:h-16 object-cover rounded-md"
                                fallbackSrc="/images/placeholder.jpg"
                            />
                            ))}
                        </div>
                        )}

                        <div className="text-gray-500 text-[10px] sm:text-xs flex justify-between">
                        <span>고객 ID: {review.customerId}</span>
                        <span>{new Date(review.createdAt).toLocaleDateString('ko-KR')}</span>
                        </div>
                    </div>
                    );
                })}
                </div>
            )}
            


            {/* 더보기 버튼 */}
            {hasMoreReviews && !loadingReviews && (
            <div className="text-center mt-8">
                <button
                onClick={loadMoreReviews}
                className="px-5 py-2 rounded-md bg-indigo-600 hover:bg-indigo-700 text-white font-light shadow-sm"
                >
                리뷰 더보기
                </button>
            </div>
            )}

            {loadingReviews && <p className="text-center text-sm text-gray-500 mt-4">리뷰 로딩 중...</p>}
            {!hasMoreReviews && reviews.length > 0 && (
            <p className="text-center text-gray-400 text-sm mt-4"> 더 이상 리뷰가 없습니다.</p>
            )}

            {/* 리뷰 무한 스크롤 observer 타겟 */}
            {hasMoreReviews && <div ref={reviewObserverTarget} className="h-1" />}
        </div>

        {/* 상품 영역 */}
        <div className="mb-12 p-6 bg-white rounded-2xl shadow-lg border border-gray-200">
            <h2 className="text-sm font-light text-gray-800 mb-6 border-b border-gray-300 pb-2">
            상품 목록
            </h2>

            {products.length === 0 && !loadingProducts ? (
            <p className="text-gray-500">등록된 상품이 없습니다.</p>
            ) : (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {products.map((product) => {
                const isInactive = !product.active;

                return (
                <Link
                    key={product.id}
                    href={`/main/products/${product.id}`}
                    className={`rounded-xl overflow-hidden shadow hover:shadow-lg transition flex flex-col ${
                    isInactive ? 'opacity-50' : ''
                    }`}
                >
                    <ImageWithFallback
                    src={product.imageThumbnailUrl}
                    alt={product.name}
                    className="w-full h-30 sm:h-48 object-cover"
                    fallbackSrc={`https://placehold.co/400x300/e0e0e0/555555?text=${product.name}`}
                    />

                    <div className="p-2 sm:p-4 bg-gray-50 flex-grow flex flex-col justify-between gap-0.5 sm:gap-1">
                    <h3 className="text-xs sm:text-base font-light line-clamp-2 text-gray-800">
                        {product.name}
                    </h3>
                    <p className="text-xs sm:text-base text-gray-700">{product.price?.toLocaleString()}원</p>
                    <p className="text-[11px] sm:text-sm text-gray-500">상태: {product.status}</p>
                    </div>

                </Link>
                );
            })}
            </div>

            )}

            {loadingProducts && <p className="text-center text-sm text-gray-500 mt-4">상품 로딩 중...</p>}
            {!hasMoreProducts && products.length > 0 && (
            <p className="text-center text-gray-400 text-sm mt-4"> 더 이상 상품이 없습니다.</p>
            )}

            {/* 상품 무한 스크롤 observer 타겟 */}
            {hasMoreProducts && <div ref={productObserverTarget} className="h-1" />}
        </div>
        </>


    );
}