// src/app/customer/orders/direct/page.tsx (DirectOrderPage)
'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { fetchMyProfile } from '@/service/customer/customerService';
import { getDirectPaymentInfo, processDirectPaymentApi } from '@/service/order/orderService';
import { DirectPaymentInfoDTO, PayRequestDTO } from '@/types/customer/order/order'; // DTOs 임포트
import './DirectOrderPage.css'; // 이 페이지를 위한 CSS 파일

// UserProfile 타입은 그대로 유지
interface UserProfile {
    name: string;
    email: string;
    phone: string;
    address: string;
}

export default function DirectOrderPage() {
    const router = useRouter();

    // --- 상태 관리 ---
    const [productInfo, setProductInfo] = useState<DirectPaymentInfoDTO | null>(null);
    const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [pageError, setPageError] = useState<string | null>(null);


    // 배송지 폼 상태
    const [shippingInfo, setShippingInfo] = useState({
        receiverName: '',
        phone: '',
        address: '', // PayRequestDTO의 deliveryAddress에 매핑될 값
    });

    // 결제 수단 상태 (PayRequestDTO의 유니온 타입에 맞춰 초기값 설정)
    const [paymentMethod, setPaymentMethod] = useState<'CARD' | 'CELL_PHONE' | 'ACCOUNT'>('CARD');

    // --- 데이터 로딩 ---
    useEffect(() => {
        const loadOrderData = async () => {
            setLoading(true);
            setPageError(null);

            const storedProductId = sessionStorage.getItem('directBuyProductId');
            const storedQuantity = sessionStorage.getItem('directBuyQuantity');

            if (!storedProductId || !storedQuantity) {
                setPageError('바로 구매할 상품 정보가 없습니다. 상품 상세 페이지에서 다시 시도해주세요.');
                setLoading(false);
                return;
            }

            const productIdNum = Number(storedProductId);
            const quantityNum = Number(storedQuantity);

            if (isNaN(productIdNum) || productIdNum <= 0 || isNaN(quantityNum) || quantityNum <= 0) {
                setPageError('잘못된 상품 정보 또는 수량입니다.');
                setLoading(false);
                return;
            }

            try {
                const productData = await getDirectPaymentInfo(productIdNum, quantityNum);
                setProductInfo(productData);

                const profileData = await fetchMyProfile();
                setUserProfile(profileData);

                setShippingInfo({
                    receiverName: profileData.name,
                    phone: profileData.phone,
                    address: profileData.address,
                });

                sessionStorage.removeItem('directBuyProductId');
                sessionStorage.removeItem('directBuyQuantity');

            } catch (err: any) {
                console.error("주문 정보 로딩 실패:", err);
                if (err.response && err.response.status === 403) {
                    setPageError('로그인이 필요하거나, 주문 정보를 조회할 권한이 없습니다.');
                } else if (err.response && err.response.data && err.response.data.message) {
                    setPageError(`오류: ${err.response.data.message}`);
                } else {
                    setPageError('주문 정보를 불러오는 데 실패했습니다.');
                }

            } finally {
                setLoading(false);
            }
        };

        loadOrderData();
    }, []);


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

    // ✨ 결제 수단 변경 핸들러
    const handlePaymentMethodChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        // `as 'CARD' | 'CELL_PHONE' | 'ACCOUNT'`를 사용하여 타입을 강제 변환
        setPaymentMethod(e.target.value as 'CARD' | 'CELL_PHONE' | 'ACCOUNT');
    };


    const handlePayment = async () => {

        if (!productInfo || !userProfile) {
            alert("주문 정보를 불러오는 중이거나 유효하지 않습니다.");
            return;
        }

        if (!shippingInfo.receiverName || !shippingInfo.phone || !shippingInfo.address) {
            alert("배송지 정보를 모두 입력해주세요.");
            return;
        }

        const totalPaymentAmount = productInfo.price * productInfo.quantity + 3000; // 배송비 3000원 고정 예시
        const confirmPayment = window.confirm(
            `총 ${totalPaymentAmount.toLocaleString()}원에 대해 결제를 진행하시겠습니까?`
        );

        if (!confirmPayment) {
            return;
        }

        // 토스페이먼츠 결제 위젯에서 결제 요청
        if (paymentWidgetRef.current) {
            try {
                const orderId = `direct_${productInfo.productId}_${Date.now()}`;
                
                await paymentWidgetRef.current.requestPayment({
                    orderId: orderId,
                    orderName: `${productInfo.name} ${productInfo.quantity}개`,
                    customerName: shippingInfo.receiverName,
                    customerEmail: userProfile.email || 'customer@example.com',
                    successUrl: process.env.NEXT_PUBLIC_TOSS_SUCCESS_URL || `${window.location.origin}/customer/orders/success`,
                    failUrl: process.env.NEXT_PUBLIC_TOSS_FAIL_URL || `${window.location.origin}/customer/orders/fail`,
                });
            } catch (error: any) {
                console.error('결제 요청 오류:', error);
                
                // 결제 승인 처리
                if (error.paymentKey && error.orderId && error.amount) {
                    await processPaymentApproval(error.paymentKey, error.orderId, error.amount);
                } else {
                    alert('결제 처리 중 오류가 발생했습니다.');
                }
            }
        }
    };

    const processPaymentApproval = async (paymentKey: string, orderId: string, amount: number) => {
        try {
            // PayRequestDTO의 새로운 구조에 맞춰 데이터 구성
            const payRequestDTO: PayRequestDTO = {
                receiverName: shippingInfo.receiverName,
                phone: shippingInfo.phone,
                deliveryAddress: shippingInfo.address,
                paymentMethod: paymentMethod,
                paymentKey: paymentKey,
                tossOrderId: orderId,
                amount: amount,
                productId: productInfo.productId,
                quantity: productInfo.quantity,
            };

            const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/customer/orders/direct-payment`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('customerToken')}`
                },
                body: JSON.stringify(payRequestDTO),
            });

            if (!response.ok) {
                throw new Error('결제 승인 처리에 실패했습니다.');
            }

            const result = await response.json();
            alert('결제가 성공적으로 완료되었습니다!');
            router.push('/customer/orders');
            
        } catch (error) {
            console.error('결제 승인 처리 오류:', error);
            alert('결제 승인 처리 중 오류가 발생했습니다.');
        }
    };

    if (loading) {
        return <div className="loading-container">주문 정보를 불러오는 중입니다...</div>;
    }

    if (pageError) {
        return <div className="error-container text-red-500 text-center py-20">{pageError}</div>;
    }

    if (!productInfo || !userProfile) {
        return <div className="error-container text-red-500">알 수 없는 오류가 발생했습니다.</div>;
    }

    // --- UI 렌더링 ---
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
                    <label htmlFor="receiverName">받는 사람</label>
                    <input id="receiverName" name="receiverName" value={shippingInfo.receiverName} onChange={handleShippingInfoChange} />

                    <label htmlFor="phone">연락처</label>
                    <input id="phone" name="phone" type="tel" value={shippingInfo.phone} onChange={handleShippingInfoChange} />

                    <label htmlFor="address">주소</label>
                    <input id="address" name="address" value={shippingInfo.address} onChange={handleShippingInfoChange} />
                    {/* TODO: 주소 검색 버튼 및 기능 추가 */}

                </div>
            </section>

            {/* 4. 결제 수단 */}
            <section className="order-section">
                <h2>결제 수단</h2>
                <div className="payment-selector">
                    <label className={paymentMethod === 'CARD' ? 'active' : ''}>
                        <input
                            type="radio"
                            name="paymentMethod"
                            value="CARD"
                            checked={paymentMethod === 'CARD'}
                            onChange={handlePaymentMethodChange} // ✨ 변경된 핸들러 사용
                        />
                        신용/체크카드
                    </label>
                    <label className={paymentMethod === 'CELL_PHONE' ? 'active' : ''}>
                        <input
                            type="radio"
                            name="paymentMethod"
                            value="CELL_PHONE"
                            checked={paymentMethod === 'CELL_PHONE'}
                            onChange={handlePaymentMethodChange}
                        />
                        휴대폰
                    </label>
                    <label className={paymentMethod === 'ACCOUNT' ? 'active' : ''}>
                        <input
                            type="radio"
                            name="paymentMethod"
                            value="ACCOUNT"
                            checked={paymentMethod === 'ACCOUNT'}
                            onChange={handlePaymentMethodChange}
                        />
                        계좌이체
                    </label>
                </div>

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
            </section>

            <button className="payment-button" onClick={handlePayment}>
                결제하기
            </button>

        </div>
    );
}