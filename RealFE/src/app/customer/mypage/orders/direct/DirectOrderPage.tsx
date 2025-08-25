// src/app/customer/mypage/orders/direct/page.tsx (DirectOrderPage)
'use client';

import { useEffect, useState, useRef, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import Script from 'next/script';
import { fetchMyProfile } from '@/service/customer/customerService';
import { getDirectPaymentInfo, processDirectPaymentApi } from '@/service/order/orderService';
import { DirectPaymentInfoDTO, PayRequestDTO } from '@/types/customer/order/order';
import { useAuthStore } from '@/store/customer/authStore';
import {
  loadTossPayments,
  requestPayment,
  DEFAULT_CONFIG,
  PaymentRequestOptions
} from '@/service/order/tossPaymentService';
import './DirectOrderPage.css';
import { useGlobalDialog } from '@/app/context/dialogContext';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import AddressInput , { parseAddress } from '@/components/customer/join/AddressInput';

// UserProfile 타입은 그대로 유지
interface UserProfile {
    name: string;
    email: string;
    phone: string;
    address: string;
}

export default function DirectOrderPage() {
    const router = useRouter();
    const { id: customerId } = useAuthStore();
    const tossPaymentsRef = useRef<any>(null);
    const [scriptLoaded, setScriptLoaded] = useState(false);
    const {show} = useGlobalDialog();
    // --- 상태 관리 ---
    const [productInfo, setProductInfo] = useState<DirectPaymentInfoDTO | null>(null);
    const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
    const [loading, setLoading] = useState(true);
    const [pageError, setPageError] = useState<string | null>(null);
    const [error, setError] = useState<string | null>(null);

    const [confirmPromise, setConfirmPromise] = useState<{ resolve: (value: boolean) => void } | null>(null);
    const [confirmMessage, setConfirmMessage] = useState('');

    const confirm = (message: string): Promise<boolean> => {
        return new Promise((resolve) => {
            setConfirmMessage(message);
            setConfirmPromise({ resolve });
        });
    };

    const handleConfirmResolve = (result: boolean) => {
        if (confirmPromise) {
            confirmPromise.resolve(result);
            setConfirmPromise(null);
        }
    };

    // 배송지 폼 상태
    const [shippingInfo, setShippingInfo] = useState({
        receiverName: '',
        phone: '',
        address: '',
    });

    // 결제 수단 상태
    const [paymentMethod, setPaymentMethod] = useState<'CARD' | 'CELL_PHONE' | 'ACCOUNT'>('CARD');

    // 계산된 금액들
    const totalProductPrice = productInfo ? productInfo.price * productInfo.quantity : 0;
    const deliveryFee = 3000; // 고정 배송비
    const finalAmount = totalProductPrice + deliveryFee;

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

    // --- 토스페이먼츠 SDK 초기화 ---
    useEffect(() => {
        if (!userProfile || !customerId || finalAmount === 0 || !scriptLoaded) return;

        const initializeTossPayments = async () => {
            try {
                console.log('토스페이먼츠 SDK 초기화 시작...', { customerId, finalAmount });
                setError(null);

                // DOM 엘리먼트가 준비될 때까지 대기
                await new Promise(resolve => setTimeout(resolve, 100));

                // 토스페이먼츠 객체 생성
                if (!(window as any).TossPayments) {
                    throw new Error('토스페이먼츠 SDK가 로드되지 않았습니다');
                }

                const tossPayments = (window as any).TossPayments(DEFAULT_CONFIG.CLIENT_KEY);
                tossPaymentsRef.current = tossPayments;
                console.log('토스페이먼츠 SDK 초기화 완료');

            } catch (error: any) {
                console.error("토스페이먼츠 SDK 초기화 실패:", error);
                setError(`토스페이먼츠 SDK 초기화에 실패했습니다: ${error.message}`);
            }
        };

        initializeTossPayments();
    }, [userProfile, customerId, finalAmount, scriptLoaded]);

    // --- 이벤트 핸들러 ---
    const handleShippingInfoChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const { name, value } = e.target;
        setShippingInfo(prev => ({ ...prev, [name]: value }));
    };

    const handlePaymentMethodChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setPaymentMethod(e.target.value as 'CARD' | 'CELL_PHONE' | 'ACCOUNT');
    };
    //AddressInput 컴포넌트가 사용할 함수
const handleAddressChange = useCallback((fullAddress: string) => {
    setShippingInfo(prev => ({ ...prev, address: fullAddress }));
}, []);

    const handlePayment = async () => {
        console.log('결제하기 버튼 클릭됨');
        console.log('tossPaymentsRef.current:', tossPaymentsRef.current);
        console.log('scriptLoaded:', scriptLoaded);
        console.log('window.TossPayments:', (window as any).TossPayments);

        if (!productInfo || !userProfile) {
            show("주문 정보를 불러오는 중이거나 유효하지 않습니다.");
            return;
        }

        if (!shippingInfo.receiverName || !shippingInfo.phone || !shippingInfo.address) {
            show("배송지 정보를 모두 입력해주세요.");
            return;
        }

        const totalPaymentAmount = productInfo.price * productInfo.quantity + 3000;
        const confirmPayment = await confirm(
            `총 ${totalPaymentAmount.toLocaleString()}원에 대해 결제를 진행하시겠습니까?`
        );

        if (!confirmPayment) {
            return;
        }

        // 토스페이먼츠 객체가 없으면 다시 초기화 시도
        if (!tossPaymentsRef.current) {
            console.log('토스페이먼츠 객체가 없음, 재초기화 시도...');
            try {
                if ((window as any).TossPayments) {
                    const tossPayments = (window as any).TossPayments(DEFAULT_CONFIG.CLIENT_KEY);
                    tossPaymentsRef.current = tossPayments;
                    console.log('토스페이먼츠 객체 재초기화 완료');
                } else {
                    show('토스페이먼츠 SDK가 로드되지 않았습니다. 다시 결제를 진행해주세요.');
                    return;
                }
            } catch (error) {
                console.error('토스페이먼츠 객체 재초기화 실패:', error);
                show('결제 시스템 초기화에 실패했습니다. 다시 결제를 진행해 주세요.');
                return;
            }
        }

        try {
            const orderId = `direct_${productInfo.productId}_${Date.now()}`;

            console.log('결제 요청 준비 중...');

            // 결제 성공 페이지에서 사용할 주문 정보를 sessionStorage에 저장
            const checkoutInfo = {
                productId: productInfo.productId,
                quantity: productInfo.quantity,
                shippingInfo: {
                    receiverName: shippingInfo.receiverName,
                    phone: shippingInfo.phone,
                    address: shippingInfo.address,
                },
                paymentMethod: paymentMethod,
                customerId: customerId,
            };
            sessionStorage.setItem('checkout_info', JSON.stringify(checkoutInfo));

            const sanitizedPhone = shippingInfo.phone.replace(/[^0-9]/g, '');

            const paymentOptions: PaymentRequestOptions = {
                orderId: orderId,
                orderName: `${productInfo.productName} ${productInfo.quantity}개`,
                amount: finalAmount,
                successUrl: `${window.location.origin}/customer/mypage/orders/success`,
                failUrl: `${window.location.origin}/customer/mypage/orders/fail`,
                customerEmail: userProfile.email || 'customer@example.com',
                customerName: shippingInfo.receiverName,
                customerMobilePhone: sanitizedPhone,
            };


            console.log('결제 옵션:', paymentOptions);

            console.log('토스페이먼츠 결제창 호출 중...');
            await requestPayment(tossPaymentsRef.current, paymentOptions);
        } catch (error: any) {
            console.error('결제 요청 오류:', error);

            if (error.paymentKey && error.orderId && error.amount) {
                await processPaymentApproval(error.paymentKey, error.orderId, error.amount);
            } else {
                show(`결제 처리 중 오류가 발생했습니다: ${error.message}`);
            }
        }
    };

    const processPaymentApproval = async (paymentKey: string, orderId: string, amount: number) => {
        try {
            const sanitizedPhone = shippingInfo.phone.replace(/[^0-9]/g, '');
            const payRequestDTO: PayRequestDTO = {
                receiverName: shippingInfo.receiverName,
                phone: sanitizedPhone,
                deliveryAddress: shippingInfo.address,
                paymentMethod: 'CARD', // 토스페이먼츠로 고정
                paymentKey: paymentKey,
                tossOrderId: orderId,
                productId: productInfo!.productId,
                quantity: productInfo!.quantity,
            };

            const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/customer/mypage/orders/direct-payment`, {
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
            await show('결제가 성공적으로 완료되었습니다!');
            router.push('/customer/mypage/orders');

        } catch (error) {
            console.error('결제 승인 처리 오류:', error);
            show('결제 승인 처리 중 오류가 발생했습니다.');
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

    return (
        <div className="order-page-container">
            {confirmPromise && (
                <ConfirmDialog
                    open={true}
                    message={confirmMessage}
                    onResolve={handleConfirmResolve}
                />
            )}
            {/* 토스페이먼츠 기본 SDK 로드 */}
            <Script
                src="https://js.tosspayments.com/v1/payment"
                strategy="afterInteractive"
                onLoad={() => {
                    console.log('토스페이먼츠 SDK 로드 완료');
                    setScriptLoaded(true);
                }}
                onError={() => {
                    console.error('토스페이먼츠 SDK 로드 실패');
                    setError('토스페이먼츠 SDK를 불러올 수 없습니다.');
                }}
            />

            {/* 1. 주문 상품 정보 */}
            <section className="order-section">
                <h2>주문 상품</h2>
                <div className="product-summary-card flex items-start gap-4">
                    <img
                        src={productInfo.imageUrl || '/images/placeholder.png'}
                        alt={productInfo.productName}
                        className="w-24 h-24 object-cover"
                    />
                    <div className="flex flex-col justify-between">
                        <p className="product-name text-sm text-gray-800">{productInfo.productName}</p>
                        <p className="product-quantity text-sm text-gray-600">수량: {productInfo.quantity}개</p>
                        <p className="product-price text-sm text-black mt-1">
                            {(productInfo.price * productInfo.quantity).toLocaleString()}원
                        </p>
                    </div>
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
                    <AddressInput
        onAddressChange={handleAddressChange}
        defaultAddress={parseAddress(shippingInfo.address)}
    />
                </div>
            </section>

            {/* 4. 토스페이먼츠 결제 */}
            <section className="order-section">
                <h2>결제</h2>

            {/* 5. 결제 동의 및 금액 요약 */}
                <aside className="order-summary">
                    <div id="agreement"></div>
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
                </aside>
            </section>

            <button
                className="w-full bg-black text-white px-4 py-2 rounded hover:bg-gray-900 transition"
                onClick={handlePayment}
            >
                결제하기
            </button>
        </div>
    );
}