'use client';

import { useRouter } from 'next/navigation';
import { useState } from 'react';
import { Search } from 'lucide-react';

interface SearchBarProps {
    onSearch?: (keyword: string) => void;
}

export default function SearchBar({ onSearch }: SearchBarProps) {
    const [keyword, setKeyword] = useState('');
    const router = useRouter();

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        const trimmed = keyword.trim();
        if (!trimmed) return;

        if (onSearch) {
            onSearch(trimmed);
        } else {
            router.push(`/main?keyword=${encodeURIComponent(trimmed)}`);
        }
    };

    return (
        <form
            onSubmit={handleSearch}
            className="flex items-center w-full max-w-[900px] border border-gray-300 rounded-full px-4 py-1.5 bg-white shadow-sm"
        >
            {/* 🔍 검색 버튼 (왼쪽) */}
            <button
                type="submit"
                className="text-gray-400 hover:text-gray-600 transition p-1.5 mr-2"
                aria-label="검색"
            >
                <Search size={16} />
            </button>

            {/* 🔤 입력창 */}
            <input
                type="text"
                value={keyword}
                onChange={(e) => setKeyword(e.target.value)}
                placeholder="상품명을 검색하세요."
                className="flex-1 bg-transparent border-none text-xs text-gray-700 placeholder:text-gray-400 focus:outline-none"
            />
        </form>
    );
}
