'use client';

import { useEffect, useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { useAuthStore } from '@/store/customer/authStore';
import { processDirectPaymentApi } from '@/service/order/orderService';
import type { PayRequestDTO } from '@/types/customer/order/order';

export default function PaymentSuccessPage() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const { id: customerId } = useAuthStore();
    
    const [orderId, setOrderId] = useState<number | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        const processPayment = async () => {
            try {
                setLoading(true);
                
                // URL 파라미터에서 결제 정보 가져오기
                const paymentKey = searchParams.get('paymentKey');
                const orderId = searchParams.get('orderId');
                const amount = searchParams.get('amount');
                
                if (!paymentKey || !orderId || !amount) {
                    throw new Error('결제 정보가 올바르지 않습니다.');
                }

                // sessionStorage에서 체크아웃 정보 가져오기
                const checkoutInfoStr = sessionStorage.getItem('checkout_info');
                if (!checkoutInfoStr) {
                    throw new Error('주문 정보를 찾을 수 없습니다.');
                }

                const checkoutInfo = JSON.parse(checkoutInfoStr);
                
                // 결제 승인 요청
                const payRequest: PayRequestDTO = {
                    paymentKey,
                    tossOrderId: orderId,
                    amount: parseInt(amount),
                    receiverName: checkoutInfo.shippingInfo.receiverName,
                    phone: checkoutInfo.shippingInfo.phone,
                    deliveryAddress: checkoutInfo.shippingInfo.address,
                    paymentMethod: 'CARD', // 기본값
                    customerId: customerId,
                    ...(checkoutInfo.orderItems ? { orderItems: checkoutInfo.orderItems } : {}),
                    ...(checkoutInfo.productId ? { productId: checkoutInfo.productId, quantity: checkoutInfo.quantity } : {})
                };

                const createdOrderId = await processDirectPaymentApi(payRequest);
                setOrderId(createdOrderId);
                
                // 체크아웃 정보 삭제
                sessionStorage.removeItem('checkout_info');
                
            } catch (err) {
                console.error('결제 처리 실패:', err);
                setError(err instanceof Error ? err.message : '결제 처리에 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };

        processPayment();
    }, [searchParams, customerId]);

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-lg">결제를 처리하고 있습니다...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center max-w-md mx-auto p-6">
                    <div className="text-red-500 text-6xl mb-4">❌</div>
                    <h1 className="text-2xl font-bold mb-4">결제 처리 실패</h1>
                    <p className="text-gray-600 mb-6">{error}</p>
                    <div className="space-y-3">
                        <button 
                            onClick={() => router.back()}
                            className="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700"
                        >
                            이전 페이지로 돌아가기
                        </button>
                        <Link 
                            href="/customer/orders"
                            className="block w-full bg-gray-600 text-white py-2 px-4 rounded hover:bg-gray-700 text-center"
                        >
                            주문 목록으로 가기
                        </Link>
                    </div>
                </div>
            </div>
        );
    }

    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="text-center max-w-md mx-auto p-6">
                <div className="text-green-500 text-6xl mb-4">✅</div>
                <h1 className="text-2xl font-bold mb-4">결제가 완료되었습니다!</h1>
                <p className="text-gray-600 mb-6">
                    주문이 성공적으로 처리되었습니다.<br />
                    주문번호: <span className="font-semibold">{orderId}</span>
                </p>
                <div className="space-y-3">
                    <Link 
                        href={`/customer/orders/${orderId}`}
                        className="block w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700 text-center"
                    >
                        주문 상세보기
                    </Link>
                    <Link 
                        href="/customer/orders"
                        className="block w-full bg-gray-600 text-white py-2 px-4 rounded hover:bg-gray-700 text-center"
                    >
                        주문 목록으로 가기
                    </Link>
                    <Link 
                        href="/main"
                        className="block w-full bg-green-600 text-white py-2 px-4 rounded hover:bg-green-700 text-center"
                    >
                        쇼핑 계속하기
                    </Link>
                </div>
            </div>
        </div>
    );
} 