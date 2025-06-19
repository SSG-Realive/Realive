'use client';

import { useEffect, useState } from 'react';
import { fetchWishlist, toggleWishlist } from '@/service/customer/wishlistService';
import { ProductListDTO } from '@/types/seller/product/product';
import Navbar from '@/components/customer/Navbar';

export default function WishlistPage() {
    const [products, setProducts] = useState<ProductListDTO[]>([]);

    useEffect(() => {
        fetchWishlist().then(setProducts);
    }, []);

    const handleToggle = async (productId: number) => {
        const result = await toggleWishlist({ productId });
        if (!result) return;

        setProducts((prev) => prev.filter((p) => p.id !== productId));
    };

    return (
        <>
            <Navbar />
            <main className="max-w-6xl mx-auto px-6 py-10">
                <h1 className="text-2xl font-bold mb-6">찜한 상품 전체 보기</h1>

                {products.length === 0 ? (
                    <p className="text-gray-500">찜한 상품이 없습니다.</p>
                ) : (
                    <ul className="grid grid-cols-2 md:grid-cols-3 gap-6">
                        {products.map((p) => (
                            <li key={p.id} className="border p-3 rounded shadow-sm">
                                <img
                                    src={p.imageThumbnailUrl}
                                    alt={p.name}
                                    className="w-full h-40 object-cover rounded"
                                />
                                <div className="mt-2 font-medium">{p.name}</div>
                                <button
                                    className="text-red-500 text-sm mt-1"
                                    onClick={() => handleToggle(p.id)}
                                >
                                    ❤️ 찜 해제
                                </button>
                            </li>
                        ))}
                    </ul>
                )}
            </main>
        </>
    );
}
