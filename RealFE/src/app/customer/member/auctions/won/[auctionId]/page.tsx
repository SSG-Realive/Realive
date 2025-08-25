'use client';

import React, { useState, useEffect } from 'react';
import { useRouter, useParams } from 'next/navigation';
import useRequireAuth from '@/hooks/useRequireAuth';
import { customerApi } from '@/lib/apiClient';

interface AdminProductDTO {
  imageThumbnailUrl?: string;
  productName?: string;
  productDescription?: string;
}
interface AuctionWinnerDetail {
  startPrice: number;
  finalPrice: number;
  bidTime: string;
  paymentDue: string;
  adminProduct: AdminProductDTO;
  paid?: boolean;
  paymentStatus?: string;
}

const useCountdown = (targetDate?: string | null) => {
  const countDownDate = targetDate ? new Date(targetDate).getTime() : new Date().getTime() - 1;
  const [countDown, setCountDown] = useState(countDownDate - new Date().getTime());

  useEffect(() => {
    if (!targetDate) {
      setCountDown(-1);
      return;
    }
    const interval = setInterval(() => {
      setCountDown(new Date(targetDate).getTime() - new Date().getTime());
    }, 1000);
    return () => clearInterval(interval);
  }, [targetDate]);

  const isExpired = countDown < 0;
  const days = Math.floor(countDown / (1000 * 60 * 60 * 24));
  const hours = Math.floor((countDown % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  const minutes = Math.floor((countDown % (1000 * 60 * 60)) / (1000 * 60));

  return { days, hours, minutes, isExpired };
};

function AuctionDetailSkeleton() {
  return (
      <div className="bg-gray-50 min-h-screen flex flex-col">
        <main className="flex-grow">
          <div className="max-w-5xl mx-auto px-4 md:px-6 py-8 animate-pulse">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 lg:gap-12">
              <div className="bg-gray-200 rounded-xl w-full h-80 md:h-96"></div>
              <div className="space-y-6">
                <div className="h-8 bg-gray-200 rounded-lg w-3/4"></div>
                <div className="h-8 bg-gray-200 rounded-lg w-1/2"></div>
                <div className="space-y-4">
                  <div className="h-24 bg-gray-200 rounded-xl"></div>
                  <div className="h-20 bg-gray-200 rounded-xl"></div>
                </div>
                <div className="h-14 bg-gray-200 rounded-lg mt-auto"></div>
              </div>
            </div>
          </div>
        </main>
      </div>
  );
}

export default function WonAuctionDetailPage() {
  const router = useRouter();
  const params = useParams();
  const auctionId = params.auctionId as string;
  const { isLoading, isAuthenticated } = useRequireAuth();

  const [data, setData] = useState<AuctionWinnerDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  const { days, hours, minutes, isExpired } = useCountdown(data?.paymentDue);

  useEffect(() => {
    if (!auctionId || !isAuthenticated) return;
    customerApi
        .get<AuctionWinnerDetail>(`/customer/auctions/${auctionId}/winner-detail`)
        .then((res) => {
          console.log('API 응답:', res.data);
          setData(res.data);
        })
        .catch((err) => {
          console.error('API 오류:', err);
          const msg = err.response?.data?.message || '데이터를 불러오지 못했습니다.';
          setError(msg);
        });
  }, [auctionId, isAuthenticated]);

  const handlePay = () => router.push(`/customer/member/auctions/won/${auctionId}/payment`);

  if (isLoading || (!data && !error)) return <AuctionDetailSkeleton />;
  if (!isAuthenticated) return null;
  if (error)
    return <div className="py-16 text-center text-red-600 text-sm">{error}</div>;
  if (!data)
    return <div className="py-16 text-center text-gray-500 text-sm">데이터가 없습니다.</div>;

  const { adminProduct, finalPrice } = data;

  return (
      <div className="bg-white-50 text-sm">
        <main className="flex-grow">
          <div className="max-w-5xl mx-auto px-4 md:px-6 py-8">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-8 lg:gap-12 items-center">
              <div className="w-full">
                <img
                    src={adminProduct.imageThumbnailUrl || '/images/placeholder.png'}
                    alt={adminProduct.productName || '경매 상품 이미지'}
                    className="w-full h-auto max-h-[500px] object-contain rounded-xl shadow-lg bg-white p-2"
                />
              </div>

              <div className="flex flex-col space-y-6 md:mt-16">
                <h1 className="text-base font-light text-gray-800">{adminProduct.productName}</h1>

                {adminProduct.productDescription && (
                    <div className="bg-white rounded-xl p-4 shadow-sm">
                      <h2 className="text-sm font-light mb-2 text-gray-700">상품 설명</h2>
                      <p className="text-gray-600 whitespace-pre-wrap">
                        {adminProduct.productDescription}
                      </p>
                    </div>
                )}

                <div className="bg-white rounded-xl p-4 shadow-sm space-y-4">
                  <h2 className="text-sm font-light text-gray-700">낙찰 정보</h2>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-600">최종 낙찰가</span>
                    <span className="text-base font-semibold text-black">
                    {finalPrice.toLocaleString()}원
                  </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-gray-600">결제 상태</span>
                    {data.paid ? (
                        <span className="font-light text-green-600">결제 완료</span>
                    ) : isExpired ? (
                        <span className="font-light text-gray-500">마감되었습니다</span>
                    ) : (
                        <span className="font-light text-red-500 animate-pulse">
                      결제 마감까지 {days}일 {hours}시간 {minutes}분 남음
                    </span>
                    )}
                  </div>
                </div>

                <div className="mt-auto pt-6">
                  {data.paid ? (
                    <div className="w-full py-3 bg-green-600 text-white rounded-none text-sm font-medium text-center">
                      결제 완료
                    </div>
                  ) : (
                    <button
                        onClick={handlePay}
                        disabled={isExpired}
                        className="w-full py-3 bg-black text-white rounded-none text-sm font-medium hover:bg-neutral-800 transition-colors disabled:bg-gray-400 disabled:cursor-not-allowed"
                    >
                      {isExpired ? '결제 기간 만료' : '결제하기'}
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>
        </main>
      </div>
  );
}