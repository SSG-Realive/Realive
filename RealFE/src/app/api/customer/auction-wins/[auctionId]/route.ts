import { NextRequest, NextResponse } from 'next/server';

export async function GET(
  request: NextRequest,
  context: any // 👈 여기서 타입 에러 회피
) {
  try {
    const { auctionId } = context.params;

    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/customer/auction-wins/${auctionId}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
        'Cookie': request.headers.get('cookie') || '',
      },
    });

    if (!response.ok) {
      throw new Error('낙찰 정보 조회에 실패했습니다.');
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('낙찰 정보 조회 오류:', error);
    return NextResponse.json(
      { error: '낙찰 정보 조회에 실패했습니다.' },
      { status: 500 }
    );
  }
}
