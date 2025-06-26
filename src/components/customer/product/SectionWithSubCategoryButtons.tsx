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

    useEffect(() => {
        fetchAllCategories().then((all) => {
            const filtered = all.filter((c) => c.parentId === categoryId);
            setSubCategories(filtered);
            setSelectedSubId(null); // 전체 보기
        });
    }, [categoryId]);

    useEffect(() => {
        const targetId = selectedSubId ?? categoryId;
        fetchPublicProducts(targetId, 1, limit).then(setProducts);
    }, [selectedSubId, categoryId, limit]);

    return (
        <div className="w-full max-w-screen-xl mx-auto px-4 mb-12">
            {/* 🔹 타이틀 + 서브 카테고리 버튼들 */}
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

            {/* 🔹 상품 목록 */}
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 sm:gap-6 w-full">
                {products.map((p) => (
                    <ProductCard key={p.id} {...p} />
                ))}
            </div>
        </div>
    );
}
