// src/app/customer/member/auctions/won/[auctionId]/payment/page.tsx
'use client';

import {
  useEffect,
  useState,
  useMemo,
  useRef,
  Suspense,
  useCallback,
} from 'react';
import {
  useRouter,
  useParams,
  useSearchParams,
  usePathname,
} from 'next/navigation';
import Link from 'next/link';
import Script from 'next/script';

import useRequireAuth from '@/hooks/useRequireAuth';
import { useGlobalDialog } from '@/app/context/dialogContext';
import { AuctionWinInfo, UserProfile, AuctionPaymentRequestDTO } from '@/types/customer/auctions'; // types

import AddressInput, { parseAddress } from '@/components/customer/join/AddressInput';
import ConfirmDialog from '@/components/ui/ConfirmDialog';
import { approvePayment, fetchAuctionWinInfo, fetchMyProfile } from '@/service/customer/auctionService'; // API 함수들


// ───────── 경매 결제 컴포넌트 ─────────
function AuctionPaymentComponent() {
  const router           = useRouter();
  const { auctionId }    = useParams<{ auctionId: string }>();
  const { show }         = useGlobalDialog();

  const [auctionInfo, setAuctionInfo] = useState<AuctionWinInfo | null>(null);
  const [userProfile, setUserProfile] = useState<UserProfile | null>(null);
  const [shippingInfo, setShippingInfo] = useState({
    receiverName: '',
    phone: '',
    address: '',
  });
  const [loading, setLoading] = useState(true);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const tossRef = useRef<any>(null);          // TossPayments 객체

  /* 데이터 로드 */
  useEffect(() => {
    if (!auctionId) return;
    (async () => {
      try {
        setLoading(true);
        const [win, profile] = await Promise.all([
          fetchAuctionWinInfo(auctionId),
          fetchMyProfile(),
        ]);

        if (win.isPaid) {
          await show('이미 결제가 완료된 상품입니다.');
          router.replace('/customer/member/auctions/won');
          return;
        }

        setAuctionInfo(win);
        setUserProfile(profile);
        setShippingInfo({
          receiverName: profile.receiverName,
          phone: profile.phone,
          address: profile.deliveryAddress,
        });
      } catch (e) {
        console.error(e);
        await show('결제 정보를 불러오지 못했습니다.');
        router.replace('/customer/member/auctions/won');
      } finally {
        setLoading(false);
      }
    })();
  }, [auctionId, router, show]);

  /* 금액 계산 */
  const { deliveryFee, finalAmount } = useMemo(() => {
    if (!auctionInfo) return { deliveryFee: 0, finalAmount: 0 };
    const fee = 3000;
    return { deliveryFee: fee, finalAmount: auctionInfo.winningBidPrice + fee };
  }, [auctionInfo]);

  /* TossPayments 초기화 : Wrapper 에서 SDK 로딩 완료 후 이 컴포넌트가 나타남 */
  useEffect(() => {
    if (!auctionInfo || !userProfile) return;
    const TP = (window as any).TossPayments;
    if (!TP || tossRef.current) return;           // 아직 또는 이미 생성
    tossRef.current = TP(process.env.NEXT_PUBLIC_TOSS_CLIENT_KEY!);
  }, [auctionInfo, userProfile]);

  /* 입력 핸들러 */
  const onBasicChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) =>
      setShippingInfo((p) => ({ ...p, [e.target.name]: e.target.value })),
    [],
  );
  const onAddressChange = useCallback(
    (full: string) => setShippingInfo((p) => ({ ...p, address: full })),
    [],
  );
  const addrObj = useMemo(() => parseAddress(shippingInfo.address), [shippingInfo.address]);

  /* 결제 요청 */
  const requestPayment = async () => {
    if (!tossRef.current) {
      show('결제 시스템이 준비되지 않았습니다.');
      return;
    }
    sessionStorage.setItem(
      `checkout_auction_${auctionId}`,
      JSON.stringify({ type: 'AUCTION', auctionId: auctionInfo!.auctionId, shippingInfo }),
    );

    try {
      await tossRef.current.requestPayment('카드', {
        amount: finalAmount,
        orderId: `auction_${auctionId}_${Date.now()}`,
        orderName: `${auctionInfo!.productName} 경매 낙찰`,
        customerName: shippingInfo.receiverName,
        customerEmail: userProfile!.email,
        successUrl: `${location.origin}/customer/member/auctions/won/${auctionId}/payment/success`,
        failUrl:    `${location.origin}/customer/member/auctions/won/${auctionId}/payment/fail`,
      });
    } catch (e) {
      console.error(e);
      show('결제 요청 중 오류가 발생했습니다.');
    }
  };

  const payClick = () => {
    if (!shippingInfo.receiverName || !shippingInfo.phone || !shippingInfo.address) {
      show('배송지 정보를 모두 입력해 주세요.');
      return;
    }
    setConfirmOpen(true);
  };

  if (loading)        return <div className="py-20 text-center">주문 정보를 준비 중입니다...</div>;
  if (!auctionInfo)   return <div className="py-20 text-center">결제할 상품 정보가 없습니다.</div>;

  return (
    <>
      <ConfirmDialog
        open={confirmOpen}
        message={`${finalAmount.toLocaleString()}원을 결제하시겠습니까?`}
        onResolve={(ok) => {
          setConfirmOpen(false);
          if (ok) requestPayment();
        }}
      />

      <div className="min-h-screen bg-white">
        <main className="mx-auto max-w-4xl px-4 py-8">

          {/* 배송지 */}
          <section className="mb-6 rounded-lg border border-gray-400 boder-gray-400 bg-white p-6">
            <h2 className="mb-4 text-lg font-light">배송지</h2>
            <div className="space-y-3">
              <input
                name="receiverName"
                value={shippingInfo.receiverName}
                onChange={onBasicChange}
                placeholder="받는 분"
                className="w-full rounded border border-gray-400 p-2 text-sm"
              />
              <input
                name="phone"
                value={shippingInfo.phone}
                onChange={onBasicChange}
                placeholder="연락처"
                className="w-full rounded border border-gray-400 p-2 text-sm"
              />
              <AddressInput onAddressChange={onAddressChange} defaultAddress={addrObj} />
            </div>
          </section>

          {/* 주문 상품 */}
          <section className="mb-6 rounded-lg border border-gray-400 bg-white p-6">
            <h2 className="mb-4 text-lg font-light">주문 상품</h2>
            <div className="flex items-start gap-4">
              <img
                src={auctionInfo.productImageUrl || '/images/default-product.png'}
                alt={auctionInfo.productName}
                className="h-20 w-20 rounded object-cover"
              />
              <div className="flex-grow">
                <p>{auctionInfo.productName}</p>
              </div>
              <p className="font-semibold">
                {auctionInfo.winningBidPrice.toLocaleString()}원
              </p>
            </div>
          </section>

          {/* 결제 금액 */}
          <section className="rounded-lg border border-gray-400 bg-white p-6">
            <h3 className="mb-4 text-lg font-light">결제 금액</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between">
                <span>낙찰가</span>
                <span>{auctionInfo.winningBidPrice.toLocaleString()}원</span>
              </div>
              <div className="flex justify-between">
                <span>배송비</span>
                <span>+ {deliveryFee.toLocaleString()}원</span>
              </div>
              <hr className="my-2" />
              <div className="flex justify-between text-base font-semibold">
                <span>최종 결제 금액</span>
                <span>{finalAmount.toLocaleString()}원</span>
              </div>
            </div>
          </section>
          <button
              onClick={payClick}
              className="mt-4 w-full rounded-none  bg-black py-3 text-white hover:bg-neutral-800"
          >
            {finalAmount.toLocaleString()}원 결제하기
          </button>
        </main>
      </div>
    </>
  );
}

/* ───────── 결과 컴포넌트 ───────── */
function PaymentResultHandler() {
  const { auctionId } = useParams<{ auctionId: string }>();
  const search        = useSearchParams();
  const [state, setState] = useState<'PROCESSING' | 'SUCCESS' | 'ERROR'>('PROCESSING');
  const [msg,   setMsg]   = useState('결제 승인 중입니다...');

  useEffect(() => {
    const paymentKey = search.get('paymentKey');
    const orderId    = search.get('orderId');
    const amount     = Number(search.get('amount'));

    if (!paymentKey || !orderId || !amount) {
      setState('ERROR'); setMsg('결제 정보가 올바르지 않습니다.'); return;
    }
    const stored = sessionStorage.getItem(`checkout_auction_${auctionId}`);
    if (!stored) {
      setState('ERROR'); setMsg('결제 세션이 만료되었습니다.'); return;
    }
    const { shippingInfo } = JSON.parse(stored);

    (async () => {
      try {
        const dto: AuctionPaymentRequestDTO = {
          auctionId: Number(auctionId),
          paymentKey,
          amount,
        };
        const res = await approvePayment(auctionId, dto);

        if (res.success) {
          setState('SUCCESS');
          setMsg('결제가 성공적으로 완료되었습니다!');
          sessionStorage.removeItem(`checkout_auction_${auctionId}`);
        } else {
          throw new Error(res.error?.message || '승인 실패');
        }
      } catch (e: any) {
        setState('ERROR');
        setMsg(e.response?.data?.error?.message || e.message);
      }
    })();
  }, [auctionId, search]);

  return (
    <div className="py-20 text-center">
      <h2
        className={`text-2xl font-semibold ${
          state === 'SUCCESS'
            ? 'text-green-600'
            : state === 'ERROR'
            ? 'text-red-500'
            : 'text-gray-700'
        }`}
      >
        {state === 'SUCCESS'
          ? '결제 완료'
          : state === 'ERROR'
          ? '결제 실패'
          : '결제 처리 중'}
      </h2>
      <p className="mt-2 text-gray-600">{msg}</p>
      <div className="mt-6">
        {state === 'SUCCESS' && (
          <Link href="/customer/mypage/orders">
            <button className="rounded bg-blue-600 px-6 py-2 text-white">
              주문 내역 확인
            </button>
          </Link>
        )}
        {state === 'ERROR' && (
          <Link href={`/customer/member/auctions/won/${auctionId}/payment`}>
            <button className="rounded bg-gray-600 px-6 py-2 text-white">
              결제 다시 시도
            </button>
          </Link>
        )}
      </div>
    </div>
  );
}

/* ───────── Wrapper ───────── */
export default function AuctionPaymentPageWrapper() {
  const { isAuthenticated, isLoading } = useRequireAuth();
  const pathname   = usePathname();
  const [sdkReady, setSdkReady] = useState(false);

  if (isLoading) return <div className="py-20 text-center">인증 확인 중...</div>;
  if (!isAuthenticated) return null;

  const isResult = pathname.endsWith('/success') || pathname.endsWith('/fail');

  return (
    <>
      <Script
        src="https://js.tosspayments.com/v1/payment"
        strategy="afterInteractive"
        onReady={() => setSdkReady(true)}
      />
      <Suspense fallback={<div className="py-20 text-center">로딩 중...</div>}>
        {isResult ? (
          <PaymentResultHandler />
        ) : sdkReady ? (
          <AuctionPaymentComponent />
        ) : (
          <div className="py-20 text-center">결제 모듈 로딩 중...</div>
        )}
      </Suspense>
    </>
  );
}
