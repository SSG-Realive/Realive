'use client'

import { useEffect, useRef, useState } from 'react'
import { ProductListDTO } from '@/types/seller/product/product'
import ProductCard from '@/components/customer/product/ProductCard'
import Slider from 'react-slick'
import { fetchPopularProducts, fetchPopularProductsByCategory } from '@/service/customer/productService';


import 'slick-carousel/slick/slick.css'
import 'slick-carousel/slick/slick-theme.css'

interface PopularProductsGridProps {
    categoryId: number | null;
}

export default function PopularProductsGrid({ categoryId }: PopularProductsGridProps) {
    const [products, setProducts] = useState<ProductListDTO[] | null>(null)
    const [currentDotIndex, setCurrentDotIndex] = useState(0)
    const sliderRef = useRef<Slider | null>(null)

    const slidesToShow = 3

    const settings = {
        dots: false,
        infinite: true,
        autoplay: true,
        autoplaySpeed: 3000,
        speed: 500,
        slidesToShow,
        slidesToScroll: 1,
        arrows: false,
        swipeToSlide: true,
        draggable: true,
        pauseOnHover: false,
        pauseOnFocus: false,
        afterChange: (index: number) => {
            setCurrentDotIndex(Math.floor(index / slidesToShow))
        },
        responsive: [
            {
                breakpoint: 1280,
                settings: { slidesToShow: 3, slidesToScroll: 1 },
            },
            {
                breakpoint: 1024,
                settings: { slidesToShow: 2, slidesToScroll: 1 },
            },
            {
                breakpoint: 768,
                settings: { slidesToShow: 2, slidesToScroll: 1 },
            },
            {
                breakpoint: 640,
                settings: {
                    slidesToShow: 1,
                    slidesToScroll: 1,
                    swipeToSlide: true,
                    draggable: true,
                },
            },
        ],
    }

    useEffect(() => {
        console.log('넘어온 categoryId:', categoryId);
        if (categoryId === null) {
            fetchPopularProducts() // 전체 인기 상품 API 호출
                .then((data) => {
                    setProducts(data.slice(0, 15));
                })
                .catch((err) => console.error('전체 인기 상품 로딩 실패', err));
        } else {
            fetchPopularProductsByCategory(categoryId)
                .then((data) => {
                    setProducts(data.slice(0, 15));
                })
                .catch((err) => console.error('카테고리 인기 상품 로딩 실패', err));
        }
    }, [categoryId]);

    const totalDots = products ? Math.ceil(products.length / slidesToShow) : 0

    return (
        <section className="max-w-screen-xl mx-auto mt-6 sm:mt-10 px-4">
            <h2 className="text-xl font-light text-gray-800 mb-1">인기 상품</h2>
            <p className="text-sm text-gray-500 mb-6">
                지금 많은 분들이 관심을 가지고 있는 상품들을 만나보세요.
            </p>

            {products && (
                <>
                    <Slider {...settings} ref={sliderRef}>
                        {products.map((product) => (
                            <div key={product.id} className="px-2 h-full flex">
                                <ProductCard {...product} />
                            </div>
                        ))}
                    </Slider>

                    {/* ✅ Dot 네비게이션 */}
                    <div className="flex justify-center gap-2 mt-4">
                        {Array.from({ length: totalDots }).map((_, index) => (
                            <button
                                key={index}
                                className={`w-2.5 h-2.5 rounded-full transition-colors duration-200 ${
                                    index === currentDotIndex
                                        ? 'bg-gray-500'
                                        : 'bg-gray-300 hover:bg-gray-400'
                                }`}
                                onClick={() =>
                                    sliderRef.current?.slickGoTo(index * slidesToShow)
                                }
                                aria-label={`슬라이드 ${index + 1}`}
                            />
                        ))}
                    </div>
                </>
            )}
        </section>
    )
}
