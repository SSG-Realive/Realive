// src/app/customer/mypage/orders/new/page.tsx (무한 루프 방지 최종안)
'use client';

import { useEffect, useState, useRef, useMemo, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import { useCartStore } from '@/store/customer/useCartStore';
import { fetchMyProfile } from '@/service/customer/customerService';
import { loadTossPayments, DEFAULT_CONFIG } from '@/service/order/tossPaymentService';
import type { MemberReadDTO } from '@/types/customer/member/member';
import useDialog from '@/hooks/useDialog';
import GlobalDialog from '@/components/ui/GlobalDialog';
import AddressInput, { parseAddress } from '@/components/customer/join/AddressInput';
import ConfirmDialog from '@/components/ui/ConfirmDialog';

export default function NewOrderPage() {
  const router = useRouter();
  const { id: customerId, userName: initialName } = useAuthStore();
  const cartItems = useCartStore((s) => s.itemsForCheckout);

  const [userProfile, setUserProfile] = useState<MemberReadDTO | null>(null);
  const [shippingInfo, setShippingInfo] = useState({
    receiverName: '',
    phone: '',
    address: '',
  });
  const tossPaymentsRef = useRef<any>(null);
  const { open, message, handleClose, show } = useDialog();
  const [confirmOpen, setConfirmOpen] = useState(false);

  // ✅ [수정] useCallback으로 감싸서 안정적인 함수로 만듭니다.
  // useEffect의 의존성 배열에 사용될 함수들은 재생성되지 않도록 처리해야 합니다.
  const showStable = useCallback(show, []);
  const routerStable = useRouter(); // next/navigation의 useRouter는 이미 안정적이지만, 명시적으로 분리

  /* ─────── 사용자·장바구니 검증 ─────── */
  useEffect(() => {
    if (cartItems.length === 0) {
      showStable('결제할 상품이 없습니다. 장바구니로 이동합니다.').then(() => {
        routerStable.replace('/customer/cart');
      });
      return;
    }

    fetchMyProfile()
      .then((p) => {
        setUserProfile(p);
        setShippingInfo({
          receiverName: p.name || '',
          phone: p.phone || '',
          address: p.address || '',
        });
      })
      .catch((e) => console.error('프로필 조회 실패:', e));
  }, [cartItems, routerStable, showStable]);

  /* ─────── 최종 금액 계산 ─────── */
  const { totalProductPrice, deliveryFee, finalAmount } = useMemo(() => {
    const productTotal = cartItems.reduce(
      (sum, i) => sum + i.productPrice * i.quantity,
      0,
    );
    const fee = cartItems.length ? 3000 : 0;
    return {
      totalProductPrice: productTotal,
      deliveryFee: fee,
      finalAmount: productTotal + fee,
    };
  }, [cartItems]);

  /* ─────── TossPayments SDK 초기화 ─────── */
  useEffect(() => {
    if (!userProfile || !customerId || !finalAmount) return;
    (async () => {
      try {
        if (!(window as any).TossPayments)
          tossPaymentsRef.current = await loadTossPayments(DEFAULT_CONFIG.CLIENT_KEY);
        else tossPaymentsRef.current = (window as any).TossPayments(DEFAULT_CONFIG.CLIENT_KEY);
      } catch (e) {
        console.error('TossPayments init 실패:', e);
      }
    })();
  }, [userProfile, customerId, finalAmount]);

  /* ─────── 입력 핸들러 (모두 useCallback 적용) ─────── */
  const handleBasicChange = useCallback((e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setShippingInfo((p) => ({ ...p, [name]: value }));
  }, []);

  const handleAddressChange = useCallback((full: string) => {
    setShippingInfo(prev => ({ ...prev, address: full }));
  }, []);

  const defaultAddrObj = useMemo(() => {
    return parseAddress(shippingInfo.address);
  }, [shippingInfo.address]);

  /* ─────── 실제 결제 요청 ─────── */
  const requestPayment = useCallback(async () => {
    const toss = tossPaymentsRef.current;
    if (!toss || !userProfile) {
      show('결제 시스템이 준비되지 않았습니다.');
      return;
    }

    sessionStorage.setItem(
      'checkout_info',
      JSON.stringify({
        orderItems: cartItems.map((i) => ({ productId: i.productId, quantity: i.quantity })),
        shippingInfo,
      }),
    );

    const orderName =
      cartItems.length > 1
        ? `${cartItems[0].productName} 외 ${cartItems.length - 1}건`
        : cartItems[0].productName;

    try {
      await toss.requestPayment('카드', {
        amount: finalAmount,
        orderId: `cart_${Date.now()}`,
        orderName,
        customerName: userProfile.name || initialName || '고객',
        successUrl: `${location.origin}/customer/mypage/orders/success`,
        failUrl: `${location.origin}/customer/mypage/orders/fail`,
      });
    } catch (e) {
      console.error('requestPayment error:', e);
      show('결제에 실패했습니다. 잠시 후 다시 시도해 주세요.');
    }
  }, [cartItems, finalAmount, initialName, shippingInfo, show, userProfile]);
  
  /* ─────── 결제 버튼 클릭 ─────── */
  const handlePaymentClick = useCallback(() => {
    if (!shippingInfo.receiverName || !shippingInfo.phone || !shippingInfo.address) {
      show('배송지 정보를 모두 입력해 주세요.');
      return;
    }
    setConfirmOpen(true);
  }, [shippingInfo, show]);


  /* ─────── 로딩 표시 ─────── */
  if (!userProfile)
    return <div className="flex h-screen items-center justify-center">주문 정보를 준비 중입니다...</div>;

  /* ─────── UI 렌더 ─────── */
  return (
    <>
      <GlobalDialog open={open} message={message} onClose={handleClose} />
      <ConfirmDialog
        open={confirmOpen}
        message={`${finalAmount.toLocaleString()}원을 결제하시겠습니까?`}
        onResolve={(ok) => {
          setConfirmOpen(false);
          if (ok) requestPayment();
        }}
      />

      <div className="min-h-screen bg-white pb-24 lg:pb-0">
        <main className="mx-auto max-w-7xl px-4 py-8 lg:px-8">
          <div className="space-y-6">
            {/* ── 배송지 ── */}
            <section className="rounded-lg bg-white p-6 shadow-sm">
              <h2 className="mb-4 text-lg font-light">배송지</h2>
              <div className="space-y-3">
                <input
                  name="receiverName"
                  value={shippingInfo.receiverName}
                  onChange={handleBasicChange}
                  placeholder="받는 분"
                  className="w-full rounded-md border border-gray-300 p-2 text-sm"
                />
                <input
                  name="phone"
                  value={shippingInfo.phone}
                  onChange={handleBasicChange}
                  placeholder="연락처"
                  className="w-full rounded-md border border-gray-300 p-2 text-sm"
                />
                <AddressInput
                  onAddressChange={handleAddressChange}
                  defaultAddress={defaultAddrObj}
                />
              </div>
            </section>

            {/* ── 주문 상품 ── */}
            <section className="rounded-lg bg-white p-6 shadow-sm">
              <h2 className="mb-4 text-lg font-light">주문 상품</h2>
              <div className="space-y-4">
                {cartItems.map((item) => (
                  <div key={item.cartItemId} className="flex items-start space-x-4">
                    <img
                      src={item.imageThumbnailUrl || '/default-image.png'}
                      alt={item.productName}
                      className="h-20 w-20 flex-shrink-0 rounded-md object-cover"
                    />
                    <div className="flex-grow">
                      <p className="font-light">{item.productName}</p>
                      <p className="text-sm text-gray-500">수량: {item.quantity}</p>
                    </div>
                    <p className="whitespace-nowrap font-light">
                      {(item.productPrice * item.quantity).toLocaleString()}원
                    </p>
                  </div>
                ))}
              </div>
            </section>

            {/* ── 결제 금액 ── */}
            <section className="rounded-lg bg-white p-6 shadow-sm">
              <h3 className="mb-4 text-lg font-light">결제 금액</h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span>총 상품금액</span>
                  <span>{totalProductPrice.toLocaleString()}원</span>
                </div>
                <div className="flex justify-between">
                  <span>배송비</span>
                  <span>+ {deliveryFee.toLocaleString()}원</span>
                </div>
                <hr className="my-2" />
                <div className="flex justify-between text-base font-light">
                  <span>최종 결제 금액</span>
                  <span>{finalAmount.toLocaleString()}원</span>
                </div>
              </div>
              <button
                onClick={handlePaymentClick}
                disabled={cartItems.length === 0}
                className="mt-4 w-full rounded-none bg-black py-3 font-light text-white hover:bg-gray-800 disabled:opacity-50"
              >
                {finalAmount.toLocaleString()}원 결제하기
              </button>
            </section>
          </div>
        </main>
      </div>
    </>
  );
}