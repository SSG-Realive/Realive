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
                                        isWished: initialWished = false,
                                    }: ProductListDTO & { isWished?: boolean }) {
    const [isWished, setIsWished] = useState<boolean>(initialWished);
    const [loading, setLoading] = useState<boolean>(false);

    const handleToggle = async (e: React.MouseEvent) => {
        e.preventDefault();
        if (loading) return;

        setLoading(true);
        try {
            const result = await toggleWishlist({ productId: id });
            setIsWished(result);


            if (result) {
                alert('찜 목록에 추가되었습니다.');
            } else {
                alert('찜 목록에서 제거되었습니다.');
            }
        } catch (err) {
            console.error('찜 토글 실패:', err);
            alert('찜 처리 중 오류가 발생했습니다.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <Link href={`/main/products/${id}`}>
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
                        disabled={loading}
                    >
                        {isWished ? '❤️' : '🤍'}
                    </button>
                </div>
            </div>
        </Link>
    );
}
