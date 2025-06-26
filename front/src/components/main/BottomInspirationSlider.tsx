'use client';

import Slider from 'react-slick';
import 'slick-carousel/slick/slick.css';
import 'slick-carousel/slick/slick-theme.css';

const inspirationImages = [
    '/images/banner9.jpg',
    '/images/banner10.jpg',
    '/images/banner11.jpg',
    '/images/banner12.jpg',
    '/images/banner13.jpg',
    '/images/banner14.jpg',
    '/images/banner15.jpg',
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
            {
                breakpoint: 1024,
                settings: {
                    slidesToShow: 3,
                },
            },
            {
                breakpoint: 768,
                settings: {
                    slidesToShow: 2,
                },
            },
            {
                breakpoint: 480,
                settings: {
                    slidesToShow: 1,
                },
            },
        ],
    };

    return (
        <div className="w-full bg-white py-10">
            <div className="max-w-screen-xl mx-auto px-4">
                <Slider {...settings}>
                    {inspirationImages.map((src, idx) => (
                        <div key={idx} className="px-2">
                            <div className="rounded-lg overflow-hidden shadow-md">
                                <img
                                    src={src}
                                    alt={`인테리어 ${idx + 1}`}
                                    className="w-full h-48 object-cover"
                                />
                            </div>
                        </div>
                    ))}
                </Slider>
            </div>
        </div>
    );
}
