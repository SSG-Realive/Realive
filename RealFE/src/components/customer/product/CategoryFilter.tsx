// src/components/CategoryFilter.tsx
'use client';

import { useState } from 'react';

const categories = [
    { id: null, name: 'ALL' },
    { id: 10, name: '거실 가구' },
    { id: 20, name: '침실 가구' },
    { id: 30, name: '주방·다이닝 가구' },
    { id: 40, name: '서재·오피스 가구' },
    { id: 50, name: '기타 가구' },
];

export default function CategoryFilter({
                                           onSelect,
                                       }: {
    onSelect: (categoryId: number | null) => void;
}) {
    const [selectedId, setSelectedId] = useState<number | null>(null);

    return (
        <div className="w-full bg-white sticky top-[64px] z-40 overflow-x-auto">
            <div className="flex gap-4 px-4 py-3 min-w-max">
                {categories.map(({ id, name }) => (
                    <button
                        key={id ?? 'all'}
                        onClick={() => {
                            setSelectedId(id);
                            onSelect(id);
                        }}
                        className={`text-sm font-light px-2 py-1 rounded-full whitespace-nowrap transition-all
                            ${
                            selectedId === id
                                ? 'bg-black text-white'
                                : 'text-gray-700 hover:bg-gray-100'
                        }`}
                    >
                        {name}
                    </button>
                ))}
            </div>
        </div>
    );
}
