// src/app/customer/orders/[orderId]/page.tsx
// 이 파일은 서버 컴포넌트입니다.

import OrderDetailClient from './orderDetailClient'; // 클라이언트 컴포넌트 임포트

interface PageProps {
    params: {
        orderId: string; // URL 파라미터는 항상 string입니다.
    };
}

export default async function OrdersDetailPage({ params }: PageProps) {
    const orderId = params.orderId; // URL 파라미터에서 orderId (string) 추출

    // 파라미터에 대한 기본적인 유효성 검사 (서버에서 미리 처리)
    if (isNaN(Number(orderId)) || Number(orderId) <= 0) {
        return (
            <div className="container mx-auto p-4 text-center text-red-600 font-inter">
                <h1 className="text-3xl font-bold mb-6">오류</h1>
                <p>유효하지 않은 주문 ID입니다.</p>
            </div>
        );
    }

    // 서버 컴포넌트에서는 orderId (string)를 그대로 클라이언트 컴포넌트에 전달합니다.
    return <OrderDetailClient orderId={orderId} />;
}