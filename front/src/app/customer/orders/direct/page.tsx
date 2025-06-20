'use client';

import { useEffect, useState } from 'react';
import { useSearchParams } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import { fetchMyProfile } from '@/service/customer/customerService'; // 내 정보 조회 API
import { getDirectPaymentInfo } from '@/service/order/orderService'; // 상품 정보 조회 API
import './DirectOrderPage.css'; // 이 페이지를 위한 CSS 파일

// 주문 정보 타입 (실제 타입 정의 파일에 맞게 수정)
interface OrderProductInfo {
    productName: string;
    quantity: number;
    price: number;
    imageUrl: string;
}

interface UserProfile {
    name: string;
    email: string;
    phone: string;
    address: string;
}

export default function DirectOrderPage() {
    const searchParams = useSearchParams();
    const productId = searchParams.get('productId');
    const quantity = Number(searchParams.get('quantity'));

    // --- 상태 관리 ---
    const [productInfo, setProductInfo] = useState<OrderProductInfo | null>(null);
    const [userProfile, setUserProfile] = useState<UserProfile | null>(null);

    // 배송지 폼 상태
    const [shippingInfo, setShippingInfo] = useState({
        receiverName: '',
        phone: '',
        address: '',
    });
    
    // 결제 수단 상태
    const [paymentMethod, setPaymentMethod] = useState('CARD'); // 기본값 '카드'

    // --- 데이터 로딩 ---
    useEffect(() => {
        // 1. 상품 정보 불러오기
        if (productId && quantity) {
            getDirectPaymentInfo(Number(productId), quantity)
                .then(setProductInfo)
                .catch(err => console.error("상품 정보 조회 실패:", err));
        }

        // 2. 내 정보 불러오기 (배송지 기본값으로 사용)
        fetchMyProfile()
            .then(profile => {
                setUserProfile(profile);
                // 불러온 내 정보로 배송지 폼 초기값 설정
                setShippingInfo({
                    receiverName: profile.name,
                    phone: profile.phone,
                    address: profile.address,
                });
            })
            .catch(err => console.error("내 정보 조회 실패:", err));
    }, [productId, quantity]);

    // --- 이벤트 핸들러 ---
    const handleShippingInfoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setShippingInfo(prev => ({ ...prev, [name]: value }));
    };

    const handlePayment = async () => {
        // TODO: 이전 답변에서 설명한 토스페이먼츠 `requestPayment` 호출 로직 구현
        alert(`
            결제 요청!
            받는 사람: ${shippingInfo.receiverName}
            연락처: ${shippingInfo.phone}
            주소: ${shippingInfo.address}
            결제수단: ${paymentMethod}
            상품: ${productInfo?.productName}
        `);
    };

    if (!productInfo || !userProfile) {
        return <div className="loading-container">주문 정보를 불러오는 중입니다...</div>;
    }

    // --- UI 렌더링 ---
    return (
        <div className="order-page-container">
            <h1 className="page-title">주문/결제</h1>

            {/* 1. 주문 상품 정보 */}
            <section className="order-section">
                <h2>주문 상품</h2>
                <div className="product-summary-card">
                    <img src={productInfo.imageUrl || '/default-image.png'} alt={productInfo.productName} />
                    <div className="product-details">
                        <p>{productInfo.productName}</p>
                        <p>수량: {productInfo.quantity}개</p>
                    </div>
                    <p className="product-price">{(productInfo.price * productInfo.quantity).toLocaleString()}원</p>
                </div>
            </section>

            {/* 2. 주문자 정보 */}
            <section className="order-section">
                <h2>주문자 정보</h2>
                <div className="info-box">
                    <p><strong>이름:</strong> {userProfile.name}</p>
                    <p><strong>이메일:</strong> {userProfile.email}</p>
                </div>
            </section>

            {/* 3. 배송 정보 입력 */}
            <section className="order-section">
                <h2>배송지 정보</h2>
                <div className="shipping-form">
                    <label>받는 사람</label>
                    <input name="receiverName" value={shippingInfo.receiverName} onChange={handleShippingInfoChange} />

                    <label>연락처</label>
                    <input name="phone" type="tel" value={shippingInfo.phone} onChange={handleShippingInfoChange} />
                    
                    <label>주소</label>
                    <input name="address" value={shippingInfo.address} onChange={handleShippingInfoChange} />
                    {/* TODO: 주소 검색 버튼 및 기능 추가 */}
                </div>
            </section>

            {/* 4. 결제 수단 선택 */}
            <section className="order-section">
                <h2>결제 수단</h2>
                <div className="payment-selector">
                    <label className={paymentMethod === 'CARD' ? 'active' : ''}>
                        <input type="radio" name="paymentMethod" value="CARD" checked={paymentMethod === 'CARD'} onChange={(e) => setPaymentMethod(e.target.value)} />
                        신용/체크카드
                    </label>
                    {/* 다른 결제 수단 추가 */}
                </div>
            </section>

            {/* 5. 최종 결제 금액 요약 */}
            <section className="order-summary">
                <h3>결제 금액</h3>
                <div className="summary-row">
                    <span>총 상품금액</span>
                    <span>{(productInfo.price * productInfo.quantity).toLocaleString()}원</span>
                </div>
                <div className="summary-row">
                    <span>배송비</span>
                    <span>+ 3,000원</span> {/* TODO: 배송비 정책에 따라 동적 계산 */}
                </div>
                <div className="summary-row total">
                    <span>최종 결제 금액</span>
                    <span>{(productInfo.price * productInfo.quantity + 3000).toLocaleString()}원</span>
                </div>
            </section>
            
            <button className="payment-button" onClick={handlePayment}>
                결제하기
            </button>
        </div>
    );
}