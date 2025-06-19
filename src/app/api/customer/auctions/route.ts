import { NextRequest, NextResponse } from "next/server";
import api from '@/app/lib/axios'; // axios 인스턴스 import

export async function GET(request: NextRequest) {
  try {
    const cookie = request.headers.get("cookie") || "";

    // 쿠키를 수동으로 axios 요청에 포함
    const response = await api.get('customer/auctions', {
      headers: {
        Cookie: cookie, // 서버사이드 요청이므로 쿠키 직접 전달
      },
      withCredentials: true, // 쿠키 기반 인증을 허용
    });

    return NextResponse.json(response.data, { status: response.status });
  } catch (error: any) {
    console.error("경매 목록 조회 중 오류 발생:", error);

    const message = error?.response?.data?.message || "경매 목록 조회 중 오류 발생";
    const status = error?.response?.status || 500;

    return NextResponse.json(
      { success: false, message },
      { status }
    );
  }
}
