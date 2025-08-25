'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { fetchWishlist, toggleWishlist } from '@/service/customer/wishlistService';
import { ProductListDTO } from '@/types/seller/product/product';
import MypageCard from '../MypageCard';

export default function Wishlist() {
    const [products, setProducts] = useState<ProductListDTO[]>([]);
    const [loadingId, setLoadingId] = useState<number | null>(null);

    useEffect(() => {
        fetchWishlist().then(setProducts);
    }, []);

    const handleToggle = async (productId: number) => {
        if (loadingId === productId) return;
        setLoadingId(productId);

        try {
            const isWished = await toggleWishlist({ productId });

            if (!isWished) {
                // 찜 해제된 경우에만 목록에서 제거
                setProducts((prev) => prev.filter((p) => p.id !== productId));
            }
            // 찜 추가된 경우는 UI 그대로 유지
        } finally {
            setLoadingId(null);
        }
    };

    return (
        <MypageCard title="찜 목록">
            {products.length === 0 ? (
                <p className="text-gray-500">찜한 상품이 없습니다.</p>
            ) : (
                <>
                    <ul className="grid grid-cols-2 gap-4">
                        {products.slice(0, 2).map((p) => (
                            <li key={p.id} className="border p-2 rounded">
                                <img
                                    src={p.imageThumbnailUrl}
                                    alt={p.name}
                                    className="w-full h-32 object-cover rounded"
                                />
                                <div className="mt-1 text-sm font-light">{p.name}</div>
                                <button
                                    className="text-red-500 text-sm mt-1"
                                    onClick={() => handleToggle(p.id)}
                                    disabled={loadingId === p.id}
                                >
                                    ❤️ 찜 해제
                                </button>
                            </li>
                        ))}
                    </ul>

                    <div className="mt-3 text-right">
                        <Link
                            href="/customer/mypage/wishlist"
                            className="text-blue-500 text-sm hover:underline"
                        >
                            전체
                        </Link>
                    </div>
                </>
            )}
        </MypageCard>
    );
}
