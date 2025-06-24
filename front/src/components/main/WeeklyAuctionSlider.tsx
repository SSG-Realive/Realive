'use client';

import { useAuctionStore } from '@/store/customer/auctionStore';
import Link from 'next/link';
import { useState, useRef, useEffect, useMemo } from 'react';
import Slider from 'react-slick';

export default function WeeklyAuctionSlider() {
  const { auctions, fetchAuctions } = useAuctionStore();
  const sliderRef = useRef<Slider>(null);
  const [currentSlide, setCurrentSlide] = useState(0);
  const [slidesToShow, setSlidesToShow] = useState(5);

  useEffect(() => {
    if (auctions.length === 0) fetchAuctions();

    const handleResize = () => {
      const width = window.innerWidth;

      if (width < 768) setSlidesToShow(1);       // 모바일
      else if (width < 1200) setSlidesToShow(3); // 태블릿 작은 화면
      else if (width < 1600) setSlidesToShow(5); // 15인치 모니터 정도
      else if (width < 1920) setSlidesToShow(7); // 큰 데스크탑
      else setSlidesToShow(9);                    // 와이드 모니터 등
    };

    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, [fetchAuctions, auctions.length]);



  // 최신 순으로 10개 정렬
  const sorted = useMemo(() => {
    return [...auctions]
      .filter((a) => a.createdAt)
      .sort(
        (a, b) =>
          new Date(b.createdAt!).getTime() - new Date(a.createdAt!).getTime()
      )
      .slice(0, 10);
  }, [auctions]);

  const handleAfterChange = (current: number) => {
    setCurrentSlide(current);
  };

  const settings = {
    infinite: true,
    speed: 700,
    cssEase: 'ease-in-out',
    centerMode: true,
    centerPadding: '0px',
    slidesToShow,
    slidesToScroll: 1,
    autoplay: true,
    autoplaySpeed: 3500,
    arrows: false,
    dots: false,
    afterChange: handleAfterChange,
  responsive: [
    { breakpoint: 1920, settings: { slidesToShow: 7 } },
    { breakpoint: 1600, settings: { slidesToShow: 5 } },
    { breakpoint: 1200, settings: { slidesToShow: 3 } },
    { breakpoint: 768, settings: { slidesToShow: 1 } },
  ],
  };

  return (
    <section className="relative px-4 mt-14 weekly-auction-container">
      <h2 className="text-xl font-bold text-center mb-6">금주의 옥션상품</h2>

      <div className="relative">
        <Slider ref={sliderRef} {...settings}>
          {sorted.map((auction, index) => {
            // centerMode에서는 slick-center 클래스를 사용하는 것이 더 안정적
            return (
              <div key={`${auction.id}-${index}`} className="px-2">
                <div className="realive-auction-slide">
                  <Link href={`/auctions/${auction.id}`}>
                    <div className="w-full max-w-[250px] mx-auto aspect-square rounded-lg shadow overflow-hidden bg-gray-100">
                      <img
                        src={
                          auction.adminProduct?.imageUrl || '/images/placeholder.png'
                        }
                        alt={auction.adminProduct?.productName || '상품 이미지'}
                        className="object-contain w-full h-full"
                      />
                    </div>
                  </Link>
                  <p className="text-sm text-center mt-1 truncate">
                    {auction.adminProduct?.productName || '상품 없음'}
                  </p>
                </div>
              </div>
            );
          })}
        </Slider>

        {/* 왼쪽 화살표 */}
        <button
          onClick={() => sliderRef.current?.slickPrev()}
          className="absolute z-10 top-[50%] left-[20%] -translate-y-1/2 text-3xl bg-white shadow rounded-full p-2 hover:scale-110 transition"
          type="button"
        >
          &#10094;
        </button>

        {/* 오른쪽 화살표 */}
        <button
          onClick={() => sliderRef.current?.slickNext()}
          className="absolute z-10 top-[50%] right-[20%] -translate-y-1/2 text-3xl bg-white shadow rounded-full p-2 hover:scale-110 transition"
          type="button"
        >
          &#10095;
        </button>
      </div>

      {/* 스타일 */}
      <style jsx global>{`
        /* 해당 컴포넌트에만 적용되도록 범위 제한 */
        .weekly-auction-container .realive-auction-slide {
          transition: transform 0.4s ease, filter 0.4s ease;
          transform: scale(0.9);
          filter: brightness(0.92);
          opacity: 0.7;
        }

        /* centerMode에서 slick이 자동으로 추가하는 slick-center 클래스 활용 */
        .weekly-auction-container .slick-center .realive-auction-slide {
          transform: scale(1.05);
          filter: brightness(1);
          opacity: 1;
          z-index: 2;
        }

        /* 슬라이더 오버플로우 설정으로 잘림 방지 - 해당 컴포넌트에만 적용 */
        .weekly-auction-container .slick-list {
          overflow: visible;
          padding: 20px 0;
        }
        
        .weekly-auction-container .slick-slide {
          overflow: visible;
        }
      `}</style>
    </section>
  );
}