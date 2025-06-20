import { NextRequest, NextResponse } from 'next/server';

export async function POST(
  request: NextRequest,
  { params }: { params: { auctionId: string } }
) {
  try {
    const { auctionId } = params;
    const body = await request.json();

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
 