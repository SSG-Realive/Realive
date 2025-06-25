'use client';

import { useEffect, useState } from 'react';
import { fetchPopularProducts } from '@/service/customer/productService';
import { ProductListDTO } from '@/types/seller/product/product';
import Link from 'next/link';

export default function PopularProductsGrid() {
  const [products, setProducts] = useState<ProductListDTO[]>([]);

  useEffect(() => {
    fetchPopularProducts()
      .then((data) => setProducts(data.slice(0, 50)))
      .catch((err) => {
        console.error('인기 상품 불러오기 실패:', err);
      });
  }, []);

  return (
    <section className="max-w-screen-2xl mx-auto bg-gray-50 rounded-2xl py-10 px-20 mt-10 ">
      <h2 className="text-2xl font-bold text-gray-800 mb-6">인기상품</h2>

      <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-6">
        {products.map((product) => (
          <Link
            key={product.id}
            href={`/main/products/${product.id}`}
            className="flex flex-col items-start hover:scale-[1.015] transition-transform"
          >
            <div className="w-full aspect-square bg-gray-100 overflow-hidden rounded-xl">
              {product.imageThumbnailUrl ? (
                <img
                  src={product.imageThumbnailUrl}
                  alt={product.name}
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-400 text-sm">
                  이미지 없음
                </div>
              )}
            </div>
            <p className="mt-3 text-sm font-medium text-gray-800 truncate w-full text-left">
              {product.name}
            </p>
            <p className="text-gray-800 font-semibold text-sm mt-1 text-left">
              {product.price.toLocaleString()}원
            </p>
          </Link>
        ))}
      </div>
    </section>
  );
}
