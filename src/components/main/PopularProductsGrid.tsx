'use client';

import { useEffect, useState } from 'react';
import { fetchPopularProducts } from '@/service/customer/productService';
import { ProductListDTO } from '@/types/seller/product/product';
import ProductCard from '@/components/customer/product/ProductCard';
import Slider from 'react-slick';

import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

function CustomArrow({
                         direction,
                         onClick,
                     }: {
    direction: 'left' | 'right';
    onClick?: () => void;
}) {
    return (
        <button
            type="button"
            onClick={onClick}
            className={`absolute top-1/2 -translate-y-1/2 z-10 hidden md:flex items-center justify-center
        w-10 h-10 text-2xl font-bold text-white
        ${direction === 'left' ? '-left-6' : '-right-6'}
        bg-black/40 hover:bg-black/60 rounded-full transition`}
        >
            {direction === 'left' ? '‹' : '›'}
        </button>
    );
}

export default function PopularProductsGrid() {
    const [products, setProducts] = useState<ProductListDTO[]>([]);

    useEffect(() => {
        fetchPopularProducts()
            .then((data) => setProducts(data.slice(0, 15))) // 최대 15개 제한
            .catch((err) => {
                console.error('인기 상품 불러오기 실패:', err);
            });
    }, []);

    const settings = {
        infinite: false,
        speed: 500,
        slidesToShow: 5,
        slidesToScroll: 5,
        arrows: true,
        prevArrow: <CustomArrow direction="left" />,
        nextArrow: <CustomArrow direction="right" />,
        responsive: [
            {
                breakpoint: 1280,
                settings: {
                    slidesToShow: 4,
                    slidesToScroll: 4,
                },
            },
            {
                breakpoint: 1024,
                settings: {
                    slidesToShow: 3,
                    slidesToScroll: 3,
                },
            },
            {
                breakpoint: 768,
                settings: {
                    slidesToShow: 2,
                    slidesToScroll: 2,
                },
            },
            {
                breakpoint: 480,
                settings: {
                    slidesToShow: 1.5,
                    slidesToScroll: 1,
                    arrows: false,
                },
            },
        ],
    };

    return (
        <section className="relative max-w-screen-2xl mx-auto bg-gray-50 rounded-2xl py-4 px-4 sm:py-6 sm:px-8 md:py-10 md:px-20 mt-6 sm:mt-10">
            <h2 className="text-2xl font-bold text-gray-800 mb-6">인기상품</h2>
            <Slider {...settings}>
                {products.map((product) => (
                    <div key={product.id} className="px-2">
                        <ProductCard {...product} />
                    </div>
                ))}
            </Slider>
        </section>
    );
}