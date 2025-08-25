// src/app/customer/member/auctions/won/[auctionId]/payment/fail/page.tsx
'use client';

import { Suspense } from 'react';
import { useSearchParams, useRouter, useParams } from 'next/navigation';
import Link from 'next/link';

function PaymentFailComponent() {
    const searchParams = useSearchParams();
    const router = useRouter();
    const params = useParams();
    const auctionId = params.auctionId as string;
    
    const code = searchParams.get('code');
    const message = searchParams.get('message');

    const getErrorMessage = (code: string | null) => {
        switch (code) {
            case 'PAY_PROCESS_CANCELED':
                return '사용자에 의해 결제가 취소되었습니다.';
            default:
                return message || '알 수 없는 오류로 결제에 실패했습니다.';
        }
    };

    return (
        <div className="min-h-screen flex items-center justify-center">
            <div className="text-center max-w-md mx-auto p-6">
                <div className="text-red-500 text-6xl mb-4">❌</div>
                <h1 className="text-2xl font-light mb-4">결제에 실패했습니다</h1>
                <p className="text-gray-600 mb-6">{getErrorMessage(code)}</p>
                <div className="space-y-3">
                    <button 
                        onClick={() => router.push(`/customer/member/auctions/won/${auctionId}/payment`)}
                        className="w-full bg-blue-600 text-white py-2 px-4 rounded hover:bg-blue-700"
                    >
                        결제 다시 시도하기
                    </button>
                    <Link 
                        href="/main"
                        className="block w-full bg-gray-600 text-white py-2 px-4 rounded hover:bg-gray-700 text-center"
                    >
                        홈으로 돌아가기
                    </Link>
                </div>
            </div>
        </div>
    );
}

export default function AuctionPaymentFailPage() {
    return (
        <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
            <PaymentFailComponent />
        </Suspense>
    );
}
