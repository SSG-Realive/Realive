'use client';

import { fetchFeaturedSellersWithProducts } from "@/service/customer/productService";
import { FeaturedSellerWithProducts } from "@/types/product";
import { useEffect, useState } from "react";
import Slider from "react-slick";
import "slick-carousel/slick/slick.css";
import "slick-carousel/slick/slick-theme.css";
import ProductImage from "@/components/ProductImage";

export default function FeaturedSellersSection() {
    const [featured, setFeatured] = useState<FeaturedSellerWithProducts[]>([]);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchFeaturedSellersWithProducts()
            .then(data => {
                const valid = data.filter(seller => seller.products.length > 0);
                const shuffled = valid.sort(() => 0.5 - Math.random());
                const picked = shuffled.slice(0, 3);
                setFeatured(picked);
                setLoading(false);
            })
            .catch(err => {
                console.error('추천 섹션 로드 실패', err);
                setError(err.message ?? '알 수 없는 에러');
                setLoading(false);
            });
    }, []);

    if (loading) {
        return <div className="text-center py-8">로딩 중...</div>;
    }

    if (error) {
        return <div className="text-center py-8 text-red-500">추천 섹션을 불러올 수 없습니다: {error}</div>;
    }

    return (
        <section className="my-10 px-4">
            {/* 🔧 슬라이더 dot 스타일 */}
            <style>{`
        .slick-dots {
          display: flex !important;
          justify-content: center;
          margin-top: 12px;
        }
        .slick-dots li {
          margin: 0 4px;
        }
        .slick-dots li button:before {
          color: #bbb;
          font-size: 10px;
        }
        .slick-dots li.slick-active button:before {
          color: #111;
        }
      `}</style>

            <div className="max-w-7xl mx-auto p-6">
                <h2 className="text-2xl font-semibold text-gray-800 text-center mb-6">
                    Today’s Seller Picks
                </h2>

                <div className="space-y-10">
                    {featured.map((seller) => (
                        <div key={seller.sellerId}>
                            <h3 className="text-lg font-bold text-gray-700 mb-4 text-center">
                                {seller.sellerName}
                            </h3>

                            <Slider
                                dots={true} // ✅ 하단 점 추가
                                infinite={true}
                                speed={500}
                                slidesToShow={3}
                                slidesToScroll={1}
                                autoplay={true}
                                autoplaySpeed={3000}
                                pauseOnHover={true}
                                responsive={[
                                    { breakpoint: 1024, settings: { slidesToShow: 2 } },
                                    { breakpoint: 640, settings: { slidesToShow: 1 } },
                                ]}
                            >
                                {seller.products.map((product) => (
                                    <div key={product.productId} className="px-2">
                                        <div className="w-full max-w-xs mx-auto text-left">
                                            {/* ✅ 이미지 비율 고정 */}
                                            <div className="relative aspect-[4/3] mb-2">
                                                <ProductImage
                                                    src={product.imageThumbnailUrl}
                                                    alt={product.name}
                                                    className="absolute top-0 left-0 w-full h-full object-cover rounded-lg shadow"
                                                />
                                            </div>

                                            <div className="mt-4 text-black">
                                                <p className="text-base font-medium truncate">
                                                    {product.name}
                                                </p>
                                                <p className="text-sm font-semibold text-gray-800 mt-1">
                                                    {product.price.toLocaleString()}
                                                    <span className="text-xs align-middle ml-1">원</span>
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </Slider>
                        </div>
                    ))}
                </div>
            </div>
        </section>
    );
}
