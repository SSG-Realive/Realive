'use client';

import { useState } from 'react';
import Link from 'next/link';
import { ProductListDTO } from '@/types/seller/product/product';
import { FaHeart, FaRegHeart } from 'react-icons/fa';

export default function ProductCard({
  id,
  name,
  price,
  imageThumbnailUrl,
}: ProductListDTO) {
  const [hovered, setHovered] = useState(false);
  const [liked, setLiked] = useState(false);

  return (
    <Link href={`/main/products/${id}`}>
      <div
        className="relative bg-white rounded-2xl overflow-hidden p-4 hover:shadow-md transition cursor-pointer w-full"
        onMouseEnter={() => setHovered(true)}
        onMouseLeave={() => setHovered(false)}
      >
        {/* 이미지 영역 */}
        <div className="relative w-full aspect-[1/1] rounded-xl overflow-hidden bg-gray-100">
          {imageThumbnailUrl ? (
            <img
              src={imageThumbnailUrl}
              alt={name}
              className="w-full h-full object-cover"
            />
          ) : (
            <div className="w-full h-full flex items-center justify-center text-gray-500 text-sm">
              이미지 없음
            </div>
          )}

          {/* 하트 버튼 (hover 시 노출) */}
          {hovered && (
            <button
              className="absolute top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2
                bg-white/70 backdrop-blur-sm text-red-500 p-3 rounded-full shadow-lg hover:bg-white transition z-10"
              onClick={(e) => {
                e.preventDefault();
                setLiked((prev) => !prev);
              }}
              type="button"
            >
              {liked ? (
                <FaHeart className="w-6 h-6 text-red-500" />
              ) : (
                <FaRegHeart className="w-6 h-6 text-gray-400" />
              )}
            </button>
          )}
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
