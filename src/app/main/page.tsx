'use client';

import { useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { fetchPublicProducts, fetchPopularProducts } from '@/service/customer/productService';
import { ProductListDTO } from '@/types/seller/product/product';
import Navbar from '@/components/customer/common/Navbar';
import ChatbotFloatingButton from '@/components/customer/common/ChatbotFloatingButton';
import ProductCard from '@/components/customer/product/ProductCard';
import BannerCarousel from '@/components/main/BannerCarousel';
import WeeklyAuctionSlider from '@/components/main/WeeklyAuctionSlider';


const categories = [
    { id: null, name: '전체' },
    { id: 1, name: '가구' },
    { id: 2, name: '수납/정리' },
    { id: 3, name: '인테리어 소품' },
    { id: 4, name: '유아/아동' },
];

const ITEMS_PER_PAGE = 20;

export default function CustomerHomePage() {
    const searchParams = useSearchParams();
    const keywordFromUrl = searchParams.get('keyword') || '';

    const [categoryId, setCategoryId] = useState<number | null>(null);
    const [setSearchKeyword] = useState(keywordFromUrl);
    const [products, setProducts] = useState<ProductListDTO[]>([]);
    const [popularProducts, setPopularProducts] = useState<ProductListDTO[]>([]); // ✅ 인기 상품
    const [page, setPage] = useState(1);
    const loader = useRef<HTMLDivElement | null>(null);

    const loadMore = async () => {
        const newProducts = await fetchPublicProducts(
            categoryId,
            page,
            ITEMS_PER_PAGE,
            keywordFromUrl
        );
        setProducts((prev) => {
            // 중복 제거: 기존 상품의 고유 식별자 목록
            const existingKeys = new Set(prev.map(p => `${p.id}-${p.imageThumbnailUrl}`));
            // 새로운 상품 중 중복되지 않는 것만 필터링
            const uniqueNewProducts = newProducts.filter(p => !existingKeys.has(`${p.id}-${p.imageThumbnailUrl}`));
            return [...prev, ...uniqueNewProducts];
        });
    };

    // ✅ 인기 상품 초기 로딩
    useEffect(() => {
        fetchPopularProducts().then(setPopularProducts);
    }, []);

    // 무한 스크롤 감지
    useEffect(() => {
        if (!loader.current) return;

        const observer = new IntersectionObserver(([entry]) => {
            if (entry.isIntersecting) {
                setPage((prev) => prev + 1);
            }
        }, { rootMargin: '100px' });

        observer.observe(loader.current);
        return () => {
            if (loader.current) observer.unobserve(loader.current);
        };
    }, []);

    // 페이지 증가 시 더 불러오기
    useEffect(() => {
        loadMore();
    }, [page]);

    // 카테고리 or 검색어 변경 시 초기화
    useEffect(() => {
        setPage(1);
        setProducts([]); // 기존 상품 목록 완전히 초기화
        fetchPublicProducts(categoryId, 1, ITEMS_PER_PAGE, keywordFromUrl).then(setProducts);
    }, [categoryId, keywordFromUrl]);

    return (
        <div>
            <Navbar />

            {/* 배너 */}
            <div className="mt-10 mb-8"> {/* 여백 추가 */}
                <BannerCarousel />
            </div>

            {/* 옥션-슬라이드 */}
            <WeeklyAuctionSlider />

            {/* 🔥 인기 상품 */}
            {popularProducts.length > 0 && (
                <div className="px-4 mb-8">
                    <h2 className="text-lg font-bold mb-3">인기 상품 🔥</h2>
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                        {popularProducts.map((p, index) => (
                            <ProductCard key={`popular-${p.id}-${p.imageThumbnailUrl}-${index}`} {...p} />
                        ))}
                    </div>
                </div>
            )}

            {/* 🎯 경매 섹션 */}
            <div className="px-4 mb-8">
                <div className="flex justify-between items-center mb-3">
                    <h2 className="text-lg font-bold">진행중인 경매 🎯</h2>
                    <Link 
                        href="/main/auctions" 
                        className="text-green-600 hover:text-green-700 text-sm font-medium"
                    >
                        전체보기 →
                    </Link>
                </div>
                <div className="bg-gradient-to-r from-green-50 to-blue-50 rounded-lg p-6 text-center">
                    <div className="text-2xl mb-2">🏆</div>
                    <h3 className="text-lg font-semibold mb-2">실시간 경매에 참여하세요!</h3>
                    <p className="text-gray-600 mb-4">중고 가구를 경매로 구매하고 특별한 가격을 만나보세요</p>
                    <Link 
                        href="/main/auctions"
                        className="inline-block bg-green-600 text-white px-6 py-2 rounded-lg hover:bg-green-700 transition-colors"
                    >
                        경매 보러가기
                    </Link>
                </div>
            </div>

            {/* 카테고리 필터 */}
            <div className="flex gap-3 overflow-x-auto mb-6 px-4 py-2">
                {categories.map(({ id, name }) => (
                    <button
                        key={id ?? 'all'}
                        onClick={() => {
                            setCategoryId(id);
                            setProducts([]);
                            setPage(1);
                        }}
                        className={`px-4 py-1 rounded-full border text-sm whitespace-nowrap ${
                            categoryId === id
                                ? 'bg-green-600 text-white border-green-600'
                                : 'bg-white text-gray-700 border-gray-300'
                        }`}
                    >
                        {name}
                    </button>
                ))}
            </div>

            {/* 전체 상품 목록 */}
            <div className="px-4 py-6 grid grid-cols-2 md:grid-cols-4 gap-4">
                {products.map((p, index) => (
                    <ProductCard key={`product-${p.id}-${p.imageThumbnailUrl}-${index}`} {...p} />
                ))}
                <div ref={loader} className="h-10 col-span-full" />
            </div>

            <ChatbotFloatingButton />
        </div>
    );
}
