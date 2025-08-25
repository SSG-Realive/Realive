'use client';

import { useEffect, useState } from 'react';
import { usePathname, useSearchParams } from 'next/navigation';

import { fetchPublicProducts } from '@/service/customer/productService';
import { fetchAllCategories } from '@/service/categoryService';

import { ProductListDTO } from '@/types/seller/product/product';
import { Category } from '@/types/common/category';

import ProductCard from '@/components/customer/product/ProductCard';
import BannerCarousel from '@/components/main/BannerCarousel';
import PopularProductsGrid from '@/components/main/PopularProductsGrid';
import MiddleBannerCarousel from '@/components/main/MiddleBannerCarousel';
import ExtraBanner from '@/components/main/ExtraBanner';
import SectionWithSubCategoryButtons from '@/components/customer/product/SectionWithSubCategoryButtons';
import FeaturedSellersSection from '@/components/main/FeaturedSellersSection';
import BottomInspirationSlider from '@/components/main/BottomInspirationSlider';
import ScrollToTopButton from '@/components/customer/common/ScrollToTopButton';
import AuctionCard from '@/components/customer/auctions/AuctionCard';
import { Auction } from '@/types/customer/auctions';
import { publicAuctionService } from '@/service/customer/publicAuctionService';

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
    const [categories, setCategories] = useState<Category[]>([]);
    const [categoryMap, setCategoryMap] = useState<Record<number, Category>>({});
    const [auctions, setAuctions] = useState<Auction[]>([]);
    const [auctionLoading, setAuctionLoading] = useState(true);
    const [auctionError, setAuctionError] = useState<string | null>(null);
    const isMainDefaultView = pathname === '/main' && !categoryFromUrl && !keywordFromUrl;

    useEffect(() => {
        fetchAllCategories().then((data) => {
            setCategories(data);
            setCategoryMap(Object.fromEntries(data.map((c) => [c.id, c])));
        });
    }, []);

    useEffect(() => {
        setCategoryId(categoryFromUrl ? Number(categoryFromUrl) : null);
        setKeyword(keywordFromUrl);
        setPage(1);
    }, [categoryFromUrl, keywordFromUrl]);

    useEffect(() => {
        fetchPublicProducts(categoryId, 1, ITEMS_PER_PAGE, keyword).then((initial) => {
            setProducts(initial);
            setShowLoadMore(initial.length === ITEMS_PER_PAGE);
        });
    }, [categoryId, keyword]);

    useEffect(() => {
        if (isMainDefaultView) {
            const loadAuctions = async () => {
                setAuctionLoading(true);
                try {
                    const paginatedData = await publicAuctionService.fetchPublicActiveAuctions();
                    setAuctions(paginatedData.content);
                } catch (error: any) {
                    console.error("경매 데이터 로딩 실패:", error);
                    setAuctionError("경매 상품을 불러올 수 없습니다.");
                } finally {
                    setAuctionLoading(false);
                }
            };
            loadAuctions();
        }
    }, [isMainDefaultView]);

    const loadMore = async () => {
        const nextPage = page + 1;
        const newProducts = await fetchPublicProducts(categoryId, nextPage, ITEMS_PER_PAGE, keyword);
        setProducts((prev) => [...prev, ...newProducts]);
        setPage(nextPage);
        setShowLoadMore(newProducts.length === ITEMS_PER_PAGE);
    };

    const getCategoryTitle = () => {
        if (!categoryId || !categoryMap[categoryId]) return '전체 상품';
        let current = categoryMap[categoryId];
        while (current.parentId && categoryMap[current.parentId]) {
            current = categoryMap[current.parentId];
        }
        return current.name;
    };

    const getRootCategoryId = (id: number | null): number | null => {
        if (id === null || !categoryMap[id]) return null;
        let current = categoryMap[id];
        while (current.parentId && categoryMap[current.parentId]) {
            current = categoryMap[current.parentId];
        }
        return current.id;
    };

    const getSiblingCategories = (): Category[] => {
        if (!categoryId || !categoryMap[categoryId]) return categories.filter((c) => c.parentId === null);
        const current = categoryMap[categoryId];
        const parentId = current.parentId ?? current.id;
        return categories.filter((c) => c.parentId === parentId);
    };

    return (
        <div className="min-h-screen overflow-x-auto">
            {isMainDefaultView && <div className="mb-0 sm:mb-2"><ExtraBanner /></div>}

            {/* ✅ 주간 경매 */}
            {isMainDefaultView && (
                <section className="w-full mt-12 mb-4 sm:mt-6 sm:mb-8">
                    <div className="max-w-screen-xl mx-auto px-4">
                        <h2 className="text-lg sm:text-xl font-light mb-2 text-gray-900">주간 경매</h2>
                        <p className="text-sm text-gray-500 mb-6">
                            이번 주에만 만나볼 수 있는 한정 경매 상품을 확인해보세요.
                        </p>
                    </div>


                    {auctionLoading ? (
                        <p className="px-4">로딩 중...</p>
                    ) : auctionError ? (
                        <p className="px-4">{auctionError}</p>
                    ) : auctions.length > 0 ? (
                        <AuctionCard auctions={auctions} />
                    ) : (
                        <p className="px-4">진행중인 경매 없음</p>
                    )}
                </section>
            )}

            {categoryMap && Object.keys(categoryMap).length > 0 && (
                <PopularProductsGrid categoryId={getRootCategoryId(categoryId)} />
            )}

            {isMainDefaultView && <BannerCarousel />}
            {isMainDefaultView && <div className="my-4 sm:my-8 md:my-12"><FeaturedSellersSection /></div>}
            {isMainDefaultView && <MiddleBannerCarousel />}

            {/* 🔹 상품 목록 */}
            <section className="max-w-screen-xl mx-auto px-4 mt-6 mb-8">
                <div className="w-full overflow-x-auto no-scrollbar">
                    <div className="inline-flex items-center gap-2 px-2 py-1">
                        <h2 className="text-xl font-light text-gray-800 mr-2 shrink-0">{getCategoryTitle()}</h2>

                        <button
                            onClick={() => {
                                setCategoryId(null);
                                setKeyword('');
                                setPage(1);
                            }}
                            className={`text-sm transition whitespace-nowrap shrink-0 ${
                                categoryId === null
                                    ? 'text-black font-light underline'
                                    : 'text-gray-500 hover:text-black'
                            }`}
                        >
                            전체
                        </button>

                        {getSiblingCategories().map((cat) => (
                            <button
                                key={cat.id}
                                onClick={() => {
                                    setCategoryId(cat.id);
                                    setKeyword('');
                                    setPage(1);
                                }}
                                className={`text-sm transition whitespace-nowrap shrink-0 ${
                                    categoryId === cat.id
                                        ? 'text-black font-light underline'
                                        : 'text-gray-500 hover:text-black'
                                }`}
                            >
                                {cat.name}
                            </button>
                        ))}
                    </div>
                </div>

                <div className="grid grid-cols-2 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-3 gap-4 mt-4 px-4">
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

            {isMainDefaultView && <BottomInspirationSlider />}

            {/* 카테고리별 섹션 */}
            {isMainDefaultView && (
                <>
                    {[10, 20, 30, 40, 50].map((id) => {
                        const titleMap: Record<number, string> = {
                            10: '거실 가구',
                            20: '침실 가구',
                            30: '주방·다이닝 가구',
                            40: '서재·오피스 가구',
                            50: '기타 가구',
                        };
                        return (
                            <div key={id} className="mb-6 sm:mb-10">
                                <SectionWithSubCategoryButtons title={titleMap[id]} categoryId={id} limit={5} />
                            </div>
                        );
                    })}
                </>
            )}
            <ScrollToTopButton />
        </div>
    );

}
