// SearchBar.tsx
'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';

interface SearchBarProps {
    onSearch?: (keyword: string) => void; // ✅ optional
}

export default function SearchBar({ onSearch }: SearchBarProps) {
    const [keyword, setKeyword] = useState('');
    const router = useRouter();

    const handleSearch = () => {
        if (onSearch) {
            onSearch(keyword);
        } else {
            router.push(`/main?keyword=${encodeURIComponent(keyword)}`);
        }
    };

    return (
        <div className="flex gap-2 items-center w-full max-w-2xl">
            <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="상품명을 검색하세요"
                className="border px-4 py-1.5 rounded w-full text-sm"
            />
            <button
                onClick={handleSearch}
                className="px-4 py-1.5 bg-green-600 text-white rounded hover:bg-green-700 text-sm"
            >
                검색
            </button>
        </div>
    );
}
