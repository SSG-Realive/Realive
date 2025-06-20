'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { loadTossPayments } from '@tosspayments/payment-sdk';

interface AuctionWin {
  auctionId: number;
  productName: string;
  productImageUrl: string | null;
  winningBidPrice: number;
  auctionEndTime: string;
  paymentDeadline: string;
  isPaid: boolean;
  paymentStatus: string;
  isNewWin: boolean;
  winMessage: string;
}

interface PaymentForm {
  receiverName: string;
  phone: string;
  deliveryAddress: string;
  paymentMethod: '카드' | '휴대폰' | '가상계좌';
}

export default function AuctionPaymentPage() {
  const params = useParams();
  const router = useRouter();
  const auctionId = params.auctionId as string;
  
  const [auctionWin, setAuctionWin] = useState<AuctionWin | null>(null);
  const [loading, setLoading] = useState(true);
  const [paymentLoading, setPaymentLoading] = useState(false);
  const [form, setForm] = useState<PaymentForm>({
    receiverName: '',
    phone: '',
    deliveryAddress: '',
    paymentMethod: '카드',
  });

  // 결제 수단을 백엔드 enum으로 변환하는 함수
  const getPaymentMethodForBackend = (frontendMethod: string): 'CARD' | 'CELL_PHONE' | 'ACCOUNT' => {
    switch (frontendMethod) {
      case '카드':
        return 'CARD';
      case '휴대폰':
        return 'CELL_PHONE';
      case '가상계좌':
        return 'ACCOUNT';
      default:
        return 'CARD';
    }
  };

  // 토스페이먼츠 결제 수단으로 변환하는 함수
  const getTossPaymentMethod = (frontendMethod: string): '카드' | '휴대폰' | '가상계좌' => {
    return frontendMethod as '카드' | '휴대폰' | '가상계좌';
  };

  useEffect(() => {
    fetchAuctionWinInfo();
  }, [auctionId]);

  const fetchAuctionWinInfo = async () => {
    try {
      setLoading(true);
      const response = await fetch(`/api/customer/auction-wins/${auctionId}`, {
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error('낙찰 정보를 불러오는데 실패했습니다.');
      }

      const data = await response.json();
      setAuctionWin(data.data);
    } catch (error) {
      console.error('낙찰 정보 조회 오류:', error);
      alert('낙찰 정보를 불러오는데 실패했습니다.');
      router.push('/customer/member/auctions/won');
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target;
    setForm(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const validateForm = () => {
    if (!form.receiverName.trim()) {
      alert('수령인 이름을 입력해주세요.');
      return false;
    }
    if (!form.phone.trim()) {
      alert('연락처를 입력해주세요.');
      return false;
    }
    if (!form.deliveryAddress.trim()) {
      alert('배송 주소를 입력해주세요.');
      return false;
    }
    return true;
  };

  const handlePayment = async () => {
    if (!validateForm()) return;
    if (!auctionWin) return;

    try {
      setPaymentLoading(true);

      // 토스페이먼츠 결제 위젯 초기화
      const tossPayments = await loadTossPayments(process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY!);

      // 주문 ID 생성 (현재 시간 기반)
      const orderId = `auction_${auctionId}_${Date.now()}`;

      // 결제 요청
      await tossPayments.requestPayment(getTossPaymentMethod(form.paymentMethod), {
        amount: auctionWin.winningBidPrice,
        orderId: orderId,
        orderName: `${auctionWin.productName} 경매 낙찰`,
        customerName: form.receiverName,
        customerEmail: 'customer@example.com', // 실제로는 고객 이메일을 가져와야 함
        successUrl: `${window.location.origin}/customer/member/auctions/won/${auctionId}/payment/success`,
        failUrl: `${window.location.origin}/customer/member/auctions/won/${auctionId}/payment/fail`,
      });

    } catch (error: any) {
      console.error('결제 요청 오류:', error);
      
      // 결제 승인 처리
      if (error.paymentKey && error.orderId && error.amount) {
        await processPaymentApproval(error.paymentKey, error.orderId, error.amount);
      } else {
        alert('결제 처리 중 오류가 발생했습니다.');
      }
    } finally {
      setPaymentLoading(false);
    }
  };

  const processPaymentApproval = async (paymentKey: string, orderId: string, amount: number) => {
    try {
      const response = await fetch(`/api/customer/auction-wins/${auctionId}/payment`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include',
        body: JSON.stringify({
          auctionId: parseInt(auctionId),
          paymentKey,
          tossOrderId: orderId,
          paymentMethod: getPaymentMethodForBackend(form.paymentMethod),
          receiverName: form.receiverName,
          phone: form.phone,
          deliveryAddress: form.deliveryAddress,
        }),
      });

      if (!response.ok) {
        throw new Error('결제 승인 처리에 실패했습니다.');
      }

      const result = await response.json();
      alert('결제가 성공적으로 완료되었습니다!');
      router.push('/customer/member/auctions/won');
      
    } catch (error) {
      console.error('결제 승인 처리 오류:', error);
      alert('결제 승인 처리 중 오류가 발생했습니다.');
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const formatPrice = (price: number) => {
    return price.toLocaleString() + '원';
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 p-8">
        <div className="max-w-4xl mx-auto">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
            <p className="mt-4 text-gray-600">낙찰 정보를 불러오는 중...</p>
          </div>
        </div>
      </div>
    );
  }

  if (!auctionWin) {
    return (
      <div className="min-h-screen bg-gray-50 p-8">
        <div className="max-w-4xl mx-auto">
          <div className="text-center">
            <h2 className="text-2xl font-bold text-gray-900 mb-4">낙찰 정보를 찾을 수 없습니다</h2>
            <button
              onClick={() => router.push('/customer/member/auctions/won')}
              className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors"
            >
              낙찰 목록으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    );
  }

  if (auctionWin.isPaid) {
    return (
      <div className="min-h-screen bg-gray-50 p-8">
        <div className="max-w-4xl mx-auto">
          <div className="text-center">
            <div className="text-green-500 text-6xl mb-4">✅</div>
            <h2 className="text-2xl font-bold text-gray-900 mb-4">이미 결제가 완료된 상품입니다</h2>
            <p className="text-gray-600 mb-6">해당 경매 상품은 이미 결제가 완료되었습니다.</p>
            <button
              onClick={() => router.push('/customer/member/auctions/won')}
              className="bg-blue-600 text-white px-6 py-3 rounded-lg hover:bg-blue-700 transition-colors"
            >
              낙찰 목록으로 돌아가기
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">경매 상품 결제</h1>
          <p className="text-gray-600">낙찰한 상품의 결제를 진행해주세요.</p>
        </div>

        {/* 낙찰 알림 */}
        {auctionWin?.isNewWin && (
          <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-lg">
            <div className="flex items-center">
              <div className="text-green-600 text-2xl mr-3">🎉</div>
              <div>
                <h3 className="text-lg font-semibold text-green-900 mb-1">축하합니다!</h3>
                <p className="text-green-800">{auctionWin.winMessage}</p>
              </div>
            </div>
          </div>
        )}

        <div className="grid gap-8 lg:grid-cols-2">
          {/* 상품 정보 */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">상품 정보</h2>
            
            <div className="aspect-w-16 aspect-h-9 bg-gray-200 rounded-lg mb-4">
              {auctionWin.productImageUrl ? (
                <img
                  src={auctionWin.productImageUrl}
                  alt={auctionWin.productName}
                  className="w-full h-48 object-cover rounded-lg"
                />
              ) : (
                <div className="w-full h-48 flex items-center justify-center text-gray-400 rounded-lg">
                  이미지 없음
                </div>
              )}
            </div>
            
            <h3 className="text-lg font-semibold text-gray-900 mb-4">{auctionWin.productName}</h3>
            
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-gray-600">낙찰가</span>
                <span className="text-xl font-bold text-blue-600">
                  {formatPrice(auctionWin.winningBidPrice)}
                </span>
              </div>
              
              <div className="flex justify-between items-center">
                <span className="text-gray-600">경매 종료</span>
                <span className="text-gray-900">{formatDate(auctionWin.auctionEndTime)}</span>
              </div>
              
              <div className="flex justify-between items-center">
                <span className="text-gray-600">결제 마감</span>
                <span className="text-gray-900">{formatDate(auctionWin.paymentDeadline)}</span>
              </div>
            </div>
          </div>

          {/* 결제 폼 */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-semibold text-gray-900 mb-4">결제 정보</h2>
            
            <form className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  수령인 이름 *
                </label>
                <input
                  type="text"
                  name="receiverName"
                  value={form.receiverName}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="수령인 이름을 입력하세요"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  연락처 *
                </label>
                <input
                  type="tel"
                  name="phone"
                  value={form.phone}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="010-0000-0000"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  배송 주소 *
                </label>
                <textarea
                  name="deliveryAddress"
                  value={form.deliveryAddress}
                  onChange={handleInputChange}
                  rows={3}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="배송 주소를 입력하세요"
                  required
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  결제 수단 *
                </label>
                <select
                  name="paymentMethod"
                  value={form.paymentMethod}
                  onChange={handleInputChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="카드">신용/체크카드</option>
                  <option value="휴대폰">휴대폰</option>
                  <option value="가상계좌">계좌입금/무통장입금</option>
                </select>
              </div>

              <div className="pt-4">
                <button
                  type="button"
                  onClick={handlePayment}
                  disabled={paymentLoading}
                  className="w-full bg-blue-600 text-white py-3 px-4 rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {paymentLoading ? '결제 처리 중...' : `${formatPrice(auctionWin.winningBidPrice)} 결제하기`}
                </button>
              </div>
            </form>
          </div>
        </div>

        <div className="mt-8 text-center">
          <button
            onClick={() => router.push('/customer/member/auctions/won')}
            className="text-gray-600 hover:text-gray-800 transition-colors"
          >
            ← 낙찰 목록으로 돌아가기
          </button>
        </div>
      </div>
    </div>
  );
} 