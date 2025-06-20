'use client';

import { useEffect, useState } from 'react';
import { useParams } from 'next/navigation';
import Navbar from '@/components/customer/common/Navbar';
import { fetchProductDetail, fetchRelatedProducts } from '@/service/customer/productService';
import { toggleWishlist } from '@/service/customer/wishlistService';
import { addToCart } from '@/service/customer/cartService';
import { fetchReviewsBySeller } from '@/service/customer/reviewService';
import ReviewList from '@/components/customer/review/ReviewList';
import { ProductDetail, ProductListDTO } from '@/types/seller/product/product';
import { ReviewResponseDTO } from '@/types/customer/review/review';

export default function ProductDetailPage() {
    const { id } = useParams();
    const [product, setProduct] = useState<ProductDetail | null>(null);
    const [related, setRelated] = useState<ProductListDTO[]>([]);
    const [reviews, setReviews] = useState<ReviewResponseDTO[]>([]);
    const [error, setError] = useState<string | null>(null);
    const [isWished, setIsWished] = useState<boolean>(false);
    const [wishlistLoading, setWishlistLoading] = useState<boolean>(false);

    useEffect(() => {
        if (!id) return;

        const productId = Number(id);
        fetchProductDetail(productId)
            .then(setProduct)
            .catch(() => setError('상품을 불러오지 못했습니다.'));

        fetchRelatedProducts(productId).then(setRelated);
    }, [id]);

    // ✅ 리뷰 API 호출
    useEffect(() => {
        if (product && typeof product.sellerId === 'number') {
            fetchReviewsBySeller(product.sellerId).then((res) => {
                console.log('✅ 리뷰 응답:', res);
                setReviews(res); // ✅ .reviews 아님
            });
        }
    }, [product]);

    const handleToggleWishlist = async () => {
        if (!product || wishlistLoading) return;
        setWishlistLoading(true);
        try {
            const result = await toggleWishlist({ productId: product.id });
            setIsWished(result);
            alert(result ? '찜 목록에 추가되었습니다.' : '찜 목록에서 제거되었습니다.');
        } catch (err) {
            console.error('찜 처리 실패:', err);
            alert('찜 처리 중 오류가 발생했습니다.');
        } finally {
            setWishlistLoading(false);
        }
    };

    const handleAddToCart = async () => {
        if (!product) return;
        try {
            await addToCart({ productId: product.id, quantity: 1 });
            alert('장바구니에 추가되었습니다.');
        } catch {
            alert('장바구니 추가 실패');
        }
    };

    if (error) return <div className="text-red-500">{error}</div>;
    if (!product) return <div>로딩 중...</div>;

    return (
        <div>
            <Navbar />

            <div className="max-w-4xl mx-auto px-6 py-10">
                <h1 className="text-2xl font-bold mb-2">{product.name}</h1>

                <p className="text-green-600 font-semibold mb-2">
                    {product.price.toLocaleString()}원
                </p>

                <button
                    onClick={handleToggleWishlist}
                    className="text-2xl mb-4"
                    aria-label="찜하기 버튼"
                    disabled={wishlistLoading}
                >
                    {isWished ? '❤️' : '🤍'}
                </button>

                <button
                    onClick={handleAddToCart}
                    className="ml-4 px-4 py-2 bg-yellow-500 text-white rounded hover:bg-yellow-600"
                >
                    🛒 장바구니 담기
                </button>

                <img
                    src={product.imageThumbnailUrl}
                    alt={product.name}
                    className="w-full h-64 object-cover mb-6 rounded"
                />

                <p className="text-gray-700 whitespace-pre-line mb-4">{product.description}</p>

                <div className="text-sm text-gray-600 mb-6">
                    <p>재고: {product.stock}개</p>
                    <p>상태: {product.status}</p>
                    {product.width && product.depth && product.height && (
                        <p>
                            사이즈: {product.width} x {product.depth} x {product.height} cm
                        </p>
                    )}
                    {product.categoryName && <p>카테고리: {product.categoryName}</p>}
                    {product.seller && <p>판매자: {product.seller}</p>}
                </div>

                {/* ✅ 리뷰 리스트 */}
                <div className="mt-10 border-t pt-6">
                    <h2 className="text-lg font-semibold mb-4">판매자 리뷰</h2>
                    <ReviewList reviews={reviews} />
                </div>

                {/* ✅ 관련 상품 */}
                {related.length > 0 && (
                    <div className="mt-10 border-t pt-6">
                        <h2 className="text-lg font-semibold mb-4">이런 상품은 어떠세요?</h2>
                        <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
                            {related.map((item) => (
                                <div
                                    key={item.id}
                                    className="border rounded p-2 hover:shadow cursor-pointer"
                                    onClick={() => (location.href = `/main/products/${item.id}`)}
                                >
                                    <img
                                        src={item.imageThumbnailUrl || '/default-thumbnail.png'}
                                        alt={item.name}
                                        className="w-full h-32 object-cover rounded"
                                    />
                                    <p className="mt-2 font-medium text-sm truncate">{item.name}</p>
                                    <p className="text-green-600 font-semibold text-sm">
                                        {item.price.toLocaleString()}원
                                    </p>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>
        </div>
    );
}
