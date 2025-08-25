'use client';

import { useEffect, useState } from 'react';
import { Category } from '@/types/common/category';
import { ProductListDTO } from '@/types/seller/product/product';
import { fetchPublicProducts } from '@/service/customer/productService';
import { fetchAllCategories } from '@/service/categoryService';
import ProductCard from './ProductCard';

interface Props {
    title: string;
    categoryId: number; // 1차 카테고리 ID
    limit: number;
}

export default function SectionWithSubCategoryButtons({ title, categoryId, limit }: Props) {
    const [subCategories, setSubCategories] = useState<Category[]>([]);
    const [selectedSubId, setSelectedSubId] = useState<number | null>(null);
    const [products, setProducts] = useState<ProductListDTO[]>([]);
    const [page, setPage] = useState(1);
    const [hasMore, setHasMore] = useState(true);

    // 서브 카테고리 로딩
    useEffect(() => {
        fetchAllCategories().then((all) => {
            const filtered = all.filter((c) => c.parentId === categoryId);
            setSubCategories(filtered);
            setSelectedSubId(null); // 전체 보기
        });
    }, [categoryId]);

    // 카테고리 or 필터 바뀌면 초기 상품 로딩
    useEffect(() => {
        const targetId = selectedSubId ?? categoryId;
        fetchPublicProducts(targetId, 1, limit).then((data) => {
            setProducts(data);
            setPage(1);
            setHasMore(data.length === limit);
        });
    }, [selectedSubId, categoryId, limit]);

    // 더 보기
    const loadMore = async () => {
        const targetId = selectedSubId ?? categoryId;
        const nextPage = page + 1;
        const newProducts = await fetchPublicProducts(targetId, nextPage, limit);
        setProducts((prev) => [...prev, ...newProducts]);
        setPage(nextPage);
        setHasMore(newProducts.length === limit);
    };

    return (
        <div className="w-full max-w-screen-xl mx-auto px-4 mb-12">
            {/* 🔹 제목 + 카테고리 버튼 */}
            <div className="mb-4">
                <div className="flex flex-wrap items-center gap-2">
                    <h2 className="text-xl font-bold text-gray-800 mr-2 whitespace-nowrap">{title}</h2>
                    <button
                        onClick={() => setSelectedSubId(null)}
                        className={`text-sm transition whitespace-nowrap ${
                            selectedSubId === null
                                ? 'text-black font-semibold underline'
                                : 'text-gray-500 hover:text-black'
                        }`}
                    >
                        전체
                    </button>
                    {subCategories.map((cat) => (
                        <button
                            key={cat.id}
                            onClick={() => setSelectedSubId(cat.id)}
                            className={`text-sm transition whitespace-nowrap ${
                                selectedSubId === cat.id
                                    ? 'text-black font-semibold underline'
                                    : 'text-gray-500 hover:text-black'
                            }`}
                        >
                            {cat.name}
                        </button>
                    ))}
                </div>
            </div>

            {/* 🔹 상품 리스트 */}
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 sm:gap-6">
                {products.map((p) => (
                    <ProductCard key={p.id} {...p} />
                ))}
            </div>

            {/* 🔹 더 보기 버튼 */}
            {hasMore && (
                <div className="mt-6 text-center">
                    <button
                        onClick={loadMore}
                        className="px-6 py-2 text-sm bg-white text-gray-800 border border-gray-300 rounded-lg hover:bg-gray-100"
                    >
                        더 보기
                    </button>
                </div>
            )}
        </div>
    );
}
