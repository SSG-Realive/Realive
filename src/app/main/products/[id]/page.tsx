'use client';

import { useEffect, useState, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation'; // useRouter 추가
import Navbar from '@/components/customer/common/Navbar';
import { fetchProductDetail, fetchRelatedProducts } from '@/service/customer/productService';
import { toggleWishlist } from '@/service/customer/wishlistService';
import { addToCart } from '@/service/customer/cartService';
import { fetchReviewsBySeller } from '@/service/customer/reviewService';
import ReviewList from '@/components/customer/review/ReviewList';
import { ProductDetail, ProductListDTO } from '@/types/seller/product/product';
import { ReviewResponseDTO } from '@/types/customer/review/review';

// ==================================================================
// 메인 페이지 컴포넌트
// ==================================================================
export default function ProductDetailPage() {
    const { id } = useParams(); // URL 파라미터에서 상품 ID를 가져옵니다.
    const router = useRouter(); // useRouter 훅 초기화

    const [product, setProduct] = useState<ProductDetail | null>(null); // 상품 상세 정보를 저장하는 상태
    const [related, setRelated] = useState<ProductListDTO[]>([]); // 관련 상품 리스트를 저장하는 상태
    const [reviews, setReviews] = useState<ReviewResponseDTO[]>([]); // 판매자 리뷰 리스트를 저장하는 상태
    const [error, setError] = useState<string | null>(null); // 에러 메시지를 저장하는 상태
    const [isWished, setIsWished] = useState<boolean>(false); // 찜하기 상태를 저장하는 상태
    const [wishlistLoading, setWishlistLoading] = useState<boolean>(false); // 찜하기 처리 중 로딩 상태
    const [quantity, setQuantity] = useState<number>(1); // 구매 수량을 저장하는 상태

    // 스크롤 감지를 위한 ref와 sticky(고정) 상태
    const [isFooterSticky, setIsFooterSticky] = useState(false);
    const triggerRef = useRef<HTMLDivElement>(null); // 푸터가 화면 상단에 고정될 시점을 감지할 요소

    // 데이터 로딩 로직
    useEffect(() => {
        if (!id) return; // ID가 없으면 함수를 종료합니다.

        const productId = Number(id); // ID를 숫자로 변환합니다.

        // 상품 상세 정보 불러오기
        fetchProductDetail(productId)
            .then(data => {
                setProduct(data); // 상품 상세 정보를 설정합니다.
            })
            .catch(() => setError('상품을 불러오지 못했습니다.')); // 에러 발생 시 에러 메시지를 설정합니다.

        // 관련 상품 불러오기 (fetchRelatedProducts가 동일 카테고리 상품을 반환한다고 가정)
        fetchRelatedProducts(productId)
            .then(setRelated)
            .catch(() => console.error('관련 상품을 불러오지 못했습니다.'));

        // 초기 찜 상태를 확인하는 API 호출이 필요할 수 있습니다.
        // 현재 코드에는 없으므로, 찜 버튼 클릭 시 토글만 수행합니다.
        // 예를 들어, fetchUserWishlist()와 같은 함수로 사용자의 찜 목록을 가져와 현재 상품이 있는지 확인해야 합니다.

    }, [id]); // ID가 변경될 때마다 실행됩니다.


    // 상품 정보가 로드되면 판매자 리뷰를 가져옵니다.
    useEffect(() => {
        if (product && typeof product.sellerId === 'number') {
            fetchReviewsBySeller(product.sellerId).then(setReviews);
        }
    }, [product]); // product 상태가 변경될 때마다 실행됩니다.

    // 스크롤 이벤트 리스너 설정
    useEffect(() => {
        const handleScroll = () => {
            if (triggerRef.current) {
                const { top } = triggerRef.current.getBoundingClientRect();
                // 트리거 요소가 화면 상단에 닿았을 때 푸터를 sticky 상태로 변경합니다.
                if (top <= 0) {
                    setIsFooterSticky(true);
                } else {
                    setIsFooterSticky(false);
                }
            }
        };

        window.addEventListener('scroll', handleScroll); // 스크롤 이벤트 리스너를 추가합니다.
        // 컴포넌트 언마운트 시 이벤트 리스너를 제거합니다.
        return () => window.removeEventListener('scroll', handleScroll);
    }, []); // 마운트 시 한 번만 실행됩니다.

    // ✅ "바로 주문" 핸들러 함수 추가
    const handleOrderNow = async () => {
        if (!product) return;
        try {
            await addToCart({ productId: product.id, quantity: 1 });
            router.push('/customer/cart'); // 장바구니 페이지로 이동
        } catch (error) {
            console.error('주문 처리 중 오류 발생:', error);
            alert('주문 처리 중 오류가 발생했습니다.');
        }
    };

    // 찜하기/찜 취소 처리 함수
    const handleToggleWishlist = async () => {
        if (!product || wishlistLoading) return; // 상품 정보가 없거나 로딩 중이면 종료합니다.
        setWishlistLoading(true); // 로딩 상태를 true로 설정합니다.
        try {
            // toggleWishlist 함수가 찜 상태를 반환하도록 수정되었다고 가정합니다.
            // 또는 toggleWishlist 내부에서 찜 성공/실패 여부를 판단하여 isWished를 업데이트합니다.
            // 현재는 토글 후 isWished 상태를 반전시키는 방식으로 시뮬레이션합니다.
            await toggleWishlist({ productId: product.id });
            setIsWished(prev => !prev); // 찜 상태를 토글
            alert(isWished ? '찜 목록에서 제거되었습니다.' : '찜 목록에 추가되었습니다.'); // 사용자에게 알림
        } catch (err: any) { // 에러 타입 명시
            console.error('찜 처리 실패:', err);
            if (err.response && err.response.status === 403) {
                alert('로그인이 필요하거나, 찜 기능을 사용할 권한이 없습니다.');
                // 로그인 페이지로 리디렉션
                router.push('/login'); // 예시 경로, 실제 로그인 경로로 변경 필요
            } else {
                alert('찜 처리 중 오류가 발생했습니다.');
            }
        } finally {
            setWishlistLoading(false); // 로딩 상태를 false로 설정합니다.
        }
    };

    // 장바구니에 상품 추가 처리 함수
    const handleAddToCart = async () => {
        if (!product || quantity <= 0) {
            alert('수량을 선택해주세요.');
            return;
        }
        try {
            await addToCart({ productId: product.id, quantity: quantity }); // 장바구니 추가 API 호출
            alert('장바구니에 추가되었습니다.'); // 사용자에게 알림
            // **중요: DB에 추가가 안된다면, src/service/customer/cartService.ts의 addToCart 함수 구현과
            // 백엔드의 장바구니 추가 API 엔드포인트를 확인해야 합니다.**
        } catch (error) {
            console.error('장바구니 추가 실패:', error);
            alert('장바구니 추가 실패');
        }
    };

    // 바로 구매 처리 함수 (구매 페이지로 리다이렉트)
    const handleBuyNow = () => {
        if (!product || quantity <= 0) {
            alert('수량을 선택해주세요.');
            return;
        }
        // 구매하기 페이지로 리다이렉트 (Next.js의 router.push 사용)
        router.push(`/customer/order/direct?productId=${product.id}&quantity=${quantity}`);
    };

    // 에러 상태일 경우 에러 메시지를 표시합니다.
    if (error) return <div className="text-red-500">{error}</div>;
    // 상품 정보가 로딩 중일 경우 로딩 메시지를 표시합니다.
    if (!product) return <div>로딩 중...</div>;

    // 총 상품 금액을 계산하여 표시 형식에 맞게 변환합니다.
    const totalDisplayPrice = (product.price * quantity).toLocaleString();

    return (
        <div>
            <Navbar /> {/* 상단 내비게이션 바 */}

            {/* ===== 상단 상품 정보 영역 ===== */}
            <div className="max-w-6xl mx-auto px-4 py-10 grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* 왼쪽 컬럼: 상품 이미지 */}
                <div>
                    <img
                        src={product.imageThumbnailUrl || '/default-thumbnail.png'} // 썸네일 이미지를 표시합니다.
                        alt={product.name}
                        className="w-full h-96 object-contain rounded-lg shadow-md border"
                    />
                </div>

                {/* 오른쪽 컬럼: 상품 상세 정보 및 구매 옵션 */}
                <div>
                    <h1 className="text-3xl font-bold mb-2">{product.name}</h1> {/* 상품명 */}
                    <p className="text-xl text-gray-700 mb-4">{product.description}</p> {/* 상품 설명 */}
                    <p className="text-3xl font-extrabold text-red-600 mb-6">{product.price.toLocaleString()}원</p> {/* 상품 가격 */}

                    <div className="mb-6 space-y-2 text-gray-700">
                        <p><span className="font-semibold">상품상태:</span> {product.status}</p> {/* 상품 상태 */}
                        <p><span className="font-semibold">재고:</span> {product.stock}개</p> {/* 재고 수량 */}
                        {product.width && product.depth && product.height && (
                            <p><span className="font-semibold">사이즈:</span> {product.width} x {product.depth} x {product.height} cm</p>
                        )} {/* 상품 사이즈 */}
                        {product.categoryName && <p><span className="font-semibold">카테고리:</span> {product.categoryName}</p>} {/* 카테고리명 */}
                        {product.sellerName && <p><span className="font-semibold">판매자:</span> {product.sellerName}</p>} {/* 판매자명 */}
                    </div>

                    {/* 구매 옵션 */}
                    <div className="border-t border-b py-6 mb-8">
                        <div className="flex items-center justify-between mb-4">
                            <span className="font-semibold text-lg">수량</span>
                            <div className="flex items-center border rounded-md">
                                <button
                                    onClick={() => setQuantity(prev => Math.max(1, prev - 1))} // 수량 감소 버튼
                                    className="px-3 py-1 text-lg font-bold text-gray-600 hover:bg-gray-100 rounded-l-md"
                                >
                                    -
                                </button>
                                <input
                                    type="text" // type="text"로 변경하여 브라우저 기본 화살표 제거
                                    inputMode="numeric" // 숫자 키패드 유도
                                    pattern="[0-9]*" // 숫자만 입력되도록 패턴 지정
                                    value={quantity}
                                    onChange={(e) => {
                                        const value = e.target.value;
                                        // 숫자가 아니거나 빈 문자열인 경우 처리
                                        if (value === '' || /^\d+$/.test(value)) {
                                            const numValue = Number(value);
                                            // 재고 수량 초과 및 1 미만 방지
                                            if (numValue >= 1 && numValue <= product.stock) {
                                                setQuantity(numValue);
                                            } else if (value === '') { // 입력 필드가 비워질 때
                                                setQuantity(1); // 기본 1로 설정
                                            } else if (numValue < 1) {
                                                setQuantity(1); // 1 미만이면 1로 설정
                                            } else if (numValue > product.stock) {
                                                setQuantity(product.stock); // 재고 초과 시 재고만큼만 설정
                                            }
                                        }
                                    }}
                                    onBlur={() => { // 필드를 벗어날 때 유효성 다시 검사 (선택 사항)
                                        if (quantity < 1) setQuantity(1);
                                        if (quantity > product.stock) setQuantity(product.stock);
                                    }}
                                    className="w-16 text-center border-l border-r py-1 focus:outline-none"
                                />
                                <button
                                    onClick={() => setQuantity(prev => Math.min(product.stock, prev + 1))} // 수량 증가 버튼 (재고 이상 증가 방지)
                                    className="px-3 py-1 text-lg font-bold text-gray-600 hover:bg-gray-100 rounded-r-md"
                                >
                                    +
                                </button>
                            </div>
                        </div>
                        <div className="flex items-center justify-between text-xl font-bold mb-6">
                            <span>총 상품금액</span>
                            <span className="text-red-600">{totalDisplayPrice}원</span> {/* 총 상품 금액 */}
                        </div>
                        <div className="flex space-x-3">
                            <button
                                onClick={handleToggleWishlist} // 찜하기/찜 취소 버튼
                                // 찜 상태일 때의 색상을 gray-700으로 변경
                                className={`flex-1 px-5 py-3 rounded-lg border text-lg ${isWished ? 'bg-gray-700 text-white border-gray-700' : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'}`}
                                aria-label="찜하기 버튼"
                                disabled={wishlistLoading}
                            >
                                {isWished ? '❤️ 찜 취소' : '🤍 찜하기'}
                            </button>
                            <button
                                onClick={handleAddToCart} // 장바구니 담기 버튼
                                className="flex-1 px-5 py-3 bg-blue-600 text-white text-lg font-semibold rounded-lg hover:bg-blue-700 transition-colors"
                            >
                                🛒 장바구니 담기
                            </button>
                            <button
                                onClick={handleBuyNow} // 바로 구매 버튼
                                className="flex-1 px-5 py-3 bg-green-600 text-white text-lg font-semibold rounded-lg hover:bg-green-700 transition-colors"
                            >
                                바로 구매
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* ===== 리뷰 및 관련 상품 영역 ===== */}
            <div className="max-w-6xl mx-auto px-4">
                <div className="mt-10 border-t pt-6">
                    <h2 className="text-2xl font-bold mb-4">판매자 리뷰</h2>
                    {reviews.length > 0 ? (
                        <ReviewList reviews={reviews} /> // 리뷰가 있을 경우 리뷰 목록 컴포넌트 표시
                    ) : (
                        <p className="text-gray-600">아직 등록된 리뷰가 없습니다.</p> // 리뷰가 없을 경우 메시지 표시
                    )}
                </div>
                {related.length > 0 && ( // 관련 상품이 있을 경우에만 표시
                    <div className="mt-16 border-t pt-6">
                        <h2 className="text-2xl font-bold mb-6">이런 상품은 어떠세요?</h2>
                        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-6">
                            {related.map((item) => (
                                <div
                                    key={item.id}
                                    className="border rounded-lg overflow-hidden shadow-sm hover:shadow-md transition-shadow cursor-pointer"
                                    onClick={() => router.push(`/main/products/${item.id}`)} // 관련 상품 클릭 시 해당 상품 페이지로 이동
                                >
                                    <img
                                        src={item.imageThumbnailUrl || '/default-thumbnail.png'}
                                        alt={item.name}
                                        className="w-full h-40 object-cover"
                                    />
                                    <div className="p-3">
                                        <p className="mt-2 font-medium text-base truncate">{item.name}</p>
                                        <p className="text-lg font-bold text-red-600 mt-1">{item.price.toLocaleString()}원</p>
                                    </div>
                                </div>
                            ))}
                        </div>
                    </div>
                )}
            </div>

            {/* ===== 하단 상세정보 및 푸터 영역 ===== */}
            <ProductDetailFooter product={product} isSticky={isFooterSticky} triggerRef={triggerRef} />

            <div
                className={`
                    fixed bottom-0 left-0 right-0 bg-white border-t p-4 shadow-lg-top 
                    transform transition-transform duration-300 ease-in-out
                    ${isFooterSticky ? 'translate-y-0' : 'translate-y-full'} 
                    z-20
                `}
            >
                <div className="max-w-4xl mx-auto flex justify-between items-center">
                    <div>
                        <p className="text-sm font-semibold truncate">{product.name}</p>
                        <p className="text-lg font-bold text-green-600">{product.price.toLocaleString()}원</p>
                    </div>
                    <button
                        onClick={handleOrderNow}
                        className="bg-red-500 text-white font-bold px-8 py-3 rounded-md hover:bg-red-600 transition-colors"
                    >
                        바로 주문
                    </button>
                </div>
            </div>
        </div>
    );
}



// ==================================================================
// 페이지 하단에 함께 정의하는 푸터 컴포넌트 (상세정보, 리뷰, Q&A, 반품/교환 정보 등)
// ==================================================================
interface FooterProps {
    product: ProductDetail; // 상품 상세 정보
    isSticky: boolean; // 푸터가 화면 상단에 고정될지 여부
    triggerRef: React.LegacyRef<HTMLDivElement>; // 스크롤 위치 감지 트리거 ref
}

function ProductDetailFooter({ product, isSticky, triggerRef }: FooterProps) {
    const [activeTab, setActiveTab] = useState('상세정보'); // 현재 활성화된 탭
    // 탭 메뉴 항목들
    const tabs = [{ label: '상세정보' }, { label: '리뷰' }, { label: 'Q&A' }, { label: '반품/교환정보' }];

    return (
        <footer className="mt-20 bg-gray-50 border-t">
            {/* 스크롤 위치를 감지할 트리거 요소 */}
            <div ref={triggerRef}></div>
            <div className="max-w-6xl mx-auto">
                {/* isSticky prop에 따라 상단에 고정되는 내비게이션 바 */}
                <nav className={`flex text-base font-medium border-y ${isSticky ? 'sticky top-0 bg-white shadow-md z-10' : ''}`}>
                    {tabs.map(({ label }) => (
                        <button
                            key={label}
                            onClick={() => setActiveTab(label)} // 탭 클릭 시 활성 탭 변경
                            className={`flex-1 px-4 py-4 ${activeTab === label ? 'text-blue-600 border-b-2 border-blue-600' : 'text-gray-600 hover:text-blue-500'}`}
                        >
                            {label}
                        </button>
                    ))}
                </nav>

                {/* 선택된 탭에 따라 다른 내용을 보여줄 수 있습니다 */}
                <div className="px-4 py-8">
                    {activeTab === '상세정보' && ( // '상세정보' 탭 내용
                        <table className="w-full text-base border-collapse border border-gray-200 bg-white">
                            <tbody>
                            <tr className="border-b border-gray-200">
                                <td className="py-3 px-4 bg-gray-100 font-semibold w-40">상품번호</td>
                                <td className="py-3 px-4 text-gray-800">{product.id}</td>
                                <td className="py-3 px-4 bg-gray-100 font-semibold w-40">상품상태</td>
                                <td className="py-3 px-4 text-gray-800">{product.status}</td>
                            </tr>
                            <tr className="border-b border-gray-200">
                                <td className="py-3 px-4 bg-gray-100 font-semibold">판매자</td>
                                <td className="py-3 px-4 text-gray-800">{product.sellerName}</td>
                            </tr>
                            <tr className="border-b border-gray-200">
                                <td className="py-3 px-4 bg-gray-100 font-semibold">사이즈</td>
                                <td colSpan={3} className="py-3 px-4 text-gray-800">
                                    {product.width && product.depth && product.height
                                        ? `${product.width} x ${product.depth} x ${product.height} cm`
                                        : '정보 없음'}
                                </td>
                            </tr>
                            <tr className="border-b border-gray-200">
                                <td className="py-3 px-4 bg-gray-100 font-semibold">카테고리</td>
                                {/* ProductDetail 타입의 categoryName이 선택적 필드이므로 없을 경우 '정보 없음' 표시 */}
                                <td colSpan={3} className="py-3 px-4 text-gray-800">{product.categoryName || '정보 없음'}</td>
                            </tr>
                            <tr className="border-b border-gray-200">
                                <td className="py-3 px-4 bg-gray-100 font-semibold">재고 수량</td>
                                <td colSpan={3} className="py-3 px-4 text-gray-800">{product.stock}개</td>
                            </tr>
                            </tbody>
                        </table>
                    )}
                    {activeTab === '리뷰' && ( // '리뷰' 탭 내용 (현재는 플레이스홀더)
                        <div className="py-8 text-center text-gray-600">
                            {/* 여기에 전체 리뷰 리스트를 표시할 수 있습니다. */}
                            <p>리뷰 콘텐츠가 여기에 표시됩니다.</p>
                            {/* ReviewList 컴포넌트가 ProductDetailPage에서 이미 사용되고 있으므로, 여기서는 단순히 플레이스홀더입니다. */}
                        </div>
                    )}
                    {activeTab === 'Q&A' && ( // 'Q&A' 탭 내용 (현재는 플레이스홀더)
                        <div className="py-8 text-center text-gray-600">
                            Q&A 콘텐츠가 여기에 표시됩니다.
                        </div>
                    )}
                    {activeTab === '반품/교환정보' && ( // '반품/교환정보' 탭 내용 (현재는 플레이스홀더)
                        <div className="py-8 text-center text-gray-600">
                            반품/교환 정보가 여기에 표시됩니다.
                        </div>
                    )}
                </div>

                <div className="py-20 text-center text-gray-400 h-[3000px] bg-gray-100">
                    상품 상세 설명 영역 (e.g. 긴 이미지)
                    {/* 일반적으로 긴 상품 상세 설명 이미지나 추가적인 텍스트가 여기에 들어갑니다. */}
                </div>

                <div className="py-8 text-xs text-gray-500 text-center border-t border-gray-200">
                    © 2025 Realive Inc. All rights reserved.
                </div>
            </div>
        </footer>
    );
}