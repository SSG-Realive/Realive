'use client';

import { useState } from 'react';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { Heart, HeartIcon, ShoppingCart } from 'lucide-react';

import ProductImage from '@/components/ProductImage';
import { ProductListDTO } from '@/types/seller/product/product';
import { toggleWishlist } from '@/service/customer/wishlistService';
import { addToCart } from '@/service/customer/cartService';
import { useAuthStore } from '@/store/customer/authStore';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function ProductCard({
                                      id,
                                      name,
                                      price,
                                      imageThumbnailUrl,
                                      isWished = false,
                                    }: ProductListDTO) {
  // 상태
  const [liked, setLiked] = useState(isWished);
  const [loading, setLoading] = useState(false);

  // 도구
  const router = useRouter();
  const pathname = usePathname();
  const { show } = useGlobalDialog();

  // 인증 확인
  const withAuth = async (action: () => Promise<void>) => {
    if (!useAuthStore.getState().accessToken) {
      await show('로그인이 필요한 서비스입니다.');
      router.push(
          `/customer/member/login?redirectTo=${encodeURIComponent(pathname)}`
      );
      return;
    }
    await action();
  };

  // 찜 버튼 핸들러
  const handleWishClick = (e: React.MouseEvent) => {
    e.preventDefault();
    withAuth(async () => {
      setLoading(true);
      try {
        const newState = await toggleWishlist({ productId: id });
        setLiked(newState);
        show(
            newState
                ? '찜한 상품에 추가되었습니다.'
                : '찜 목록에서 제거되었습니다.'
        );
      } finally {
        setLoading(false);
      }
    });
  };

  // 장바구니 버튼 핸들러
  const handleCartClick = (e: React.MouseEvent) => {
    e.preventDefault();
    withAuth(() =>
        addToCart({ productId: id, quantity: 1 }).then(() =>
            show('장바구니에 담았습니다.')
        )
    );
  };

  // UI
  return (
      <Link href={`/main/products/${id}`} prefetch={false}>
        <div className="group relative bg-white rounded-none p-4 hover:shadow-md transition">
          {/* --- 이미지 --- */}
          <div className="relative w-full aspect-square rounded-none overflow-hidden bg-gray-100">
            {imageThumbnailUrl ? (
                <ProductImage
                    src={imageThumbnailUrl}
                    alt={name}
                    className="w-full h-full object-cover"
                />
            ) : (
                <div className="flex items-center justify-center w-full h-full text-sm text-gray-500">
                  이미지 없음
                </div>
            )}

            {/* 장바구니 버튼 — 좌측 상단 */}
            <button
                onClick={handleCartClick}
                title="장바구니 담기"
                className="absolute top-2 left-2 bg-white/80 backdrop-blur-sm
                       p-2 rounded-full shadow hover:bg-white transition"
            >
              <ShoppingCart size={18} className="text-gray-600" />
            </button>

            {/* 찜 버튼 — 우측 상단 */}
            <button
                onClick={handleWishClick}
                disabled={loading}
                title={liked ? '찜 취소' : '찜하기'}
                className="absolute top-2 right-2 bg-white/80 backdrop-blur-sm
                       p-2 rounded-full shadow hover:bg-white transition"
            >
              {liked ? (
                  <Heart size={18} className="fill-red-500 text-red-500" />
              ) : (
                  <HeartIcon size={18} className="text-gray-400" />
              )}
            </button>
          </div>

          {/* 텍스트 정보 */}
          <div className="mt-1">
            <p className="text-sm font-light text-gray-800 truncate text-center">{name}</p>
            <p className="text-xs font-light text-gray-700 text-right mt-0.5">
              KRW {price.toLocaleString()}
            </p>
          </div>
        </div>
      </Link>
  );
}
