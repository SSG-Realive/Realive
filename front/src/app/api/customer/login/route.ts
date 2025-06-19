// src/app/api/customer/login/route.ts
import { NextResponse } from 'next/server';
import type { LoginRequest, LoginResponse } from '@/app/types/customer/login';
import api from '@/app/lib/axios';
import axios from 'axios';

export async function POST(request: Request) {
  try {
    /* 1) 입력값 파싱 */
    const { email, password } = (await request.json()) as LoginRequest;

    /* 2) 백엔드 로그인 엔드포인트 호출
       - URL 앞에 슬래시 포함 주의
       - <LoginResponse> 제네릭으로 타입 지정 */
    const { data } = await api.post<LoginResponse>(
      '/api/public/auth/login',
      { email, password }
    );

    /* 3) 백엔드가 내려준 플랫 JSON 그대로 반환 */
    return NextResponse.json(data);        // 200 OK
  } catch (err: unknown) {
    /* 4) 에러 메시지 추출 */
    if (axios.isAxiosError(err) && err.response) {
      const { status, data } = err.response;
      const message = (data as any).message ?? '로그인에 실패했습니다.';
      return NextResponse.json({ message }, { status: status || 400 });
    }

    return NextResponse.json(
      { message: '로그인에 실패했습니다.' },
      { status: 400 }
    );
  }
}
