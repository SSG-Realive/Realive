'use client';

import Slider from 'react-slick';
import Link from 'next/link';
import { useAuctionStore } from '@/store/customer/auctionStore';
import { useEffect, useRef } from 'react';

export default function WeeklyAuctionSlider() {
  const { auctions, fetchAuctions } = useAuctionStore();
  const sliderRef = useRef<Slider>(null);

  useEffect(() => {
    if (auctions.length === 0) {
      fetchAuctions();
    }
  }, [fetchAuctions, auctions.length]);

  const sorted = [...auctions]
    .filter((a) => a.createdAt)
    .sort(
      (a, b) =>
        new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime()
    )
    .slice(0, 10);

  const settings = {
    infinite: true,
    speed: 700,
    cssEase: 'ease-in-out',
    centerMode: true,
    centerPadding: '0px',
    slidesToShow: 5,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3500,
    arrows: false, // 기본 화살표 제거
    dots: false,
    responsive: [
      {
        breakpoint: 1024,
        settings: { slidesToShow: 3 },
      },
      {
        breakpoint: 768,
        settings: { slidesToShow: 1 },
      },
    ],
  };

  return (
    <section className="relative px-4 mt-14">
      <h2 className="text-xl font-bold text-center mb-6">금주의 옥션상품</h2>

      {/* 슬라이더 + 화살표 wrapper */}
      <div className="relative">
        <Slider ref={sliderRef} {...settings}>
          {sorted.map((auction) => (
            <div key={auction.id} className="px-2 realive-auction-slide">
              <Link href={`/auctions/${auction.id}`}>
                <div className="w-full aspect-w-16 aspect-h-9 rounded-lg shadow overflow-hidden bg-gray-100">
                    <img
                        src={auction.adminProduct?.imageUrl || '/images/placeholder.png'}
                        alt={auction.adminProduct?.productName || '상품 이미지'}
                        className="object-contain w-full h-full"
                    />
                </div>
              </Link>
              <p className="text-sm text-center mt-1 truncate">
                {auction.adminProduct?.productName || '상품 없음'}
              </p>
            </div>
          ))}
        </Slider>

        {/* 왼쪽 화살표 - 1,2번 사이 위치 */}
        <button
          onClick={() => sliderRef.current?.slickPrev()}
          className="absolute z-10 top-[50%] left-[20%] -translate-y-1/2 text-3xl bg-white shadow rounded-full p-2 hover:scale-110 transition"
          type="button"
        >
          &#10094;
        </button>

        {/* 오른쪽 화살표 - 4,5번 사이 위치 */}
        <button
          onClick={() => sliderRef.current?.slickNext()}
          className="absolute z-10 top-[50%] right-[20%] -translate-y-1/2 text-3xl bg-white shadow rounded-full p-2 hover:scale-110 transition"
          type="button"
        >
          &#10095;
        </button>
      </div>

      <style jsx global>{`
        .realive-auction-slide {
          transition: transform 0.4s ease, filter 0.4s ease;
          transform: scale(0.9);
          filter: brightness(0.92);
          opacity: 0.7;
        }

        .slick-center .realive-auction-slide {
          transform: scale(1.05);
          filter: brightness(1);
          opacity: 1;
          z-index: 2;
        }
      `}</style>
    </section>
  );
}
