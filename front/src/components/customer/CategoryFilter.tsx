// src/components/CategoryFilter.tsx
'use client';

import { useState } from 'react';

// ✅ 카테고리 이름 + ID 구조 (null = 전체)
const categories = [
    { id: null, name: '전체' },
    { id: 1, name: '가구' },
    { id: 2, name: '수납/정리' },
    { id: 3, name: '인테리어 소품' },
    { id: 4, name: '유아/아동' },
];

export default function CategoryFilter({
                                           onSelect,
                                       }: {
    onSelect: (categoryId: number | null) => void;
}) {
    const [selectedId, setSelectedId] = useState<number | null>(null);

    return (
        <div className="flex gap-3 overflow-x-auto px-4 py-2">
            {categories.map(({ id, name }) => (
                <button
                    key={id ?? 'all'}
                    onClick={() => {
                        setSelectedId(id);
                        onSelect(id); // ✅ 숫자 ID 전달 (null 포함)
                    }}
                    className={`px-4 py-1 rounded-full border text-sm whitespace-nowrap ${
                        selectedId === id
                            ? 'bg-green-600 text-white border-green-600'
                            : 'bg-white text-gray-700 border-gray-300'
                    }`}
                >
                    {name}
                </button>
            ))}
        </div>
    );
}
