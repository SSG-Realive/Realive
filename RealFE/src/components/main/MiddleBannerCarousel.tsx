'use client';

import React from 'react';
import Slider from 'react-slick';
import Link from 'next/link';
import Image from 'next/image';
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

            <div className="w-full mt-12 mb-20 overflow-visible">
                <div className="w-full rounded-none overflow-hidden">
                    <Slider {...settings}>
                        {images.map((item, index) => (
                            <div key={index}>
                                <Link href={item.link}>
                                    <div
                                        className="
                      relative w-full
                      h-[180px]
                      sm:h-[250px]
                      md:h-[320px]
                      lg:h-[420px]
                      xl:h-[480px]
                      2xl:h-[520px]
                    "
                                    >
                                        <Image
                                            src={item.src}
                                            alt={`하단 배너 ${index + 1}`}
                                            fill
                                            className="object-cover object-bottom rounded-none cursor-pointer"
                                            sizes="100vw"
                                            priority={index === 0}
                                        />
                                    </div>
                                </Link>
                            </div>
                        ))}
                    </Slider>
                </div>
            </div>
        </>
    );
}
