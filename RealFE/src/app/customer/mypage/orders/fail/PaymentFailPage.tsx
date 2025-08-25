'use client';

import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';

export default function PaymentFailPage() {
    const searchParams = useSearchParams();
    const router = useRouter();
    
    const code = searchParams.get('code');
    const message = searchParams.get('message');
    const orderId = searchParams.get('orderId');

    const getErrorMessage = (code: string | null) => {
        switch (code) {
            case 'PAY_PROCESS_CANCELED':
                return '결제가 취소되었습니다.';
            case 'PAY_PROCESS_ABORTED':
                return '결제가 중단되었습니다.';
            case 'INVALID_CARD':
                return '유효하지 않은 카드입니다.';
            case 'INSUFFICIENT_FUNDS':
                return '잔액이 부족합니다.';
            case 'CARD_EXPIRED':
                return '만료된 카드입니다.';
            default:
                return message || '결제에 실패했습니다.';
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="text-center max-w-md mx-auto p-6">
                <div className="text-red-500 text-6xl mb-4">❌</div>
                <h1 className="text-2xl font-light mb-4">결제에 실패했습니다</h1>
                <p className="text-gray-600 mb-6">
                    {getErrorMessage(code)}
                </p>
                {orderId && (
                    <p className="text-sm text-gray-500 mb-6">
                        주문번호: {orderId}
                    </p>
                )}
                <div className="space-y-3">
                    <button 
                        onClick={() => router.back()}
                        className="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700"
                    >
                        다시 시도하기
                    </button>
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