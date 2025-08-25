'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { OrderResponseDTO } from '@/types/orders/orderResponseDTO';
import { OrderItemResponseDTO } from '@/types/orders/orderItemResponseDTO';
import { useAuthStore } from '@/store/customer/authStore';

async function fetchOrderDetail(orderId: number, token: string): Promise<OrderResponseDTO> {
    const url = `https://www.realive-ssg.click/api/customer/orders/${orderId}`;
    const response = await fetch(url, {
        cache: 'no-store',
        redirect: 'follow',
        headers: {
            Authorization: token ? `Bearer ${token}` : '',
            'Content-Type': 'application/json',
        },
    });

    if (!response.ok) {
        const errorData = await response.text();
        throw new Error(`주문 상세 정보를 불러오는 데 실패했습니다: ${response.statusText || errorData}`);
    }

    return response.json();
}

interface OrderDetailClientProps {
    orderId: string;
}

export default function OrderDetailClient({ orderId }: OrderDetailClientProps) {
    const [orderData, setOrderData] = useState<OrderResponseDTO | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const router = useRouter();
    const { accessToken, hydrated } = useAuthStore();

    useEffect(() => {
        if (!hydrated) return;

        if (!accessToken) {
            setError('로그인 토큰이 없습니다. 다시 로그인 해주세요.');
            setLoading(false);
            return;
        }

        const numericOrderId = Number(orderId);
        if (isNaN(numericOrderId) || numericOrderId <= 0) {
            setError('유효하지 않은 주문 ID입니다.');
            setLoading(false);
            return;
        }

        const getOrder = async () => {
            try {
                const data = await fetchOrderDetail(numericOrderId, accessToken);
                setOrderData(data);
            } catch (err) {
                setError(err instanceof Error ? err.message : '알 수 없는 오류가 발생했습니다.');
            } finally {
                setLoading(false);
            }
        };

        getOrder();
    }, [orderId, accessToken, hydrated]);

    if (loading) {
        return (
            <div className="min-h-screen flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto mb-4"></div>
                    <p className="text-lg">주문 정보를 불러오는 중...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div>
                <div className="container mx-auto p-4 font-inter">

                    <h1 className="text-3xl font-light mb-6 text-center text-gray-800">
                        주문 상세
                    </h1>
                    <div
                        className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded-lg relative mb-4"
                        role="alert"
                    >
                        <strong className="font-light">오류: </strong>
                        <span className="block sm:inline">{error}</span>
                    </div>
                </div>
            </div>
        );
    }

    if (!orderData) {
        return (
            <div className="text-center py-10">
                <p>주문 정보를 찾을 수 없습니다.</p>
            </div>
        );
    }

    return (
        <div className="container mx-auto p-4 max-w-screen-lg min-h-screen font-inter">
            {/* 주문 기본 정보 */}
            <div className="bg-white-50 border border-gray-200 rounded-md p-8 mb-8">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6 text-gray-700">
                    <div>
                        <p className="text-sm mb-2">주문 번호: <span className="font-bold">{orderData.orderId}</span></p>
                        <p className="text-sm mb-2">고객 ID: <span className="font-light">{orderData.customerId}</span></p>
                        <p className="text-sm mb-2">배송 주소: <span className="font-light">{orderData.deliveryAddress}</span></p>
                        <p className="text-sm mb-2">총 주문 가격: <span className="font-bold">{orderData.totalPrice.toLocaleString()}원</span></p>
                    </div>
                    <div>
                        <p className="text-sm mb-2">주문 일시: <span className="font-light">{new Date(orderData.orderCreatedAt).toLocaleString()}</span></p>
                        <p className="text-sm mb-2">최종 업데이트: <span className="font-light">{new Date(orderData.updatedAt).toLocaleString()}</span></p>
                        <p className="text-sm mb-2">결제 방식: <span className="font-light">{orderData.paymentType}</span></p>
                        <p className="text-sm mb-2">배송비: <span className="font-light">{orderData.deliveryFee.toLocaleString()}원</span></p>
                        <p className="text-sm mb-2">수령인: <span className="font-light">{orderData.receiverName}</span></p>
                        <p className="text-sm mb-2">연락처: <span className="font-light">{orderData.phone}</span></p>
                    </div>
                </div>
                <p className="text-sm font-light mt-4">
                    <span className="inline-block px-4 py-1 font-light rounded-full bg-black text-white">
                        {orderData.orderStatus === 'ORDER' ? '주문 완료' : orderData.orderStatus === 'CANCEL' ? '주문 취소' : orderData.orderStatus}
                    </span>
                    <span className="ml-4 font-light text-blue-600">({orderData.deliveryStatus})</span>
                </p>
            </div>

            {/* 주문 상품 슬라이더 */}
            <div className="bg-white-50 border border-gray-200 rounded-md p-8 mb-8">
                <h2 className="text-sm font-bold mb-6 text-gray-800">주문 상품</h2>
                {orderData.orderItems.length === 0 ? (
                    <p className="text-sm text-gray-600 text-center py-4">주문된 상품이 없습니다.</p>
                ) : (
                    Object.entries(
                        orderData.orderItems.reduce((acc, item) => {
                            if (!acc[item.sellerId]) {
                                acc[item.sellerId] = {
                                    sellerName: item.sellerName,
                                    items: []
                                };
                            }
                            acc[item.sellerId].items.push(item);
                            return acc;
                        }, {} as Record<string, { sellerName: string; items: OrderItemResponseDTO[] }>)
                    ).map(([sellerId, { sellerName, items }]) => (
                        <div key={sellerId} className="mb-10">
                            <h3 className="text-sm font-light text-gray-800 mb-4">{sellerName}</h3>
                            <div className="overflow-x-auto whitespace-nowrap scrollbar-hide px-1">
                                {items.map((item) => (
                                    <div
                                        key={item.productId}
                                        className="inline-block align-top w-64 mr-4 p-4 rounded-md bg-white shadow-sm"
                                    >
                                        <img
                                            src={
                                                item.imageUrl && typeof item.imageUrl === 'string' && item.imageUrl.trim() !== ''
                                                    ? item.imageUrl
                                                    : '/images/placeholder.png'
                                            }
                                            alt={item.productName || '상품 이미지'}
                                            className="w-full h-40 object-cover mb-2 border-none outline-none ring-0"
                                            onError={(e) => {
                                                e.currentTarget.src = '/images/placeholder.png';
                                                e.currentTarget.onerror = null;
                                            }}
                                        />
                                        <h4 className="text-sm font-medium text-gray-800 mb-1">{item.productName}</h4>
                                        <p className="text-sm text-gray-600 mb-1">{item.quantity}개</p>
                                        <p className="text-sm font-semibold text-gray-700">{item.price.toLocaleString()}원</p>
                                    </div>
                                ))}
                            </div>
                            {!items[0].reviewWritten && (
                                <div className="text-right mt-4">
                                    <button
                                        className="w-full text-sm px-4 py-2 bg-black text-white rounded-none hover:bg-gray-800 transition"
                                        onClick={() =>
                                            router.push(`/customer/mypage/orders/${orderData.orderId}/reviews?sellerId=${sellerId}`)
                                        }
                                    >
                                        리뷰 작성
                                    </button>
                                </div>
                            )}
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}
