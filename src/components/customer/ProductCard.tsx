'use client';

import { toggleWishlist } from '@/service/customer/wishlistService';
import { ProductListDTO } from '@/types/seller/product/product';
import { useState } from 'react';
import Link from 'next/link';

export default function ProductCard({
                                        id,
                                        name,
                                        price,
                                        imageThumbnailUrl,
                                        isActive,
                                    }: ProductListDTO) {
    const [isWished, setIsWished] = useState<boolean>(false);

    const handleToggle = async (e: React.MouseEvent) => {
        e.preventDefault(); // ✅ 링크 이동 방지
        const res = await toggleWishlist({ productId: id });
        setIsWished(res);
    };

    return (
        <Link href={`/main/products/${id}`}> {/* ✅ 수정된 경로 */}
            <div className="bg-white shadow rounded overflow-hidden border p-3 hover:shadow-md transition">
                {imageThumbnailUrl ? (
                    <img
                        src={imageThumbnailUrl}
                        alt={name}
                        className="w-full h-48 object-cover"
                    />
                ) : (
                    <div className="w-full h-48 bg-gray-100 flex items-center justify-center text-gray-500">
                        이미지 없음
                    </div>
                )}
                <div className="mt-3">
                    <p className="text-sm font-semibold truncate">{name}</p>
                    <p className="text-green-600 font-bold text-sm">
                        {price.toLocaleString()}원
                    </p>
                    <button
                        onClick={handleToggle}
                        className="mt-2 text-xl"
                        aria-label="찜하기 버튼"
                    >
                        {isWished ? '❤️' : '🤍'}
                    </button>
                </div>
            </div>
        </Link>
    );
}
