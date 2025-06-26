'use client';

import { useEffect, useRef, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import Navbar from '@/components/customer/common/Navbar';
import { fetchProductDetail, fetchRelatedProducts } from '@/service/customer/productService';
import { toggleWishlist } from '@/service/customer/wishlistService';
import { addToCart } from '@/service/customer/cartService';
import { fetchReviewsBySeller } from '@/service/customer/reviewService';
import ReviewList from '@/components/customer/review/ReviewList';
import { ProductDetail, ProductListDTO } from '@/types/seller/product/product';
import { ReviewResponseDTO } from '@/types/customer/review/review';
import { FaHeart, FaRegHeart } from 'react-icons/fa';

export default function ProductDetailPage() {
    const { id } = useParams();
    const router = useRouter();
    const [product, setProduct] = useState<ProductDetail | null>(null);
    const [related, setRelated] = useState<ProductListDTO[]>([]);
    const [reviews, setReviews] = useState<ReviewResponseDTO[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [isWished, setIsWished] = useState(false);
    const [wishlistLoading, setWishlistLoading] = useState(false);
    const [quantity, setQuantity] = useState(1);
    const triggerRef = useRef<HTMLDivElement>(null);
    const [isFooterSticky, setIsFooterSticky] = useState(false);

    useEffect(() => {
        if (!id) return;
        const productId = Number(id);
        fetchProductDetail(productId).then(setProduct).catch(() => setError('상품을 불러오지 못했습니다.'));
        fetchRelatedProducts(productId).then(setRelated).catch(() => {});
    }, [id]);

    useEffect(() => {
        if (product?.sellerId) {
            fetchReviewsBySeller(product.sellerId).then(setReviews);
        }
    }, [product]);

    useEffect(() => {
        const handleScroll = () => {
            if (triggerRef.current) {
                const { top } = triggerRef.current.getBoundingClientRect();
                setIsFooterSticky(top <= 0);
            }
        };
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    const handleToggleWishlist = async () => {
        if (!product || wishlistLoading) return;
        setWishlistLoading(true);
        try {
            await toggleWishlist({ productId: product.id });
            setIsWished((prev) => !prev);
        } catch {
            router.push('/login');
        } finally {
            setWishlistLoading(false);
        }
    };

    const handleAddToCart = async () => {
        if (!product || quantity <= 0) return;
        try {
            await addToCart({ productId: product.id, quantity });
            alert('장바구니에 추가되었습니다.');
        } catch {
            alert('장바구니 추가 실패');
        }
    };

    const handleBuyNow = () => {
        if (!product || quantity <= 0) return;
        sessionStorage.setItem('directBuyProductId', product.id.toString());
        sessionStorage.setItem('directBuyQuantity', quantity.toString());
        router.push('/customer/orders/direct');
    };

    if (error) return <div className="text-red-500 text-sm">{error}</div>;
    if (!product) return <div className="text-sm">로딩 중...</div>;
    const totalDisplayPrice = (product.price * quantity).toLocaleString();

    return (
        <div>
            <Navbar />
            <div className="max-w-6xl mx-auto px-4 py-10 grid grid-cols-1 md:grid-cols-2 gap-8">
                <div>
                    <img
                        src={product.imageThumbnailUrl || '/default-thumbnail.png'}
                        alt={product.name}
                        className="w-full h-96 object-contain rounded-lg shadow-md"
                    />
                </div>
                <div>
                    <h1 className="text-2xl font-bold text-black mb-2">{product.name}</h1>
                    <p className="text-sm text-gray-700 mb-4">{product.description}</p>
                    <p className="text-xl font-bold text-black mb-6">{product.price.toLocaleString()}원</p>

                    <div className="mb-6 space-y-2 text-sm text-gray-700">
                        <p><span className="font-semibold">상품상태:</span> {product.status}</p>
                        <p><span className="font-semibold">재고:</span> {product.stock}개</p>
                        {product.width && product.depth && product.height && (
                            <p><span className="font-semibold">사이즈:</span> {product.width} x {product.depth} x {product.height} cm</p>
                        )}
                        {product.categoryName && <p><span className="font-semibold">카테고리:</span> {product.categoryName}</p>}
                        {product.sellerName && <p><span className="font-semibold">판매자:</span> {product.sellerName}</p>}
                    </div>

                    <div className="border-t border-b py-6 mb-8">
                        <div className="flex items-center justify-between mb-4">
                            <span className="font-semibold text-sm">수량</span>
                            <div className="flex items-center border rounded-md">
                                <button
                                    onClick={() => setQuantity((prev) => Math.max(1, prev - 1))}
                                    className="px-3 py-1 text-sm font-bold text-gray-600 hover:bg-gray-100 rounded-l-md"
                                >-</button>
                                <input
                                    type="text"
                                    inputMode="numeric"
                                    pattern="[0-9]*"
                                    value={quantity}
                                    onChange={(e) => {
                                        const value = e.target.value;
                                        if (value === '' || /^\d+$/.test(value)) {
                                            const num = Number(value);
                                            if (num >= 1 && num <= product.stock) setQuantity(num);
                                        }
                                    }}
                                    onBlur={() => {
                                        if (quantity < 1) setQuantity(1);
                                        if (quantity > product.stock) setQuantity(product.stock);
                                    }}
                                    className="w-16 text-center border-l border-r py-1 text-sm"
                                />
                                <button
                                    onClick={() => setQuantity((prev) => Math.min(product.stock, prev + 1))}
                                    className="px-3 py-1 text-sm font-bold text-gray-600 hover:bg-gray-100 rounded-r-md"
                                >+</button>
                            </div>
                        </div>
                        <div className="flex items-center justify-between text-base font-bold mb-6">
                            <span>총 상품금액</span>
                            <span className="text-black">{totalDisplayPrice}원</span>
                        </div>
                        <div className="flex space-x-3">
                            <button
                                onClick={handleToggleWishlist}
                                disabled={wishlistLoading}
                                className="w-10 h-10 flex items-center justify-center rounded-full border border-gray-300 bg-white hover:bg-gray-100 transition"
                            >
                                {isWished ? (
                                    <FaHeart className="w-5 h-5 text-red-500" />
                                ) : (
                                    <FaRegHeart className="w-5 h-5 text-gray-400" />
                                )}
                            </button>
                            <button
                                onClick={handleAddToCart}
                                className="flex-1 px-5 py-3 bg-white text-gray-700 text-sm font-semibold rounded-lg border border-gray-300 hover:bg-gray-100"
                            >
                                장바구니
                            </button>
                            <button
                                onClick={handleBuyNow}
                                className="flex-1 px-5 py-3 bg-black text-white text-sm font-semibold rounded-lg hover:bg-gray-900"
                            >
                                바로 구매
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <div className="max-w-6xl mx-auto px-4 mt-0 pb-52">
                <div className="mt-2">
                    <h2 className="text-lg font-bold mb-4">판매자 리뷰</h2>
                    {reviews.length > 0 ? (
                        <ReviewList reviews={reviews} />
                    ) : (
                        <p className="text-sm text-gray-600">아직 등록된 리뷰가 없습니다.</p>
                    )}
                </div>

                {related.length > 0 && (
                    <div className="mt-8">
                        <h2 className="text-lg font-bold mb-4">추천상품</h2>
                        <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-4 mb-8">
                            {related.map((item) => (
                                <div
                                    key={item.id}
                                    className="bg-white w-full flex flex-col rounded-md overflow-hidden shadow-sm hover:shadow-md transition-shadow cursor-pointer"
                                    onClick={() => router.push(`/main/products/${item.id}`)}
                                >
                                    <img
                                        src={item.imageThumbnailUrl || '/default-thumbnail.png'}
                                        alt={item.name}
                                        className="w-full aspect-[4/3] object-cover"
                                    />
                                    <div className="p-3 flex flex-col justify-between flex-1">
                                        <p className="text-sm font-medium text-black truncate">{item.name}</p>
                                        <p className="text-sm font-bold text-black mt-1">{item.price.toLocaleString()}원</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}

                <div ref={triggerRef} className="h-10" />
            </div>

            <div
                className={`fixed bottom-0 left-0 right-0 bg-white border-t p-4 shadow-lg-top transform transition-transform duration-300 ease-in-out ${isFooterSticky ? 'translate-y-0' : 'translate-y-full'} z-20`}
            >
                <div className="max-w-4xl mx-auto flex justify-between items-center">
                    <div>
                        <p className="text-sm font-semibold truncate">{product.name}</p>
                        <p className="text-base font-bold text-black">{product.price.toLocaleString()}원</p>
                    </div>
                    <button
                        onClick={handleAddToCart}
                        className="bg-red-500 text-white font-bold px-8 py-3 rounded-md hover:bg-red-600 transition-colors text-sm"
                    >
                        바로 주문
                    </button>
                </div>
            </div>
        </div>
    );
}
