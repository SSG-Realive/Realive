'use client';

import { useState } from 'react';
import Link from 'next/link';
import { ProductListDTO } from '@/types/seller/product/product';
import { FaHeart, FaRegHeart } from 'react-icons/fa';
import { toggleWishlist } from '@/service/customer/wishlistService';
import type { WishlistToggleRequest } from '@/types/customer/wishlist/wishlist';
import ProductImage from '@/components/ProductImage';

export default function ProductCard({
                                        id,
                                        name,
                                        price,
                                        imageThumbnailUrl,
                                        isWished = false,
                                    }: ProductListDTO) {
    const [hovered, setHovered] = useState(false);
    const [liked, setLiked] = useState(isWished);

    return (
        <Link href={`/main/products/${id}`}>
            <div
                className="group relative bg-white rounded-2xl overflow-hidden p-4 hover:shadow-md transition cursor-pointer w-full"
                onMouseEnter={() => setHovered(true)}
                onMouseLeave={() => setHovered(false)}
            >
                {/* 이미지 영역 */}
                <div className="relative w-full aspect-[1/1] rounded-xl overflow-hidden bg-gray-100">
                    {imageThumbnailUrl ? (
                        <ProductImage
                            src={imageThumbnailUrl}
                            alt={name}
                            className="w-full h-full object-cover"
                        />
                    ) : (
                        <div className="w-full h-full flex items-center justify-center text-gray-500 text-sm">
                            이미지 없음
                        </div>
                    )}

                    {/* ✅ 하트 버튼 - 모바일 항상 보임, PC는 hover */}
                    <button
                        className="absolute top-2 right-2
             bg-white/70 backdrop-blur-sm p-1.5 sm:p-2 rounded-full shadow-lg
             hover:bg-white transition z-10 flex"
                        onClick={async (e) => {
                            e.preventDefault();
                            const newLiked = !liked;
                            setLiked(newLiked);
                            try {
                                const result = await toggleWishlist({ productId: id });
                                setLiked(result);
                            } catch (error) {
                                console.error('찜 토글 실패:', error);
                                alert('찜 처리 중 오류가 발생했습니다.');
                                setLiked(!newLiked);
                            }
                        }}
                        type="button"
                    >
                        {liked ? (
                            <FaHeart className="w-4 h-4 sm:w-5 sm:h-5 text-red-500" />
                        ) : (
                            <FaRegHeart className="w-4 h-4 sm:w-5 sm:h-5 text-gray-400" />
                        )}
                    </button>

                </div>

                {/* 텍스트 영역 */}
                <div className="mt-4 text-black">
                    <p className="text-base font-medium truncate text-left">{name}</p>
                    <p className="text-sm font-semibold text-left text-gray-800 mt-1">
                        {price.toLocaleString()}
                        <span className="text-xs align-middle ml-1">원</span>
                    </p>
                </div>
            </div>
        </Link>
    );
}
