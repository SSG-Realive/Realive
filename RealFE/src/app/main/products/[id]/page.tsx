'use client';

import { useEffect, useRef, useState, useMemo } from 'react';
import { useParams, useRouter } from 'next/navigation';

import {
  fetchProductDetail,
  fetchRelatedProducts,
} from '@/service/customer/productService';
import { toggleWishlist } from '@/service/customer/wishlistService';
import { addToCart } from '@/service/customer/cartService';
import { fetchReviewsBySeller } from '@/service/customer/reviewService';
import { getProductQnaList } from '@/service/customer/customerQnaService';

import ReviewList from '@/components/customer/review/ReviewList';
import QnaList from '@/components/customer/qna/QnaList';
import TrafficLightStatusCardforProductDetail from '@/components/seller/TrafficLightStatusCardforProductDetail';

import { ProductDetail, ProductListDTO } from '@/types/seller/product/product';
import { ReviewResponseDTO } from '@/types/customer/review/review';
import { CustomerQnaResponse, CustomerQnaListResponse } from '@/types/customer/qna/customerQnaResponse';

import { FaHeart, FaRegHeart } from 'react-icons/fa';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function ProductDetailPage() {
  const { id } = useParams<{ id: string }>();
  const router = useRouter();
  const { show } = useGlobalDialog();

  const [product, setProduct] = useState<ProductDetail | null>(null);
  const [related, setRelated] = useState<ProductListDTO[]>([]);
  const [reviews, setReviews] = useState<ReviewResponseDTO[]>([]);
  const [qnas, setQnas] = useState<CustomerQnaResponse[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [isWished, setIsWished] = useState(false);
  const [wishlistLoading, setWishlistLoading] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const triggerRef = useRef<HTMLDivElement>(null);
  const [sticky, setSticky] = useState(false);
  const [mainImage, setMainImage] = useState<string>('');

  useEffect(() => {
    if (!id) return;
    const pid = Number(id);
    fetchProductDetail(pid)
        .then((data) => {
          setProduct(data);

          const thumbnail = data.imageThumbnailUrl;
          const images = data.imageUrls ?? [];
          const firstImage = thumbnail || images[0] || '/default-thumbnail.png';
          setMainImage(firstImage);
        })
        .catch(() => setError('상품을 불러오지 못했습니다.'));
    fetchRelatedProducts(pid).then(setRelated).catch(() => {});
  }, [id]);

  useEffect(() => {
    if (product?.sellerId) {
      fetchReviewsBySeller(product.sellerId).then(setReviews);
    }
    if (id) {
      const pid = Number(id);
      getProductQnaList(pid)
          .then((res: CustomerQnaListResponse) => setQnas(res.content))
          .catch(console.error);
    }
  }, [product, id]);

  useEffect(() => {
    const onScroll = () => {
      if (triggerRef.current) {
        const { top } = triggerRef.current.getBoundingClientRect();
        setSticky(top <= 0);
      }
    };
    window.addEventListener('scroll', onScroll);
    return () => window.removeEventListener('scroll', onScroll);
  }, []);

  const { averageRating, reviewCount } = useMemo(() => {
    if (!reviews.length) return { averageRating: 0, reviewCount: 0 };
    const totalRating = reviews.reduce((sum, review) => sum + review.rating, 0);
    return {
      averageRating: parseFloat((totalRating / reviews.length).toFixed(1)),
      reviewCount: reviews.length,
    };
  }, [reviews]);

  const imageList = useMemo(() => {
    if (!product) return [];
    const thumbnail = product.imageThumbnailUrl;
    const images = product.imageUrls ?? [];
    const set = new Set<string>();
    if (thumbnail) set.add(thumbnail);
    images.forEach((img) => set.add(img));
    return Array.from(set);
  }, [product]);

  const withAuth = async (action: () => Promise<void>) => await action();

  const handleToggleWishlist = () =>
      withAuth(async () => {
        if (!product || wishlistLoading) return;
        setWishlistLoading(true);
        try {
          await toggleWishlist({ productId: product.id });
          setIsWished((prev) => !prev);
        } finally {
          setWishlistLoading(false);
        }
      });

  const handleAddToCart = () =>
      withAuth(async () => {
        if (!product || quantity <= 0) return;
        await addToCart({ productId: product.id, quantity });
        show('장바구니에 추가되었습니다.');
      });

  const handleBuyNow = () =>
      withAuth(async () => {
        if (!product || quantity <= 0) return;
        sessionStorage.setItem('directBuyProductId', product.id.toString());
        sessionStorage.setItem('directBuyQuantity', quantity.toString());
        router.push('/customer/mypage/orders/direct');
      });

  const handleWriteQna = () => {
    withAuth(async () => {
      router.push(`/customer/qna/write?productId=${id}`);
    });
  };

  if (error) return <p className="text-red-500">{error}</p>;
  if (!product) return <p>로딩 중…</p>;

  return (
      <div>
        <div className="max-w-6xl mx-auto px-4 py-10 grid grid-cols-1 md:grid-cols-2 gap-8">
          {/* 대표 이미지 + 썸네일 */}
          <div>
            <div className="w-full max-h-[500px] bg-white overflow-hidden shadow-sm">
              <img
                  src={mainImage}
                  alt={product.name}
                  className="w-full h-auto object-cover"
              />
            </div>
            {imageList.length > 0 && (
                <div className="mt-4">
                  <div className="flex gap-2 overflow-x-auto no-scrollbar pb-2 sm:justify-center">
                    {imageList.map((url, idx) => (
                        <button
                            key={idx}
                            onClick={() => setMainImage(url)}
                            className={`flex-shrink-0 w-20 h-20 overflow-hidden transition-all duration-200 
    ${mainImage === url ? 'outline outline-2 outline-gray-300 outline-offset-[-2px]' : ''}`}
                        >
                          <img
                              src={url}
                              alt={`서브 이미지 ${idx + 1}`}
                              className="w-full h-full object-cover"
                          />
                        </button>
                    ))}
                  </div>
                </div>
            )}
          </div>

          {/* 상품 설명 + 버튼 */}
          <div>
            <h1 className="text-2xl font-bold mb-2">{product.name}</h1>
            <p className="text-sm text-gray-700 mb-2 break-words whitespace-pre-wrap">
              {product.description}
            </p>

            <p className="text-base text-gray-800 font-light mb-4">
              상품 금액: KRW {product.price.toLocaleString()}
            </p>

            <div className="mb-6 space-y-2 text-sm text-gray-700">
              <p><span className="font-light">상품상태:</span> {product.status}</p>
              <p><span className="font-light">재고:</span> {product.stock}개</p>
              {product.width && product.depth && product.height && (
                  <p><span className="font-light">사이즈:</span> {product.width}×{product.depth}×{product.height} cm</p>
              )}
              {product.categoryName && (
                  <p><span className="font-light">카테고리:</span> {product.categoryName}</p>
              )}
              {product.sellerName && (
                  <p
                      className="cursor-pointer hover:underline text-blue-600"
                      onClick={() => router.push(`/main/seller/${product.sellerId}`)}
                  >
                    <span className="font-light text-gray-700">판매자:</span> {product.sellerName}
                  </p>
              )}

              <div className="mb-6">
                <TrafficLightStatusCardforProductDetail rating={averageRating} count={reviewCount} />
              </div>
            </div>

            <div className="border-t border-b py-6 mb-8">
              <div className="flex items-center justify-between mb-4">
                <span className="font-light text-sm">수량</span>
                <div className="flex items-center gap-2">
                  <button onClick={() => setQuantity(Math.max(1, quantity - 1))} className="w-6 h-6 text-sm flex items-center justify-center border rounded hover:bg-gray-100">-</button>
                  <span className="w-6 text-center text-sm">{quantity}</span>
                  <button onClick={() => setQuantity(Math.min(product.stock, quantity + 1))} className="w-6 h-6 text-sm flex items-center justify-center border rounded hover:bg-gray-100">+</button>
                </div>
              </div>

              <div className="flex items-center justify-between text-base font-light mb-6">
                <span>총 상품금액</span>
                <span>KRW {(product.price * quantity).toLocaleString()}</span>
              </div>

              <div className="flex gap-3">
                <button onClick={handleToggleWishlist} disabled={wishlistLoading} className="w-10 h-10 flex items-center justify-center bg-white hover:bg-gray-100">
                  {isWished ? <FaHeart className="w-5 h-5 text-red-500" /> : <FaRegHeart className="w-5 h-5 text-gray-400" />}
                </button>
                <button onClick={handleAddToCart} className="flex-1 px-5 py-3 border bg-white hover:bg-gray-100 text-sm font-light">장바구니</button>
                <button onClick={handleBuyNow} className="flex-1 px-5 py-3 bg-black text-white hover:bg-gray-900 text-sm font-light">구매</button>
              </div>
            </div>
          </div>
        </div>

        {/* 리뷰 */}
        <div className="max-w-6xl mx-auto px-4">
          <h2 className="text-lg font-light text-gray-600 mb-4">판매자 리뷰</h2>
          {reviews.length > 0 ? <ReviewList reviews={reviews} /> : <p className="text-sm text-gray-600">아직 등록된 리뷰가 없습니다.</p>}
        </div>

        {/* QnA */}
        <div className="max-w-6xl mx-auto px-4 mt-12">
          <h2 className="text-lg font-light text-gray-600 mb-4">QnA</h2>
          {qnas.length > 0 ? <QnaList qnas={qnas} initialDisplayCount={3} /> : <p className="text-sm text-gray-600 p-4 shadow-sm bg-white">등록된 QnA가 없습니다.</p>}
          <div className="flex justify-end mt-4">
            <button onClick={handleWriteQna} className="px-5 py-2 bg-black text-white rounded-none text-sm font-light hover:bg-gray-900">등록</button>
          </div>
        </div>

        {/* 추천 상품 */}
        {related.length > 0 && (
            <div className="max-w-6xl mx-auto px-4 mt-20">
              <h2 className="text-lg font-light text-gray-600 mb-4">이런 상품은 어떠세요?</h2>
              <div className="flex gap-4 overflow-x-auto no-scrollbar pb-2">
                {related.map((item) => (
                    <div
                        key={item.id}
                        onClick={() => router.push(`/main/products/${item.id}`)}
                        className="w-32 sm:w-36 md:w-40 flex-shrink-0 bg-white rounded-md shadow hover:shadow-md transition overflow-hidden cursor-pointer"
                    >
                      <img
                          src={item.imageThumbnailUrl ?? '/default-thumbnail.png'}
                          alt={item.name}
                          className="w-full aspect-square object-cover"
                      />
                      <div className="p-2">
                        <p className="text-sm font-light truncate text-black text-center">{item.name}</p>
                        <p className="text-sm font-light text-black mt-1 text-right">KRW {item.price.toLocaleString()}</p>
                      </div>
                    </div>
                ))}
              </div>
            </div>
        )}

        <div ref={triggerRef} className="h-10" />

        {/* 하단 플로팅 구매 버튼 */}
        <div className={`fixed bottom-0 left-0 right-0 bg-white border-t p-4 shadow-lg transition-transform duration-300 ${sticky ? 'translate-y-0' : 'translate-y-full'}`}>
          <div className="max-w-4xl mx-auto flex justify-between items-center">
            <div>
              <p className="text-sm font-light truncate">{product.name}</p>
              <p className="text-base font-light">{product.price.toLocaleString()}<span className="text-sm ml-1">원</span></p>
            </div>
            <button onClick={handleBuyNow} className="bg-red-500 text-white px-8 py-3 rounded-md hover:bg-red-600 text-sm font-light">바로 주문</button>
          </div>
        </div>
      </div>
  );
}
