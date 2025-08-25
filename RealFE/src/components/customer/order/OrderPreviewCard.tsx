'use client';

import { Order } from '@/types/customer/order/order';
import { useRouter } from 'next/navigation';

interface Props {
    order: Order;
}

export default function OrderPreviewCard({ order }: Props) {
    const router = useRouter();

    return (
        <div
            className="bg-white rounded-lg px-4 py-2 shadow-sm hover:shadow cursor-pointer flex items-center justify-between"
            onClick={() => router.push(`/customer/mypage/orders/${order.orderId}`)}
        >
            <div className="text-xs text-gray-600">주문번호: {order.orderId}</div>
            <div className="text-sm font-light text-blue-600">{order.deliveryStatus}</div>
        </div>
    );
}
