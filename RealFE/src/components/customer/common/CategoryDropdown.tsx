'use client';

import { useEffect, useState, useRef } from 'react';
import axios from 'axios';

interface Category {
    id: number;
    name: string;
    parentId: number | null;
}

interface CategoryGroup {
    id: number;
    name: string;
    subCategories: Category[];
}

interface Props {
    onCategorySelect?: (id: number) => void;
    isCompact?: boolean;
}

export default function CategoryDropdown({ onCategorySelect, isCompact }: Props) {
    const [categories, setCategories] = useState<CategoryGroup[]>([]);
    const [hoveredId, setHoveredId] = useState<number | null>(null);
    const [hoveredFirstId, setHoveredFirstId] = useState<number | null>(null);
    const [allOpen, setAllOpen] = useState(false);
    const timeoutRef = useRef<NodeJS.Timeout | null>(null);

    useEffect(() => {
        axios
            .get<Category[]>('https://www.realive-ssg.click/api/seller/categories')
            .then((res) => {
                const all = res.data;
                const parents = all.filter((c) => c.parentId === null);
                const children = all.filter((c) => c.parentId !== null);

                const grouped: CategoryGroup[] = parents.map((p) => ({
                    id: p.id,
                    name: p.name,
                    subCategories: children.filter((c) => c.parentId === p.id),
                }));

                setCategories(grouped);
            })
            .catch((err) => {
                console.error('카테고리 불러오기 실패:', err);
            });
    }, []);

    const goToCategory = (id: number | null) => {
        if (onCategorySelect) onCategorySelect(id ?? -1);
        if (id === null) {
            location.href = '/main';
        } else {
            location.href = `/main?category=${id}`;
        }
    };

    const isMobile = () => typeof window !== 'undefined' && window.innerWidth < 768;

    return (
        <div
            style={{ border: 'none', boxShadow: 'none' }}
            className={`overflow-x-auto md:overflow-visible whitespace-nowrap no-scrollbar px-4 py-0 bg-transparent border-none shadow-none rounded-none text-sm relative mb-0 ${
                isCompact ? 'z-10' : 'z-50'
            }`}
        >
            <div className="inline-flex items-center gap-4 w-full">
                {/* ✅ 전체 버튼 */}
                <div
                    className="relative"
                    onMouseEnter={() => {
                        if (!isMobile()) {
                            if (timeoutRef.current) clearTimeout(timeoutRef.current);
                            setAllOpen(true);
                            setHoveredId(null);
                        }
                    }}
                    onMouseLeave={() => {
                        if (!isMobile()) {
                            timeoutRef.current = setTimeout(() => setAllOpen(false), 150);
                        }
                    }}
                >
                    <span
                        className="inline-block relative px-2 pt-1 pb-1 cursor-pointer text-base font-light tracking-tight text-gray-800 z-50"
                        onClick={() => goToCategory(null)}
                    >
                        전체
                    </span>

                    {/* ✅ 모바일 전용 경매 버튼 (전체 옆) */}
                    <span
                        className="inline-block md:hidden ml-2 px-2 pt-1 pb-1 cursor-pointer text-base font-light tracking-tight text-red-600 hover:text-red-700 z-50"
                        onClick={() => (location.href = '/auctions')}
                    >
                        경매
                    </span>

                    {/* 전체 드롭다운 */}
                    {!isMobile() && allOpen && (
                        <div className="absolute top-full left-0 mt-1 bg-[rgba(255,255,255,0.85)] backdrop-blur-sm shadow-md rounded z-50 min-w-[180px] py-2">
                            <ul>
                                {categories.map((group) => {
                                    const isGroupOpen = hoveredFirstId === group.id;

                                    return (
                                        <li
                                            key={group.id}
                                            className="px-4 py-2 hover:bg-gray-100 cursor-pointer relative font-light"
                                            onMouseEnter={() => setHoveredFirstId(group.id)}
                                        >
                                            {group.name}
                                            {isGroupOpen && group.subCategories.length > 0 && (
                                                <ul className="absolute top-0 left-full ml-2 bg-[rgba(255,255,255,0.85)] backdrop-blur-sm shadow-md rounded z-50 min-w-[160px] py-2">
                                                    {group.subCategories.map((sub) => (
                                                        <li
                                                            key={sub.id}
                                                            onClick={() => goToCategory(sub.id)}
                                                            className="px-4 py-2 hover:bg-gray-100 cursor-pointer whitespace-nowrap font-light"
                                                        >
                                                            {sub.name}
                                                        </li>
                                                    ))}
                                                </ul>
                                            )}
                                        </li>
                                    );
                                })}
                            </ul>
                        </div>
                    )}
                </div>

                {!isCompact && (
                    <div className="hidden md:block w-px h-6 bg-gray-300 self-center" />
                )}

                {/* ✅ 카테고리 리스트 */}
                {categories.map((cat) => {
                    const isOpen = hoveredId === cat.id;

                    return (
                        <div
                            key={cat.id}
                            className="relative"
                            onMouseEnter={() => {
                                if (!isMobile()) {
                                    if (timeoutRef.current) clearTimeout(timeoutRef.current);
                                    setHoveredId(cat.id);
                                    setAllOpen(false);
                                }
                            }}
                            onMouseLeave={() => {
                                if (!isMobile()) {
                                    timeoutRef.current = setTimeout(() => setHoveredId(null), 150);
                                }
                            }}
                        >
                            <span
                                className={
                                    isCompact
                                        ? 'inline-block relative px-2 sm:px-3 py-0.5 sm:py-1 cursor-pointer text-[11px] sm:text-sm font-light bg-black text-white rounded-full hover:bg-gray-800 transition tracking-tight z-10'
                                        : 'inline-block relative px-2 pt-1 pb-1 cursor-pointer text-base font-light tracking-tight text-gray-800 z-50'
                                }
                                onClick={() => goToCategory(cat.id)}
                            >
                                {cat.name}
                            </span>

                            {/* 서브 카테고리 드롭다운 */}
                            {!isMobile() && isOpen && cat.subCategories.length > 0 && (
                                <div className="absolute top-full left-0 mt-1 bg-[rgba(255,255,255,0.85)] backdrop-blur-sm shadow-lg rounded z-50 min-w-[160px] py-2">
                                    <ul>
                                        {cat.subCategories.map((sub) => (
                                            <li
                                                key={sub.id}
                                                onClick={() => goToCategory(sub.id)}
                                                className="px-4 py-2 hover:bg-gray-100 whitespace-nowrap cursor-pointer font-light"
                                            >
                                                {sub.name}
                                            </li>
                                        ))}
                                    </ul>
                                </div>
                            )}
                        </div>
                    );
                })}

                {/* ✅ 데스크탑 전용 경매 버튼 (맨 오른쪽) */}
                <span
                    className="hidden md:inline-block px-2 pt-1 pb-1 cursor-pointer text-base font-light tracking-tight text-red-600 hover:text-red-700 z-50"
                    onClick={() => (location.href = '/auctions')}
                >
                    경매
                </span>
            </div>
        </div>
    );
}
