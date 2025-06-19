import { NextResponse } from 'next/server';
import axios from 'axios';
import api from '@/app/lib/axios'; // 커스텀 axios 인스턴스
import { MemberJoinDTO } from '@/types/customer/signup/signup';


export async function POST(request: Request) {
  try {
    const body: MemberJoinDTO = await request.json();

    // 백엔드에 회원가입 요청
    const response = await api.post('api/public/auth/join', body);

    return NextResponse.json({
      success: true,
      message: response.data.message,
      token: response.data.token,
    });
  } catch (error: any) {
    console.error('Signup error:', error);

    // Axios 에러 응답 처리
    if (axios.isAxiosError(error) && error.response) {
      return NextResponse.json(
        {
          success: false,
          message: error.response.data?.message || '회원가입 실패',
        },
        { status: error.response.status || 400 }
      );
    }

    return NextResponse.json(
      { success: false, message: '알 수 없는 오류로 회원가입에 실패했습니다.' },
      { status: 500 }
    );
  }
}
