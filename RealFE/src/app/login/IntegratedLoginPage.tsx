'use client';

import { User, Store } from 'lucide-react';
import Link  from 'next/link';
import { useSearchParams } from 'next/navigation'


export default function IntegratedLoginPage() {
  /* ✅ 바로 사용 */
  const params = useSearchParams();
  const redirectTo = params.get('redirectTo') ?? '';
  const customerLoginUrl =
      `/customer/member/login${redirectTo ? `?redirectTo=${redirectTo}` : ''}`;
  const sellerLoginUrl =
      `/seller/login${redirectTo ? `?redirectTo=${redirectTo}` : ''}`;

  return (
      <main className="relative flex flex-col items-center justify-start px-4 pt-6 pb-4 bg-transparent">


        {/* 배경 패턴 */}
        <div className="absolute inset-0 bg-[url('/images/login-pattern.svg')]
                      bg-cover bg-center opacity-5 pointer-events-none z-0" />

        <div className="z-10 w-full max-w-2xl text-center space-y-10 md:mt-20">
          <div className="space-y-3">
            <p className="text-gray-500 text-sm md:text-base">
              1인 가구를 위한 중고 가구 거래 플랫폼
            </p>
          </div>

          {/* 계정 선택 */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <Link href={customerLoginUrl} className="group">
              <div className="p-8 bg-white rounded-lg border border-gray-300 shadow-sm hover:shadow-xl hover:border-blue-500 transition h-full">
                <div className="flex justify-center mb-4">
                  <User className="h-14 w-14 text-blue-600 group-hover:scale-110 transition" />
                </div>
                <h2 className="text-xl font-semibold text-gray-900">고객</h2>
                <p className="text-sm text-gray-500 mt-2">
                  상품을 탐색하고 구매해보세요
                </p>
              </div>
            </Link>

            <Link href={sellerLoginUrl} className="group">
              <div className="p-8 bg-white rounded-lg border border-gray-300 shadow-sm hover:shadow-xl hover:border-green-500 transition h-full">
                <div className="flex justify-center mb-4">
                  <Store className="h-14 w-14 text-green-600 group-hover:scale-110 transition" />
                </div>
                <h2 className="text-xl font-semibold text-gray-900">판매자</h2>
                <p className="text-sm text-gray-500 mt-2">
                  상품을 등록하고 거래를 시작하세요
                </p>
              </div>
            </Link>
          </div>

          <p className="text-sm text-gray-400 mt-10">
            믿을 수 있는 중고거래, Realive에서 시작하세요.
          </p>
        </div>
      </main>
  );
}
