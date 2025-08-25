'use client';

import { useEffect, useState } from 'react';
import { Category } from '@/types/common/category';
import { fetchAllCategories } from '@/service/categoryService';

interface Props {
    selectedCategoryId: number | null;
    onSelect: (id: number | null) => void;
}

export default function CategoryFilterInline({ selectedCategoryId, onSelect }: Props) {
    const [categories, setCategories] = useState<Category[]>([]);

    useEffect(() => {
        fetchAllCategories().then(all => {
            const firstLevel = all.filter(cat => cat.parentId === null);
            setCategories(firstLevel);
        });
    }, []);

    return (
        <div className="flex flex-wrap items-center justify-between mb-6">
            {/* 왼쪽: 전체상품 텍스트 */}
            <h2 className="text-lg font-bold whitespace-nowrap mr-4">전체 상품</h2>

            {/* 오른쪽: 카테고리 버튼들 */}
            <div className="flex flex-wrap gap-2">
                <button
                    onClick={() => onSelect(null)}
                    className={`px-3 py-1 text-sm rounded-full transition ${
                        selectedCategoryId === null
                            ? 'bg-black text-white'
                            : 'bg-white text-gray-600 hover:bg-gray-100'
                    }`}
                >
                    전체
                </button>
                {categories.map(cat => (
                    <button
                        key={cat.id}
                        onClick={() => onSelect(cat.id)}
                        className={`px-3 py-1 text-sm rounded-full transition ${
                            selectedCategoryId === cat.id
                                ? 'bg-black text-white'
                                : 'bg-white text-gray-600 hover:bg-gray-100'
                        }`}
                    >
                        {cat.name}
                    </button>
                ))}
            </div>
        </div>
    );
}
