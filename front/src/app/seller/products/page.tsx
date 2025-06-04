'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import apiClient from '@/lib/apiClient';

interface Product {
    id: number;
    name: string;
    price: number;
    status: string;
    isActive: boolean;
    imageUrl: string;
}

export default function SellerProductList() {
    const router = useRouter();
    const [products, setProducts] = useState<Product[]>([]);

    useEffect(() => {
        apiClient.get('/seller/products')
            .then(res => setProducts(res.data))
            .catch(() => alert('상품 목록 불러오기 실패'));
    }, []);

    return (
        <div className="p-8">
            <div className="flex justify-between items-center mb-4">
                <h1 className="text-2xl font-bold">상품 목록</h1>
                <button
                    className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                    onClick={() => router.push('/seller/products/new')}
                >
                    상품 등록
                </button>
            </div>

            <div className="grid gap-4 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
                {products.map((product) => (
                    <div key={product.id} className="border p-4 rounded shadow-sm">
                        <img src={product.imageUrl} alt={product.name} className="w-full h-48 object-cover rounded" />
                        <h2 className="text-xl font-semibold mt-2">{product.name}</h2>
                        <p className="text-gray-600">{product.price.toLocaleString()}원</p>
                        <p className={`text-sm ${product.isActive ? 'text-green-600' : 'text-red-500'}`}>
                            {product.isActive ? '판매중' : '판매중지'}
                        </p>
                        <button
                            className="mt-2 text-blue-600 hover:underline"
                            onClick={() => router.push(`/seller/products/edit/${product.id}`)}
                        >
                            수정하기
                        </button>
                    </div>
                ))}
            </div>
        </div>
    );
}