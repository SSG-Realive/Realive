'use client';

import { useEffect, useState, useRef, useMemo } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import { fetchMyProfile } from '@/service/customer/customerService';
import { getDirectPaymentInfo } from '@/service/order/orderService';
import { loadPaymentWidget, PaymentWidgetInstance } from '@tosspayments/payment-widget-sdk';
import type { DirectPaymentInfoDTO } from '@/types/customer/order/order';
import type { MemberReadDTO } from '@/types/customer/member/member';
import './DirectOrderPage.css';

// 토스페이먼츠 클라이언트 키
const TOSS_CLIENT_KEY = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY as string;

export default function DirectOrderPage() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const { id: customerId, userName: initialName } = useAuthStore();
    
    const productId = searchParams.get('productId');
    const quantity = Number(searchParams.get('quantity'));

    // --- 상태 관리 ---
    const [productInfo, setProductInfo] = useState<DirectPaymentInfoDTO | null>(null);
    const [userProfile, setUserProfile] = useState<MemberReadDTO | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const paymentWidgetRef = useRef<PaymentWidgetInstance | null>(null);

    // 배송지 폼 상태
    const [shippingInfo, setShippingInfo] = useState({
        receiverName: '',
        phone: '',
        address: '',
    });

    // --- 최종 결제 금액 계산 ---
    const { totalProductPrice, deliveryFee, finalAmount } = useMemo(() => {
        if (!productInfo) {
            return { totalProductPrice: 0, deliveryFee: 0, finalAmount: 0 };
        }
        const totalProductPrice = productInfo.price * productInfo.quantity;
        const deliveryFee = 3000; // TODO: 실제 배송비 정책 적용
        const finalAmount = totalProductPrice + deliveryFee;
        return { totalProductPrice, deliveryFee, finalAmount };
    }, [productInfo]);

    // --- 데이터 로딩 ---
    useEffect(() => {
        const loadData = async () => {
            try {
                setLoading(true);
                setError(null);

                // 1. 상품 정보 불러오기
                if (productId && quantity) {
                    const productData = await getDirectPaymentInfo(Number(productId), quantity);
                    setProductInfo(productData);
                } else {
                    throw new Error('상품 정보가 올바르지 않습니다.');
                }

                // 2. 내 정보 불러오기
                const profileData = await fetchMyProfile();
                setUserProfile(profileData);
                
                // 배송지 폼 초기값 설정
                setShippingInfo({
                    receiverName: profileData.name || '',
                    phone: profileData.phone || '',
                    address: profileData.address || '',
                });
            } catch (err) {
                console.error('데이터 로딩 실패:', err);
                setError(err instanceof Error ? err.message : '데이터를 불러오는데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        loadData();
    }, [productId, quantity]);

    // --- 토스페이먼츠 위젯 렌더링 ---
    useEffect(() => {
        if (!userProfile || !customerId || finalAmount === 0) return;

        const initializeWidget = async () => {
            try {
                const tossPayments = await loadPaymentWidget(TOSS_CLIENT_KEY, customerId.toString());
                
                tossPayments.renderPaymentMethods('#payment-widget', { value: finalAmount }, { variantKey: 'DEFAULT' });
                tossPayments.renderAgreement('#agreement', { variantKey: 'DEFAULT' });

                paymentWidgetRef.current = tossPayments;
            } catch (error) {
                console.error("토스페이먼츠 위젯 렌더링 실패:", error);
                setError('결제 위젯을 불러오는데 실패했습니다.');
            }
        };

        initializeWidget();
    }, [userProfile, customerId, finalAmount]);

    // --- 이벤트 핸들러 ---
    const handleShippingInfoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setShippingInfo(prev => ({ ...prev, [name]: value }));
    };

    const handlePayment = async () => {
        const paymentWidget = paymentWidgetRef.current;
        if (!paymentWidget || !userProfile || !productInfo) {
            alert("결제 위젯이 준비되지 않았습니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        // 배송 정보 유효성 검사
        if (!shippingInfo.receiverName || !shippingInfo.phone || !shippingInfo.address) {
            alert('배송 정보를 모두 입력해주세요.');
            return;
        }

        try {
            // 결제 요청
            await paymentWidget.requestPayment({
                orderId: `direct_${Date.now()}`,
                orderName: productInfo.productName,
                customerName: userProfile.name || initialName || '고객',
                successUrl: `${window.location.origin}/customer/orders/success`,
                failUrl: `${window.location.origin}/customer/orders/fail`,
            });
        } catch (error) {
            console.error("결제 요청 실패:", error);
            alert("결제에 실패했습니다. 다시 시도해주세요.");
        }
    };

    // 로딩 상태
    if (loading) {
        return <div className="loading-container">주문 정보를 불러오는 중입니다...</div>;
    }

    // 에러 상태
    if (error) {
        return (
            <div className="order-page-container">
                <div className="error-container">
                    <h2>오류가 발생했습니다</h2>
                    <p>{error}</p>
                    <button onClick={() => router.back()}>이전 페이지로 돌아가기</button>
                </div>
            </div>
        );
    }

    // 데이터 없음
    if (!productInfo || !userProfile) {
        return (
            <div className="order-page-container">
                <div className="error-container">
                    <h2>주문 정보를 찾을 수 없습니다</h2>
                    <button onClick={() => router.back()}>이전 페이지로 돌아가기</button>
                </div>
            </div>
        );
    }

    return (
        <div className="order-page-container">
            <h1 className="page-title">주문/결제</h1>

            {/* 1. 주문 상품 정보 */}
            <section className="order-section">
                <h2>주문 상품</h2>
                <div className="product-summary-card">
                    <img src={productInfo.imageUrl || '/images/placeholder.png'} alt={productInfo.productName} />
                    <div className="product-details">
                        <p className="product-name">{productInfo.productName}</p>
                        <p className="product-quantity">수량: {productInfo.quantity}개</p>
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
                    <label>받는 사람 *</label>
                    <input 
                        name="receiverName" 
                        value={shippingInfo.receiverName} 
                        onChange={handleShippingInfoChange}
                        placeholder="받는 사람 이름을 입력하세요"
                        required
                    />

                    <label>연락처 *</label>
                    <input 
                        name="phone" 
                        type="tel" 
                        value={shippingInfo.phone} 
                        onChange={handleShippingInfoChange}
                        placeholder="010-0000-0000"
                        required
                    />
                    
                    <label>주소 *</label>
                    <input 
                        name="address" 
                        value={shippingInfo.address} 
                        onChange={handleShippingInfoChange}
                        placeholder="상세 주소를 입력하세요"
                        required
                    />
                </div>
            </section>

            {/* 4. 결제 수단 */}
            <section className="order-section">
                <h2>결제 수단</h2>
                <div id="payment-widget" style={{ width: '100%' }} />
            </section>

            {/* 5. 결제 동의 및 금액 요약 */}
            <aside className="order-summary">
                <div id="agreement" />
                <h3>결제 금액</h3>
                <div className="summary-row">
                    <span>총 상품금액</span>
                    <span>{totalProductPrice.toLocaleString()}원</span>
                </div>
                <div className="summary-row">
                    <span>배송비</span>
                    <span>+ {deliveryFee.toLocaleString()}원</span>
                </div>
                <div className="summary-row total">
                    <span>최종 결제 금액</span>
                    <span>{finalAmount.toLocaleString()}원</span>
                </div>
                <button 
                    className="payment-button" 
                    onClick={handlePayment}
                    disabled={!paymentWidgetRef.current}
                >
                    {finalAmount.toLocaleString()}원 결제하기
                </button>
            </aside>
        </div>
    );
}