'use client';

import Slider from 'react-slick';
import Link from 'next/link';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

const inspirationImages = [
    { src: '/images/banner10.jpg', link: '/main?category=25' },
    { src: '/images/banner11.jpg', link: '/main?category=25' },
    { src: '/images/banner12.jpg', link: '/main?category=20' },
    { src: '/images/banner13.jpg', link: '/main?category=25' },
    { src: '/images/banner14.jpg', link: '/main?category=20' },
    { src: '/images/banner15.jpg', link: '/main?category=10' },
];

export default function BottomInspirationSlider() {
    const settings = {
        dots: false,
        infinite: true,
        speed: 1000,
        slidesToShow: 4,
        slidesToScroll: 1,
        autoplay: true,
        autoplaySpeed: 3000,
        responsive: [
            { breakpoint: 1024, settings: { slidesToShow: 3 } },
            { breakpoint: 768, settings: { slidesToShow: 2 } },
            { breakpoint: 480, settings: { slidesToShow: 1 } },
        ],
    };

    return (
        <div className="w-full bg-white py-10">
            {/* ✅ w-full로 확장, padding 제거 */}
            <div className="relative w-full overflow-visible px-0">
                <Slider {...settings}>
                    {inspirationImages.map((item, idx) => (
                        <div key={idx} className="px-2">
                            <Link href={item.link}>
                                <div className="rounded-lg overflow-hidden shadow-md cursor-pointer">
                                    <img
                                        src={item.src}
                                        alt={`인테리어 ${idx + 1}`}
                                        className="
  w-full
  h-[200px]
  sm:h-[260px]
  md:h-[320px]
  object-cover
  rounded-lg
"
                                    />
                                </div>
                            </Link>
                        </div>
                    ))}
                </Slider>
            </div>
        </div>
    );
}
