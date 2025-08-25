'use client';

import { useEffect, useState } from 'react';
import { ProductListDTO } from '@/types/seller/product/product';
import { fetchPublicProducts } from '@/service/customer/productService';
import ProductCard from './ProductCard';

interface SectionProps {
    title: string;
    categoryId: number;
    limit?: number; // ⬅️ limit 추가 (기본값 10)
}

export default function Section({ title, categoryId, limit = 10 }: SectionProps) {
    const [products, setProducts] = useState<ProductListDTO[]>([]);

    useEffect(() => {
        fetchPublicProducts(categoryId, 1, limit, '').then(setProducts);
    }, [categoryId, limit]);

    return (
        <section className="max-w-screen-xl mx-auto px-4 py-10">
            <h2 className="text-xl font-light text-gray-800 mb-2">{title}</h2>
            <p className="text-sm text-gray-500 mb-6">2차 카테고리</p>

            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-4">
                {products.map((product) => (
                    <ProductCard key={product.id} {...product} />
                ))}
            </div>
        </section>
    );
}
