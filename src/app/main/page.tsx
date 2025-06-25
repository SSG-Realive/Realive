'use client';

import { useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import Link from 'next/link';
import { fetchPublicProducts } from '@/service/customer/productService';
import { ProductListDTO } from '@/types/seller/product/product';
import Navbar from '@/components/customer/common/Navbar';
import ChatbotFloatingButton from '@/components/customer/common/ChatbotFloatingButton';
import ProductCard from '@/components/customer/product/ProductCard';
import BannerCarousel from '@/components/main/BannerCarousel';
import WeeklyAuctionSlider from '@/components/main/WeeklyAuctionSlider';
import PopularProductsGrid from '@/components/main/PopularProductsGrid';

const ITEMS_PER_PAGE = 20;

// ✅ 카테고리 ID → 이름 매핑 (추후 API 연동 가능)
const categoryMap: Record<number, string> = {
    10: '거실 가구',
    20: '침실 가구',
    30: '주방·다이닝 가구',
    40: '서재·오피스 가구',
    50: '기타 가구',
};

export default function CustomerHomePage() {
    const searchParams = useSearchParams();
    const categoryFromUrl = searchParams.get('category');
    const keywordFromUrl = searchParams.get('keyword') || '';

    const [categoryId, setCategoryId] = useState<number | null>(null);
    const [keyword, setKeyword] = useState<string>('');
    const [products, setProducts] = useState<ProductListDTO[]>([]);
    const [page, setPage] = useState(1);
    const loader = useRef<HTMLDivElement | null>(null);

    // ✅ URL 파라미터 → 상태 반영
    useEffect(() => {
        setCategoryId(categoryFromUrl ? Number(categoryFromUrl) : null);
        setKeyword(keywordFromUrl);
        setPage(1);
    }, [categoryFromUrl, keywordFromUrl]);

    // ✅ categoryId 또는 keyword가 바뀌면 상품 초기화 후 새로 로딩
    useEffect(() => {
        setProducts([]);
        fetchPublicProducts(categoryId, 1, ITEMS_PER_PAGE, keyword).then(setProducts);
    }, [categoryId, keyword]);

    // ✅ 페이지 증가 → 다음 상품 추가
    useEffect(() => {
        if (page === 1) return;
        fetchPublicProducts(categoryId, page, ITEMS_PER_PAGE, keyword).then((newProducts) => {
            setProducts((prev) => [...prev, ...newProducts]);
        });
    }, [page]);

    // ✅ 무한 스크롤
    useEffect(() => {
        if (!loader.current) return;
        const observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.isIntersecting) {
                    setPage((prev) => prev + 1);
                }
            },
            { rootMargin: '100px' }
        );
        observer.observe(loader.current);
        return () => {
            if (loader.current) observer.unobserve(loader.current);
        };
    }, []);

    return (
        <div>
            <Navbar
                onCategorySelect={(id) => {
                    const query = new URLSearchParams();
                    if (id !== null) query.set('category', String(id));
                    if (keyword) query.set('keyword', keyword);
                    window.location.href = `/main?${query.toString()}`;
                }}
                onSearch={(newKeyword) => {
                    const query = new URLSearchParams();
                    if (categoryId !== null) query.set('category', String(categoryId));
                    if (newKeyword) query.set('keyword', newKeyword);
                    window.location.href = `/main?${query.toString()}`;
                }}
            />

            {/* ✅ 배너 */}
            {!categoryFromUrl && (
                <div className="mb-8">
                    <BannerCarousel />
                </div>
            )}

            {/* ✅ 옥션 */}
            {!categoryFromUrl && (
                <div className="mb-8">
                    <WeeklyAuctionSlider />
                </div>
            )}

            {/* ✅ 인기 상품 */}
            <PopularProductsGrid />

            {/* ✅ 상품 목록 */}
            <section className="max-w-screen-xl mx-auto px-1 py-30">
                <h2 className="text-2xl font-bold text-gray-800 mb-2">
                    {categoryId && categoryMap[categoryId]
                        ? categoryMap[categoryId]
                        : '전체상품'}
                </h2>
                <p className="text-sm text-gray-600 mb-6">
                    다양한 상품을 확인하고 원하는 제품을 찾아보세요.
                </p>

                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-2">
                    {products.map((p, index) => (
                        <ProductCard key={`product-${p.id}-${p.imageThumbnailUrl}-${index}`} {...p} />
                    ))}
                    <div ref={loader} className="h-10 col-span-full" />
                </div>
            </section>

            <ChatbotFloatingButton />
        </div>
    );
}
