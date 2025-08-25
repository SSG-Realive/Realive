import { User, Store } from 'lucide-react';
import Link from 'next/link';

// ✅ 페이지 컴포넌트가 searchParams를 props로 받도록 구조를 변경합니다.
interface LoginPageProps {
  searchParams: Promise<{
    redirectTo?: string;
  }>;
}

export default async function IntegratedLoginPage({ searchParams }: LoginPageProps) {
  // ✅ URL에서 redirectTo 값을 추출합니다.
  const params = await searchParams;
  const redirectTo = params.redirectTo || '';

  // ✅ 각 로그인 링크에 redirectTo 파라미터를 추가할 URL을 생성합니다.
  const customerLoginUrl = `/customer/member/login${redirectTo ? `?redirectTo=${redirectTo}` : ''}`;
  const sellerLoginUrl = `/seller/login${redirectTo ? `?redirectTo=${redirectTo}` : ''}`;
  
  return (
    <main className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <div className="w-full max-w-2xl">
        <div className="text-center mb-10">
          <p className="mt-2 text-gray-600">
            어떤 계정으로 로그인하시겠어요?
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          
          {/* 고객 로그인 카드 */}
          {/* ✅ 생성된 URL을 href에 연결합니다. */}
          <Link href={customerLoginUrl} className="group">
            <div className="p-8 bg-white rounded-lg border border-gray-200 shadow-sm hover:shadow-xl hover:border-blue-500 transition-all duration-300 cursor-pointer text-center h-full">
              <div className="flex justify-center mb-4">
                <User className="h-14 w-14 text-blue-600 group-hover:scale-110 transition-transform" />
              </div>
              <h2 className="text-xl font-semibold text-gray-900">
                고객
              </h2>
            </div>
          </Link>

          {/* 판매자 로그인 카드 */}
          {/* ✅ 생성된 URL을 href에 연결합니다. */}
          <Link href={sellerLoginUrl} className="group">
            <div className="p-8 bg-white rounded-lg border border-gray-200 shadow-sm hover:shadow-xl hover:border-green-500 transition-all duration-300 cursor-pointer text-center h-full">
              <div className="flex justify-center mb-4">
                <Store className="h-14 w-14 text-green-600 group-hover:scale-110 transition-transform" />
              </div>
              <h2 className="text-xl font-semibold text-gray-900">
                판매자
              </h2>
            </div>
          </Link>

        </div>
      </div>
    </main>
  );
}