'use client';

import { useEffect, useState } from 'react';
import Slider from 'react-slick';
import Link from 'next/link';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

export default function ExtraBannerCarousel() {
    const bannerImages = [
        { src: '/images/banner6.jpg', link: '/main?category=11' },
        { src: '/images/banner7.jpg', link: '/main?category=21' },
        { src: '/images/banner8.jpg', link: '/main?category=11' },
    ];

    const [isMobile, setIsMobile] = useState(false);

    useEffect(() => {
        const handleResize = () => {
            setIsMobile(window.innerWidth < 640); // Tailwind 기준 sm: 640px
        };

        handleResize(); // 초기 실행
        window.addEventListener('resize', handleResize);
        return () => window.removeEventListener('resize', handleResize);
    }, []);

    const sliderSettings = {
        dots: true,
        infinite: true,
        autoplay: true,
        autoplaySpeed: 4000,
        speed: 500,
        slidesToShow: 1,
        slidesToScroll: 1,
        arrows: false,
    };

    return (
        <div className="w-full my-0 pt-0">
            {/* ✅ max-w 제거하고 padding도 없앰 */}
            {isMobile ? (
                <Slider {...sliderSettings}>
                    {bannerImages.map((item, idx) => (
                        <div key={idx}>
                            <Link href={item.link}>
                                <img
                                    src={item.src}
                                    alt={`배너 ${idx + 1}`}
                                    className="w-full h-auto object-cover cursor-pointer"
                                />
                            </Link>
                        </div>
                    ))}
                </Slider>
            ) : (
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-0">
                    {bannerImages.map((item, idx) => (
                        <Link
                            key={idx}
                            href={item.link}
                            className="w-full block overflow-hidden"
                        >
                            <img
                                src={item.src}
                                alt={`배너 ${idx + 1}`}
                                className="w-full h-full object-cover"
                            />
                        </Link>
                    ))}
                </div>
            )}
        </div>
    );
}
