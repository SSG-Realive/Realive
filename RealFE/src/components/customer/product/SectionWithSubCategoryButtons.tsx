'use client';

import { useEffect, useState } from 'react';
import Slider from 'react-slick';

import { Category } from '@/types/common/category';
import { ProductListDTO } from '@/types/seller/product/product';
import { fetchPublicProducts } from '@/service/customer/productService';
import { fetchAllCategories } from '@/service/categoryService';
import ProductCard from './ProductCard';

import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

interface Props {
    title: string;
    categoryId: number;
    limit: number;
}

// 🔽 커스텀 화살표
const Arrow = ({
                   className,
                   onClick,
                   direction,
               }: {
    className?: string;
    onClick?: () => void;
    direction: 'left' | 'right';
}) => (
    <div
        className={`${className} z-10 bg-black bg-opacity-40 text-white rounded-full w-8 h-8 flex items-center justify-center cursor-pointer hover:bg-opacity-70 ${
            direction === 'left' ? 'left-1' : 'right-1'
        }`}
        onClick={onClick}
    >
        {direction === 'left' ? '<' : '>'}
    </div>
);

export default function SectionWithSubCategoryButtons({ title, categoryId, limit }: Props) {
    const [subCategories, setSubCategories] = useState<Category[]>([]);
    const [selectedSubId, setSelectedSubId] = useState<number | null>(null);
    const [products, setProducts] = useState<ProductListDTO[]>([]);

    useEffect(() => {
        fetchAllCategories().then((all) => {
            const filtered = all.filter((c) => c.parentId === categoryId);
            setSubCategories(filtered);
            setSelectedSubId(null);
        });
    }, [categoryId]);

    useEffect(() => {
        const targetId = selectedSubId ?? categoryId;
        fetchPublicProducts(targetId, 1, limit).then((data) => {
            setProducts(data);
        });
    }, [selectedSubId, categoryId, limit]);

    // ✅ 슬라이더 설정: 2개씩 보여주기
    const sliderSettings = {
        slidesToShow: 2,
        slidesToScroll: 1,
        infinite: true,
        autoplay: true,
        autoplaySpeed: 4000,
        arrows: true,
        nextArrow: <Arrow direction="right" />,
        prevArrow: <Arrow direction="left" />,
        responsive: [
            { breakpoint: 640, settings: { slidesToShow: 1 } },  // 모바일: 1개
            { breakpoint: 768, settings: { slidesToShow: 2 } },  // 태블릿~PC: 2개
            { breakpoint: 1024, settings: { slidesToShow: 2 } },
            { breakpoint: 1280, settings: { slidesToShow: 2 } },
        ],
    };

    return (
        <div className="w-full max-w-screen-xl mx-auto px-4 mb-12">
            {/* 🔹 타이틀 + 서브 카테고리 버튼 줄 */}
            <div className="w-full overflow-x-auto no-scrollbar">
                <div className="inline-flex items-center gap-2 px-2 py-1">
                    <h2 className="text-xl font-light text-gray-800 mr-2 shrink-0">{title}</h2>
                    <button
                        onClick={() => setSelectedSubId(null)}
                        className={`text-sm transition whitespace-nowrap shrink-0 ${
                            selectedSubId === null
                                ? 'text-black font-light underline'
                                : 'text-gray-500 hover:text-black'
                        }`}
                    >
                        전체
                    </button>
                    {subCategories.map((cat) => (
                        <button
                            key={cat.id}
                            onClick={() => setSelectedSubId(cat.id)}
                            className={`text-sm transition whitespace-nowrap shrink-0 ${
                                selectedSubId === cat.id
                                    ? 'text-black font-light underline'
                                    : 'text-gray-500 hover:text-black'
                            }`}
                        >
                            {cat.name}
                        </button>
                    ))}
                </div>
            </div>

            {/* 🔹 상품 슬라이더 */}
            <Slider {...sliderSettings}>
                {products.map((p) => (
                    <div key={p.id} className="px-2 sm:px-3">
                        <ProductCard {...p} />
                    </div>
                ))}
            </Slider>
        </div>
    );
}
