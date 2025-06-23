import { NextRequest, NextResponse } from 'next/server';

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url);
    const page = searchParams.get('page') || '0';
    const size = searchParams.get('size') || '10';

    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/customer/auction-wins?page=${page}&size=${size}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Cookie': request.headers.get('cookie') || '',
      },
    });

    if (!response.ok) {
      throw new Error('낙찰한 경매 목록 조회에 실패했습니다.');
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('낙찰한 경매 목록 조회 오류:', error);
    return NextResponse.json(
      { error: '낙찰한 경매 목록 조회에 실패했습니다.' },
      { status: 500 }
    );
  }
} 