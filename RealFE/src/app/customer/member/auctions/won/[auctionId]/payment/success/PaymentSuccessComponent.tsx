// src/app/customer/member/auctions/won/[auctionId]/payment/success/page.tsx
'use client';

import { useEffect, useState, Suspense } from 'react';
import { useRouter, useSearchParams, useParams } from 'next/navigation';
import Link from 'next/link';
import { customerApi } from '@/lib/apiClient';

// 백엔드의 AuctionPaymentRequestDTO와 일치하는 타입
interface AuctionPaymentRequestDTO {
    auctionId: number;
    paymentKey: string;
    amount: number; // 백엔드에서는 Long이지만 프론트엔드에서는 number로 처리
}

// 백엔드 ApiResponse 구조에 맞는 타입
interface ApiResponse<T> {
    status: number;
    message: string;
    data: T;
    timestamp: string;
}

// ✅ [수정] 경매 전용 결제 승인 API 호출 함수
const processAuctionPaymentApi = async (auctionId: string, data: AuctionPaymentRequestDTO) => {
  // customerApi를 사용하여 올바른 엔드포인트로 POST 요청을 보냅니다.
  const response = await customerApi.post(`/customer/auction-wins/${auctionId}/payment`, data);
  return response.data;
};


function PaymentSuccessComponent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const params = useParams();
  const auctionId = params.auctionId as string;

  const [status, setStatus] = useState<'PROCESSING' | 'SUCCESS' | 'ERROR'>('PROCESSING');
  const [message, setMessage] = useState('결제 승인 중입니다...');
  const [orderId, setOrderId] = useState<number | null>(null);

  useEffect(() => {
    // URL 파라미터가 없으면 실행하지 않음
    if (!searchParams.has('paymentKey')) return;

    const approvePayment = async () => {
      const paymentKey = searchParams.get('paymentKey');
      const tossOrderId = searchParams.get('orderId');
      const amount = Number(searchParams.get('amount'));

      if (!paymentKey || !tossOrderId || !amount) {
        setStatus('ERROR');
        setMessage('결제 정보가 올바르지 않습니다.');
        return;
      }

      // 결제 페이지에서 저장했던 배송지 정보를 가져옵니다.
      const checkoutInfoStr = sessionStorage.getItem(`checkout_auction_${auctionId}`);
      if (!checkoutInfoStr) {
        setStatus('ERROR');
        setMessage('결제 세션이 만료되었습니다. 다시 시도해주세요.');
        return;
      }
      const checkoutInfo = JSON.parse(checkoutInfoStr);

      try {
        // ✅ [수정] 백엔드 DTO에 맞게 요청 데이터를 정확히 구성합니다.
        const requestData: AuctionPaymentRequestDTO = {
          auctionId: parseInt(auctionId),
          paymentKey,
          amount,
        };
        
        console.log('결제 요청 데이터:', requestData);
        
        // ✅ [수정] 올바른 API 함수를 호출합니다.
        const response: ApiResponse<number> = await processAuctionPaymentApi(auctionId, requestData);

        console.log('결제 응답:', response);

        // ✅ [수정] 백엔드 ApiResponse 구조에 맞게 처리
        if (response.status === 200 || response.status === 201) {
          setOrderId(response.data); // 백엔드에서 반환된 최종 주문 ID
          setStatus('SUCCESS');
          setMessage('결제가 성공적으로 완료되었습니다!');
          sessionStorage.removeItem(`checkout_auction_${auctionId}`); // 성공 후 정보 삭제
        } else {
          // API 응답이 성공(2xx)이지만, status가 200/201이 아닌 경우
          throw new Error(response.message || '결제 승인에 실패했습니다.');
        }
      } catch (err: any) {
        console.error('결제 처리 오류:', err);
        console.error('오류 응답:', err.response);
        // API 호출 자체가 실패한 경우 (5xx, 4xx 에러 등)
        setStatus('ERROR');
        setMessage(err.response?.data?.message || err.message || '결제 최종 승인 중 오류가 발생했습니다.');
      }
    };

    approvePayment();
  }, [auctionId, searchParams, router]);

  // --- UI 렌더링 부분 ---
  if (status === 'PROCESSING') {
    return (
      <div className="text-center py-20">
        <h2 className="text-xl font-light">결제 승인 중...</h2>
        <p className="text-gray-600 mt-2">안전하게 결제를 처리하고 있습니다. 잠시만 기다려주세요.</p>
      </div>
    );
  }

  if (status === 'ERROR') {
    return (
      <div className="text-center py-20">
        <h2 className="text-xl font-light text-red-500">결제 승인 실패</h2>
        <p className="text-gray-600 mt-2">{message}</p>
        <Link href={`/customer/member/auctions/won/${auctionId}/payment`}>
          <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600">
            결제 다시 시도
          </button>
        </Link>
      </div>
    );
  }
  
  return (
    <div className="text-center py-20">
      <h2 className="text-xl font-light text-green-600">결제가 성공적으로 완료되었습니다.</h2>
      <p className="text-gray-600 mt-2">주문 내역은 마이페이지에서 확인하실 수 있습니다.</p>
      <div className="mt-6">
        {orderId && (
          <Link href={`/customer/mypage/orders/${orderId}`}>
            <button className="px-4 py-2 bg-gray-800 text-white rounded hover:bg-black">
              주문 상세 보기
            </button>
          </Link>
        )}
        <Link href="/">
          <button className="ml-4 px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300">
            홈으로 돌아가기
          </button>
        </Link>
      </div>
    </div>
  );
}

// Suspense로 감싸서 useSearchParams 사용 문제를 방지합니다.
export default function AuctionPaymentSuccessPage() {
    return (
        <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
            <PaymentSuccessComponent />
        </Suspense>
    );
}