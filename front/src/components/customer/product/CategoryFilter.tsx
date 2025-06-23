// src/components/CategoryFilter.tsx
'use client';

import { useState } from 'react';

const categories = [
    { id: null, name: 'ALL' },
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
        <div className="w-full bg-white sticky top-[64px] z-40 overflow-x-auto">
            <div className="flex gap-4 px-4 py-3 min-w-max">
                {categories.map(({ id, name }) => (
                    <button
                        key={id ?? 'all'}
                        onClick={() => {
                            setSelectedId(id);
                            onSelect(id);
                        }}
                        className={`text-sm font-semibold px-2 py-1 rounded-full whitespace-nowrap transition-all
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
