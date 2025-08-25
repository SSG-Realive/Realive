// File: src/app/api/customer/auction-wins/[auctionId]/route.ts

import { NextRequest, NextResponse } from 'next/server';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ auctionId: string }> }
) {
  try {
    const { auctionId } = await params;

    // 예시: 경매 정보 조회 로직 (여기에 실제 로직 작성)
    const data = { message: `GET 요청 - 경매 ID: ${auctionId}` };

    return NextResponse.json(data);
  } catch (error) {
    console.error('GET 처리 오류:', error);
    return NextResponse.json(
      { error: error instanceof Error ? error.message : '처리 실패' },
      { status: 500 }
    );
  }
}

export async function POST(
  request: NextRequest,
  { params }: { params: Promise<{ auctionId: string }> }
) {
  try {
    const { auctionId } = await params;
    const body = await request.json();

    // 예시: 결제 처리 API 호출 (여기에 실제 API 호출 작성)
    const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL}/api/customer/auction-wins/${auctionId}/payment`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Cookie': request.headers.get('cookie') || '',
      },
      body: JSON.stringify(body),
    });

    if (!response.ok) {
      const errorData = await response.json();
      throw new Error(errorData.message || '결제 처리에 실패했습니다.');
    }

    const data = await response.json();
    return NextResponse.json(data);
  } catch (error) {
    console.error('경매 결제 처리 오류:', error);
    return NextResponse.json(
      { error: error instanceof Error ? error.message : '결제 처리에 실패했습니다.' },
      { status: 500 }
    );
  }
}
 
