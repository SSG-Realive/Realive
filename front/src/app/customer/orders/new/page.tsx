// 주문 생성
'use client';

import { useState } from 'react';
import { createOrder } from '@/service/orderService';
import { useRouter } from 'next/navigation';

export default function OrderCreatePage() {
    const router = useRouter();
    const [productId, setProductId] = useState('');
    const [quantity, setQuantity] = useState(1);
    const [deliveryAddress, setDeliveryAddress] = useState('');

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await createOrder({
                productId: Number(productId),
                quantity,
                deliveryAddress,
            });
            alert('주문이 완료되었습니다.');
            router.push('/orders');
        } catch (err) {
            console.error('주문 실패', err);
            alert('주문 중 오류가 발생했습니다.');
        }
    };

    return (
        <div className="p-6 max-w-lg mx-auto">
            <h1 className="text-2xl font-bold mb-4">주문 생성</h1>
            <form onSubmit={handleSubmit} className="space-y-4">
                <input
                    type="number"
                    placeholder="상품 ID"
                    value={productId}
                    onChange={(e) => setProductId(e.target.value)}
                    className="w-full border p-2"
                    required
                />
                <input
                    type="number"
                    placeholder="수량"
                    value={quantity}
                    onChange={(e) => setQuantity(Number(e.target.value))}
                    className="w-full border p-2"
                    required
                />
                <input
                    type="text"
                    placeholder="배송 주소"
                    value={deliveryAddress}
                    onChange={(e) => setDeliveryAddress(e.target.value)}
                    className="w-full border p-2"
                    required
                />
                <button className="bg-blue-600 text-white px-4 py-2 rounded" type="submit">
                    주문하기
                </button>
            </form>
        </div>
    );
}