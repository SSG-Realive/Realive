'use client';

import { useEffect, useState, useRef, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import { useCartStore } from '@/store/customer/useCartStore';
import { fetchMyProfile } from '@/service/customer/customerService';
import { loadPaymentWidget, PaymentWidgetInstance } from '@tosspayments/payment-widget-sdk';
import type { CartItem } from '@/types/customer/cart/cart';
import type { MemberReadDTO } from '@/types/customer/member/member';

import './OrderPage.css';

// 클라이언트 키는 .env.local 파일에 NEXT_PUBLIC_TOSS_CLIENT_KEY=... 와 같이 저장되어 있어야 합니다.
const TOSS_CLIENT_KEY = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY as string;

export default function NewOrderPage() {
    const router = useRouter();
    const { id: customerId, userName: initialName } = useAuthStore();
    const cartItems = useCartStore((state) => state.itemsForCheckout);

    const [userProfile, setUserProfile] = useState<MemberReadDTO | null>(null);
    const [shippingInfo, setShippingInfo] = useState({ receiverName: '', phone: '', address: '' });
    const paymentWidgetRef = useRef<PaymentWidgetInstance | null>(null);

    // 사용자 정보 로딩 및 장바구니 유효성 검사
    useEffect(() => {
        if (cartItems.length === 0) {
            alert("결제할 상품 정보가 없습니다. 장바구니 페이지로 돌아갑니다.");
            router.replace('/customer/cart'); // 장바구니 페이지 경로로 수정
            return;
        }

        fetchMyProfile().then(profile => {
            setUserProfile(profile);
            setShippingInfo({
                receiverName: profile.name || '',
                phone: profile.phone || '',
                address: profile.address || '',
            });
        }).catch(err => console.error("사용자 정보 조회 실패:", err));
    }, [cartItems, router]);

    // 최종 결제 금액 계산
    const { totalProductPrice, deliveryFee, finalAmount } = useMemo(() => {
        if (cartItems.length === 0) {
            return { totalProductPrice: 0, deliveryFee: 0, finalAmount: 0 };
        }
        const totalProductPrice = cartItems.reduce((sum, item) => sum + item.productPrice * item.quantity, 0);
        const deliveryFee = 3000; // TODO: 실제 배송비 정책 적용
        const finalAmount = totalProductPrice + deliveryFee;
        return { totalProductPrice, deliveryFee, finalAmount };
    }, [cartItems]);

    // 토스페이먼츠 위젯 렌더링
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
            }
        };
        initializeWidget();
    }, [userProfile, customerId, finalAmount]);

    // 배송지 정보 변경 핸들러
    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setShippingInfo(prev => ({ ...prev, [name]: value }));
    };

    // 결제하기 버튼 핸들러
    const handlePayment = async () => {
        const paymentWidget = paymentWidgetRef.current;
        if (!paymentWidget || !userProfile) {
            alert("결제 위젯이 준비되지 않았습니다. 잠시 후 다시 시도해주세요.");
            return;
        }

        const checkoutInfo = {
            orderItems: cartItems.map(item => ({ productId: item.productId, quantity: item.quantity })),
            shippingInfo: shippingInfo,
        };
        sessionStorage.setItem('checkout_info', JSON.stringify(checkoutInfo));
        
        const orderName = cartItems.length > 1
            ? `${cartItems[0].productName} 외 ${cartItems.length - 1}건`
            : cartItems[0].productName;

        try {
            await paymentWidget.requestPayment({
                orderId: `order_${new Date().getTime()}`,
                orderName: orderName,
                customerName: userProfile.name || initialName || '고객',
                successUrl: `${window.location.origin}/customer/orders/success`,
                failUrl: `${window.location.origin}/customer/orders/fail`,
            });
        } catch (error) {
            console.error("결제 요청 실패:", error);
            alert("결제에 실패했습니다. 다시 시도해주세요.");
        }
    };

    if (!userProfile) {
        return <div className="loading-container">주문 정보를 준비 중입니다...</div>;
    }

    return (
        <main className="order-page-container">
            <h1 className="page-title">주문 / 결제</h1>

            <section className="order-section">
                <h2>주문 상품</h2>
                {cartItems.map(item => (
                    <div key={item.cartItemId} className="product-summary-card">
                        <img src={item.imageThumbnailUrl || '/default-image.png'} alt={item.productName} />
                        <div className="product-details">
                            <p>{item.productName}</p>
                            <p>수량: {item.quantity}개</p>
                        </div>
                        <p className="product-price">{(item.productPrice * item.quantity).toLocaleString()}원</p>
                    </div>
                ))}
            </section>
            
            <section className="order-section"><h2>주문자 정보</h2>{/* ... */} </section>
            <section className="order-section"><h2>배송지 정보</h2>{/*... */}</section>

            <section className="order-section">
                <h2>결제 수단</h2>
                <div id="payment-widget" style={{ width: '100%' }} />
            </section>
            
            <aside className="order-summary-box">
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
                <button className="payment-button" onClick={handlePayment} disabled={cartItems.length === 0}>
                    {finalAmount.toLocaleString()}원 결제하기
                </button>
            </aside>
        </main>
    );
}