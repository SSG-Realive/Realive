'use client';

import React from 'react';
import Slider from 'react-slick';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

const bannerImages = [
  '/images/배너1.jpg',
  '/images/배너2.jpg',
  '/images/배너3.jpg',
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
          position: static !important; /* 이미지 내부 말고 아래로 */
          display: flex !important;
          justify-content: center;
          margin-top: 16px;
          padding: 0;
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

        /* 기본 slick 화살표 숨기기 (우리가 직접 만들었으므로) */
        .slick-prev, .slick-next {
          display: none !important;
        }
      `}</style>

      <div className="w-[75vw] max-w-5xl mx-auto relative">
        <div className="relative">
          <Slider {...settings}>
            {bannerImages.map((src, index) => (
              <div key={index}>
                <img
                  src={src}
                  alt={`배너 ${index + 1}`}
                  className="w-full h-[300px] object-cover rounded-xl"
                />
              </div>
            ))}
          </Slider>
        </div>
      </div>
    </>
  );
}
