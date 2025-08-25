'use client';

import { useEffect, useState } from 'react';
import { usePathname, useSearchParams } from 'next/navigation';
import Link from 'next/link';

import { fetchPublicProducts } from '@/service/customer/productService';
import { fetchAllCategories } from '@/service/categoryService';

import { ProductListDTO } from '@/types/seller/product/product';
import { Category } from '@/types/common/category';

import Navbar from '@/components/customer/common/Navbar';
import ChatbotFloatingButton from '@/components/customer/common/ChatbotFloatingButton';
import ProductCard from '@/components/customer/product/ProductCard';
import BannerCarousel from '@/components/main/BannerCarousel';
import WeeklyAuctionSlider from '@/components/main/WeeklyAuctionSlider';
import PopularProductsGrid from '@/components/main/PopularProductsGrid';
import MiddleBannerCarousel from '@/components/main/MiddleBannerCarousel';
import ExtraBanner from '@/components/main/ExtraBanner';
import SectionWithSubCategoryButtons from "@/components/customer/product/SectionWithSubCategoryButtons";
import FeaturedSellersSection from '@/components/main/FeaturedSellersSection';
import BottomInspirationSlider from '@/components/main/BottomInspirationSlider';

const ITEMS_PER_PAGE = 10;

export default function CustomerHomePage() {
    const pathname = usePathname();
    const searchParams = useSearchParams();

    const categoryFromUrl = searchParams.get('category');
    const keywordFromUrl = searchParams.get('keyword') || '';
    const [categoryId, setCategoryId] = useState<number | null>(null);
    const [keyword, setKeyword] = useState<string>('');
    const [products, setProducts] = useState<ProductListDTO[]>([]);
    const [page, setPage] = useState(1);
    const [showLoadMore, setShowLoadMore] = useState(true);
    const [categoryMap, setCategoryMap] = useState<Record<number, Category>>({});

    const showMainTopBanners = pathname === '/main' && !categoryFromUrl && !keywordFromUrl;

    useEffect(() => {
        fetchAllCategories().then((categories) => {
            const map = Object.fromEntries(categories.map((c) => [c.id, c]));
            setCategoryMap(map);
        });
    }, []);

    useEffect(() => {
        setCategoryId(categoryFromUrl ? Number(categoryFromUrl) : null);
        setKeyword(keywordFromUrl);
    }, [categoryFromUrl, keywordFromUrl]);

    useEffect(() => {
        fetchPublicProducts(categoryId, 1, ITEMS_PER_PAGE, keyword).then((initial) => {
            setProducts(initial);
            setShowLoadMore(initial.length === ITEMS_PER_PAGE);
        });
    }, [categoryId, keyword]);

    const loadMore = async () => {
        const nextPage = page + 1;
        const newProducts = await fetchPublicProducts(categoryId, nextPage, ITEMS_PER_PAGE, keyword);
        setProducts((prev) => [...prev, ...newProducts]);
        setPage(nextPage);
        setShowLoadMore(newProducts.length === ITEMS_PER_PAGE);
    };

    function getTopCategoryName(id: number | null): string {
        if (!id || !categoryMap[id]) return '전체상품';
        let current = categoryMap[id];
        while (current.parentId && categoryMap[current.parentId]) {
            current = categoryMap[current.parentId];
        }
        return current.name;
    }

    return (
        <div className="min-h-screen overflow-x-hidden overflow-y-auto">
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

            {showMainTopBanners && (
                <div className="mb-6 sm:mb-8">
                    <BannerCarousel />
                </div>
            )}

            {!categoryId && (
                <div className="mt-2 mb-4 sm:mt-10 sm:mb-8">
                    <WeeklyAuctionSlider />
                </div>
            )}

            <PopularProductsGrid />

            {showMainTopBanners && <ExtraBanner />}

            {showMainTopBanners && (
                <div className="my-4 sm:my-8 md:my-12">
                    <FeaturedSellersSection />
                </div>
            )}

            {/* ✅ 상품 리스트만 표시 */}
            <section className="max-w-screen-xl mx-auto px-1 py-8 sm:py-16">
                <div className="flex justify-between items-center mb-2">
                    <h2 className="text-2xl font-bold text-gray-800">
                        {getTopCategoryName(categoryId)}
                    </h2>
                </div>
                <p className="text-sm text-gray-600 mb-4">필터 예정</p>

                <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-2 sm:gap-4 px-2 sm:px-0">
                    {products.map((p, index) => (
                        <ProductCard key={`product-${p.id}-${index}`} {...p} />
                    ))}
                </div>

                {showLoadMore && (
                    <div className="text-center mt-6">
                        <button
                            onClick={loadMore}
                            className="px-6 py-2 text-sm bg-white hover:bg-gray-100 text-gray-800 rounded-lg border border-gray-300"
                        >
                            더보기
                        </button>
                    </div>
                )}
            </section>

            {showMainTopBanners && (
                <div className="hidden md:block">
                    <BottomInspirationSlider />
                </div>
            )}

            {showMainTopBanners && (
                <>
                    {[10, 20, 30, 40, 50].map((id) => (
                        <div key={id} className="mb-6 sm:mb-10">
                            <SectionWithSubCategoryButtons
                                title={
                                    id === 10 ? '거실 가구' :
                                        id === 20 ? '침실 가구' :
                                            id === 30 ? '주방·다이닝 가구' :
                                                id === 40 ? '서재·오피스 가구' :
                                                    '기타 가구'
                                }
                                categoryId={id}
                                limit={5}
                            />
                        </div>
                    ))}
                </>
            )}

            {showMainTopBanners && (
                <div className="hidden md:block">
                    <MiddleBannerCarousel />
                </div>
            )}

            <div className="w-full bg-gray-100 py-8 mt-10 text-center text-sm text-gray-600">
                <p className="mb-1 font-semibold">© 2025 Realive</p>
                <p>중고 가구 거래 플랫폼 | 개인정보처리방침 | 이용약관</p>
            </div>

            <ChatbotFloatingButton />
        </div>
    );
}
