'use client';

import React from 'react';
import Slider from 'react-slick';
import Link from 'next/link';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

const defaultImages = [
    { src: '/images/banner4.png', link: '/main?category=25' },
    { src: '/images/banner5.png', link: '/main?category=54' },
];

export default function MiddleBannerCarousel({
                                                 images = defaultImages,
                                             }: {
    images?: { src: string; link: string }[];
}) {
    const settings = {
        dots: true,
        dotsClass: 'slick-dots custom-dots',
        infinite: true,
        speed: 500,
        autoplay: true,
        autoplaySpeed: 4000,
        slidesToShow: 1,
        slidesToScroll: 1,
        arrows: false,
    };

    return (
        <>
            <style>{`
        .custom-dots {
          position: static !important;
          display: flex !important;
          justify-content: center;
          margin-top: 8px;
          gap: 8px;
        }

        .custom-dots li {
          width: 24px;
          height: 4px;
        }

        .custom-dots li button {
          width: 100%;
          height: 100%;
          background: #ccc;
          border-radius: 2px;
          padding: 0;
          text-indent: -9999px;
        }

        .custom-dots li.slick-active button {
          background: #333;
        }
      `}</style>

            <div className="w-full flex justify-center mt-12 mb-20 px-2 sm:px-4 overflow-visible">
                <div className="w-full max-w-7xl rounded-lg overflow-hidden">
                    <Slider {...settings}>
                        {images.map((item, index) => (
                            <div key={index}>
                                <Link href={item.link}>
                                    <img
                                        src={item.src}
                                        alt={`하단 배너 ${index + 1}`}
                                        className="w-full h-auto aspect-[4/1] object-cover rounded-lg cursor-pointer"
                                    />
                                </Link>
                            </div>
                        ))}
                    </Slider>
                </div>
            </div>
        </>
    );
}
