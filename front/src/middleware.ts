import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // 잘못된 URL 패턴 처리
  if (pathname.startsWith('/customer/autions')) {
    const newUrl = request.nextUrl.clone();
    newUrl.pathname = pathname.replace('/customer/autions', '/customer/auctions');
    return NextResponse.redirect(newUrl);
  }

  return NextResponse.next();
}

export const config = {
  matcher: [
    '/customer/autions/:path*',
  ],
}; 