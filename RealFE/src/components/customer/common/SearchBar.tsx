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
            className="flex items-center w-full max-w-[900px] mx-auto
             rounded-full px-4 py-1.5
             bg-gray-100 border-none shadow-none"
        >
            {/* 🔍 검색 버튼 */}
            <button
                type="submit"
                className="text-black hover:text-black transition p-1.5 mr-2"
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
                className="flex-1 bg-transparent border-none text-sm text-black placeholder:text-black focus:outline-none"
            />
        </form>
    );
}
