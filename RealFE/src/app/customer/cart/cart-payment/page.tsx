// src/app/customer/mypage/orders/cart-payment/page.tsx
import { PayRequestDTO } from '@/types/orders/payRequestDTO';
import { ProductQuantityDTO } from '@/types/orders/productQuantityDTO';
import { cookies } from 'next/headers';
import { redirect } from 'next/navigation';
import React from 'react';

interface ErrorResponse {
    status: number;
    code: string;
    message: string;
}

async function CartPaymentPage() {
    const cookieStore = await cookies();
    const token = cookieStore.get('token')?.value;

    const cartResponse = await fetch('https://www.realive-ssg.click/api/customer/cart', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': token ? `Bearer ${token}` : ''
        },
        cache: 'no-store' // 매번 최신 장바구니 정보를 가져오도록 설정
    });

    if (!cartResponse.ok) {
        const errorData: ErrorResponse = await cartResponse.json();
        // 장바구니 데이터 로드 실패 시 에러 처리
        throw new Error(errorData.message || '장바구니 데이터를 불러오는데 실패했습니다.');
    }

    const cartData = await cartResponse.json();

    // --- 2. 결제 처리 Server Action ---
    const handlePayment = async (formData: FormData) => {
        'use server'; // 이 함수는 서버에서 실행됩니다.

        const cookieStore = await cookies();
        const token = cookieStore.get('token')?.value;

        // 폼 데이터에서 필수 정보 추출
        const receiverName = formData.get('receiverName')?.toString();
        const phone = formData.get('phone')?.toString();
        const deliveryAddress = formData.get('deliveryAddress')?.toString();
        const paymentMethod: PayRequestDTO['paymentMethod'] = (formData.get('paymentMethod')?.toString() as PayRequestDTO['paymentMethod']) || "CARD";

        // 필수 필드 유효성 검사 (프론트엔드에서도 유효성 검사 필요)
        if (!receiverName || !phone || !deliveryAddress) {
            throw new Error('모든 필수 정보를 입력해주세요.');
        }

        // 장바구니 데이터를 PayRequestDTO의 orderItems 형식으로 변환
        const orderItems: ProductQuantityDTO[] = cartData.items.map((item: any) => ({
            productId: item.productId,
            quantity: item.quantity
        }));

        const finalPayRequestDTO: PayRequestDTO = {
            orderItems: orderItems,
            // 단일 상품 결제 옵션은 사용하지 않으므로 undefined
            productId: undefined,
            quantity: undefined,
            receiverName: receiverName, // 폼 데이터로 업데이트
            phone: phone, // 폼 데이터로 업데이트
            deliveryAddress: deliveryAddress, // 폼 데이터로 업데이트
            paymentMethod: paymentMethod, // 폼 데이터로 업데이트 (현재는 CARD 고정)
        };

        try {
            // 백엔드 POST API 호출
            const paymentProcessResponse = await fetch('https://www.realive-ssg.click/api/customer/cart/payment', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': token ? `Bearer ${token}` : ''
                },
                body: JSON.stringify(finalPayRequestDTO),
            });

            if (!paymentProcessResponse.ok) {
                const errorData: ErrorResponse = await paymentProcessResponse.json();
                console.error('결제 처리 서버 오류:', errorData);
                throw new Error(errorData.message || '결제 처리 중 오류가 발생했습니다. 다시 시도해 주세요.');
            }

            const orderId = await paymentProcessResponse.json();
            console.log("장바구니 주문이 성공적으로 생성되었습니다. 주문 ID:", orderId);

            // 결제 성공 후 주문 완료 페이지로 리다이렉트
            redirect(`/customer/mypage/orders/${orderId}`);

        } catch (error: any) {
            console.error('결제 처리 중 예상치 못한 오류 발생:', error);
            throw new Error(`결제 처리 중 오류가 발생했습니다: ${error.message}`);
        }
    };

    return (
        <div className="container mx-auto p-4 max-w-2xl">
            <h1 className="text-3xl font-light mb-8 text-center">결제</h1>

            {/* 주문 정보 요약 */}
            <div className="bg-white rounded-lg shadow-md p-6 mb-6">
                <h2 className="text-xl font-semibold mb-4">주문 정보</h2>
                <div className="space-y-2">
                    {/* Receiver Name, Phone, Delivery Address는 Form 필드로 이동하여 사용자 입력 가능하게 */}
                    <p><span className="font-light">총 결제 금액:</span> {cartData.totalPrice?.toLocaleString()}원</p>
                </div>
            </div>

            {/* 결제 정보 입력 폼 */}
            <div className="bg-white rounded-lg shadow-md p-6">
                <h2 className="text-xl font-semibold mb-4">결제 정보</h2>
                <form className="space-y-4" action={handlePayment}>
                    {/* 수령인 정보 */}
                    <div>
                        <label htmlFor="receiverName" className="block text-sm font-light text-gray-700 mb-1">
                            수령인
                        </label>
                        <input
                            type="text"
                            id="receiverName"
                            name="receiverName"
                            defaultValue={cartData.receiverName} // 기존 데이터로 초기값 설정
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required // 필수 입력
                        />
                    </div>
                    {/* 연락처 정보 */}
                    <div>
                        <label htmlFor="phone" className="block text-sm font-light text-gray-700 mb-1">
                            연락처
                        </label>
                        <input
                            type="text"
                            id="phone"
                            name="phone"
                            defaultValue={cartData.phone} // 기존 데이터로 초기값 설정
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>
                    {/* 배송지 정보 */}
                    <div>
                        <label htmlFor="deliveryAddress" className="block text-sm font-light text-gray-700 mb-1">
                            배송지
                        </label>
                        <input
                            type="text"
                            id="deliveryAddress"
                            name="deliveryAddress"
                            defaultValue={cartData.deliveryAddress} // 기존 데이터로 초기값 설정
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            required
                        />
                    </div>

                    {/* 카드 번호 입력 (민감 정보이므로 실제 서비스에선 PG사 SDK 사용 권장) */}
                    <div>
                        <label htmlFor="cardNumber" className="block text-sm font-light text-gray-700 mb-1">
                            카드 번호
                        </label>
                        <input
                            type="text"
                            id="cardNumber"
                            name="cardNumber"
                            placeholder="1234-5678-1234-5678"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            maxLength={19}
                            // required // 실제 결제 처리 시 필수
                        />
                    </div>

                    {/* 카드 유효기간 */}
                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label htmlFor="expiryMonth" className="block text-sm font-light text-gray-700 mb-1">
                                유효기간 (월)
                            </label>
                            <input
                                type="text"
                                id="expiryMonth"
                                name="expiryMonth"
                                placeholder="MM"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                maxLength={2}
                                // required
                            />
                        </div>
                        <div>
                            <label htmlFor="expiryYear" className="block text-sm font-light text-gray-700 mb-1">
                                유효기간 (년)
                            </label>
                            <input
                                type="text"
                                id="expiryYear"
                                name="expiryYear"
                                placeholder="YY"
                                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                                maxLength={2}
                                // required
                            />
                        </div>
                    </div>

                    {/* CVC */}
                    <div>
                        <label htmlFor="cvc" className="block text-sm font-light text-gray-700 mb-1">
                            CVC
                        </label>
                        <input
                            type="text"
                            id="cvc"
                            name="cvc"
                            placeholder="123"
                            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                            maxLength={3}
                            // required
                        />
                    </div>

                    {/* 결제 수단 (현재는 'CARD' 고정) */}
                    <input type="hidden" name="paymentMethod" value="CARD" />

                    {/* 결제 금액은 ReadOnly로 표시, 폼 데이터로 보내지 않아도 됨 (백엔드에서 계산) */}
                    <div className="border-t pt-4 mt-4">
                        <div className="flex justify-between items-center text-lg font-semibold">
                            <span>결제 금액</span>
                            <span>{cartData.totalPrice?.toLocaleString()}원</span>
                        </div>
                    </div>

                    {/* 결제 버튼 */}
                    <button
                        type="submit"
                        className="w-full bg-blue-600 text-white py-3 px-4 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                    >
                        결제하기
                    </button>
                </form>
            </div>
        </div>
    );
}

export default CartPaymentPage;