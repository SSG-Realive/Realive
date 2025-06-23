'use client';

import { useEffect, useState, useRef, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import { useCartStore } from '@/store/customer/useCartStore';
import { fetchMyProfile } from '@/service/customer/customerService';
import { loadPaymentWidget, PaymentWidgetInstance } from '@tosspayments/payment-widget-sdk';
import type { CartItem } from '@/types/customer/cart/cart';
import type { MemberReadDTO } from '@/types/customer/member/member';
import Navbar from '@/components/customer/common/Navbar';

// 클라이언트 키는 .env.local 파일에 NEXT_PUBLIC_TOSS_CLIENT_KEY=... 와 같이 저장되어 있어야 합니다.
const TOSS_CLIENT_KEY = process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY as string;

export default function NewOrderPage() {
    const router = useRouter();
    const { id: customerId, userName: initialName } = useAuthStore();
    const cartItems = useCartStore((state) => state.itemsForCheckout);

    const [userProfile, setUserProfile] = useState<MemberReadDTO | null>(null);
    const [shippingInfo, setShippingInfo] = useState({ receiverName: '', phone: '', address: '' });
    const paymentWidgetRef = useRef<PaymentWidgetInstance | null>(null);

    // 화면 크기를 감지하여 상태로 관리
    const [isLgScreen, setIsLgScreen] = useState(false);

    // 컴포넌트 마운트 시 화면 크기를 확인하고, 리사이즈 이벤트를 감지하는 useEffect
    useEffect(() => {
        const checkScreenSize = () => {
            setIsLgScreen(window.innerWidth >= 1024); // Tailwind CSS의 'lg' breakpoint는 1024px
        };
        checkScreenSize(); // 초기 로드 시 확인
        window.addEventListener('resize', checkScreenSize); // 화면 크기가 변경될 때마다 재확인
        
        // 컴포넌트 언마운트 시 이벤트 리스너 제거 (메모리 누수 방지)
        return () => window.removeEventListener('resize', checkScreenSize);
    }, []);

    // 사용자 정보 로딩 및 장바구니 유효성 검사
    useEffect(() => {
        if (cartItems.length === 0) {
            alert("결제할 상품 정보가 없습니다. 장바구니 페이지로 돌아갑니다.");
            router.replace('/customer/cart');
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
                
                const paymentWidgetSelector = isLgScreen ? '#payment-widget' : '#payment-widget-mobile';
                const agreementSelector = isLgScreen ? '#agreement' : '#agreement-mobile';
                
                // 해당 ID를 가진 요소가 DOM에 존재하는지 확인 후 렌더링
                if (document.getElementById(paymentWidgetSelector.slice(1))) {
                    tossPayments.renderPaymentMethods(paymentWidgetSelector, { value: finalAmount }, { variantKey: 'DEFAULT' });
                }
                if (document.getElementById(agreementSelector.slice(1))) {
                    tossPayments.renderAgreement(agreementSelector, { variantKey: 'DEFAULT' });
                }

                paymentWidgetRef.current = tossPayments;
            } catch (error) {
                console.error("토스페이먼츠 위젯 렌더링 실패:", error);
            }
        };
        initializeWidget();
    }, [userProfile, customerId, finalAmount, isLgScreen]);

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
        return <div className="flex justify-center items-center h-screen">주문 정보를 준비 중입니다...</div>;
    }

    return (
        <div className="bg-gray-50 min-h-screen pb-24 lg:pb-0">
            <Navbar />
            <main className="max-w-7xl mx-auto px-4 lg:px-8 py-8">
                <h1 className="text-2xl lg:text-3xl font-bold mb-6">주문 / 결제</h1>

                <div className="grid grid-cols-1 lg:grid-cols-3 lg:gap-8">
                    <div className="lg:col-span-2 space-y-6">

                        <section className="bg-white p-6 rounded-lg shadow-sm">
                            <h2 className="text-lg font-semibold mb-4">배송지</h2>
                            <div className="space-y-3">
                                <div><label className="text-sm font-medium text-gray-700">받는 분</label><input type="text" name="receiverName" value={shippingInfo.receiverName} onChange={handleChange} className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2" /></div>
                                <div><label className="text-sm font-medium text-gray-700">연락처</label><input type="text" name="phone" value={shippingInfo.phone} onChange={handleChange} className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2" /></div>
                                <div><label className="text-sm font-medium text-gray-700">주소</label><input type="text" name="address" value={shippingInfo.address} onChange={handleChange} className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm p-2" /></div>
                            </div>
                        </section>
                        
                        <section className="bg-white p-6 rounded-lg shadow-sm">
                            <h2 className="text-lg font-semibold mb-4">주문 상품</h2>
                            <div className="space-y-4">
                                {cartItems.map(item => (
                                    <div key={item.cartItemId} className="flex items-start space-x-4">
                                        <img src={item.imageThumbnailUrl || '/default-image.png'} alt={item.productName} className="w-20 h-20 object-cover rounded-md flex-shrink-0" />
                                        <div className="flex-grow"><p className="font-medium">{item.productName}</p><p className="text-sm text-gray-500">수량: {item.quantity}개</p></div>
                                        <p className="font-semibold whitespace-nowrap">{(item.productPrice * item.quantity).toLocaleString()}원</p>
                                    </div>
                                ))}
                            </div>
                        </section>
                        
                        {!isLgScreen && (
                            <div className="space-y-6">
                                <section className="bg-white p-6 rounded-lg shadow-sm"><h2 className="text-lg font-semibold mb-4">결제 수단</h2><div id="payment-widget-mobile" style={{ width: '100%' }} /></section>
                                <section className="bg-white p-6 rounded-lg shadow-sm">
                                    <h3 className="text-lg font-semibold mb-4">결제 상세</h3>
                                    <div className="space-y-2 text-sm">
                                        <div className="flex justify-between"><span>총 상품금액</span><span>{totalProductPrice.toLocaleString()}원</span></div>
                                        <div className="flex justify-between"><span>배송비</span><span>+ {deliveryFee.toLocaleString()}원</span></div>
                                        <div className="border-t my-2"></div>
                                        <div className="flex justify-between font-bold text-base"><span>최종 결제 금액</span><span>{finalAmount.toLocaleString()}원</span></div>
                                    </div>
                                    <div id="agreement-mobile" className="mt-4" />
                                </section>
                            </div>
                        )}
                    </div>
                    
                    {isLgScreen && (
                        <aside className="lg:col-span-1">
                            <div className="lg:sticky lg:top-8 space-y-6">
                                <section className="bg-white p-6 rounded-lg shadow-sm"><h2 className="text-lg font-semibold mb-4">결제 수단</h2><div id="payment-widget" style={{ width: '100%' }} /></section>
                                <section className="bg-white p-6 rounded-lg shadow-sm">
                                    <h3 className="text-lg font-semibold mb-4">결제 금액</h3>
                                    <div className="space-y-2">
                                        <div className="flex justify-between text-sm"><span>총 상품금액</span><span>{totalProductPrice.toLocaleString()}원</span></div>
                                        <div className="flex justify-between text-sm"><span>배송비</span><span>+ {deliveryFee.toLocaleString()}원</span></div>
                                        <div className="border-t my-2"></div>
                                        <div className="flex justify-between font-bold text-base"><span>최종 결제 금액</span><span>{finalAmount.toLocaleString()}원</span></div>
                                    </div>
                                    <div id="agreement" className="mt-4" />
                                    <button className="w-full bg-green-500 text-white font-bold py-3 mt-4 rounded-md hover:bg-green-600 transition-colors" onClick={handlePayment} disabled={cartItems.length === 0}>
                                        {finalAmount.toLocaleString()}원 결제하기
                                    </button>
                                </section>
                            </div>
                        </aside>
                    )}
                </div>
            </main>
            
            {!isLgScreen && (
                <footer className="fixed bottom-0 left-0 right-0 bg-white border-t p-4 shadow-lg-top">
                    <button className="w-full bg-green-500 text-white font-bold py-3 rounded-md hover:bg-green-600 transition-colors" onClick={handlePayment} disabled={cartItems.length === 0}>
                        {finalAmount.toLocaleString()}원 결제하기
                    </button>
                </footer>
            )}
        </div>
    );
}