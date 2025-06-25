'use client';

import React from 'react';
import Slider from 'react-slick';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

const bannerImages = [
  '/images/banner1.jpg',
  '/images/banner2.jpg',
  '/images/banner3.jpg',
];

const CustomArrow = ({
                         onClick,
                         direction,
                     }: {
    onClick?: () => void;
    direction: 'left' | 'right';
}) => {
    return (
        <div
            onClick={onClick}
            className={`absolute top-1/2 z-20 transform -translate-y-1/2 bg-black bg-opacity-50 text-white text-2xl w-10 h-10 flex items-center justify-center rounded-full cursor-pointer ${
                direction === 'left' ? 'left-3' : 'right-3'
            }`}
        >
            {direction === 'left' ? '<' : '>'}
        </div>
    );
};

export default function BannerCarousel() {
    const settings = {
        dots: true,
        dotsClass: 'slick-dots custom-dots',
        infinite: true,
        speed: 500,
        autoplay: true,
        autoplaySpeed: 3000,
        slidesToShow: 1,
        slidesToScroll: 1,
        prevArrow: <CustomArrow direction="left" />,
        nextArrow: <CustomArrow direction="right" />,
    };

    return (
        <>
            <style>{`
        .custom-dots {
          position: static !important;
          display: flex !important;
          justify-content: center;
          margin-top: 16px;
          gap: 8px;
        }
        .custom-dots li {
          width: 40px;
          height: 6px;
        }
        .custom-dots li button {
          width: 100%;
          height: 6px;
          background: #ccc;
          border-radius: 3px;
          padding: 0;
          text-indent: -9999px;
        }
        .custom-dots li.slick-active button {
          background: #333;
        }

        .slick-slide > div {
          height: 100%;
        }

        .slick-prev, .slick-next {
          display: none !important;
        }
        /* 모바일에서 dot 사이즈 축소 */
        @media (max-width: 767px) {
            .custom-dots {
            margin-top: 8px !important; /* 이미지와의 간격 좁힘 */
            gap: 1px !important;        /* dot 간 간격 좁힘 */
            }

            .custom-dots li {
            width: 20px;
            height: 4px;
            }

            .custom-dots li button {
            height: 4px;
            }
        }
      `}</style>

            <div className="w-full h-[200px] sm:h-[280px] md:h-[420px] lg:h-[560px] relative overflow-hidden">
                <Slider {...settings}>
                    {bannerImages.map((src, index) => (
                        <div key={index}>
                            <img
                                src={src}
                                alt={`배너 ${index + 1}`}
                                className="w-full h-full object-cover"
                            />
                        </div>
                    ))}
                </Slider>
            </div>
        </>
    );
}
