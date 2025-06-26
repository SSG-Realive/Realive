// src/app/api/reviews/exists/route.ts
import { NextRequest, NextResponse } from 'next/server';
import axios from 'axios';

export async function GET(req: NextRequest) {
    const { searchParams } = new URL(req.url);

    const orderId = searchParams.get('orderId');
    const sellerId = searchParams.get('sellerId');

    if (!orderId || !sellerId) {
        return NextResponse.json(
            { error: 'orderId와 sellerId는 필수입니다.' },
            { status: 400 }
        );
    }

    try {
        // ✅ 백엔드 서버 주소에 맞게 수정
        const backendRes = await axios.get(
            `http://localhost:8080/api/reviews/exists`,
            {
                params: {
                    orderId,
                    sellerId,
                },
            }
        );

        return NextResponse.json(backendRes.data, { status: 200 });
    } catch (error: any) {
        console.error('백엔드 리뷰 중복 체크 오류:', error?.response?.data || error.message);
        return NextResponse.json(
            {
                status: 500,
                code: 'INTERNAL_SERVER_ERROR',
                message: '서버 오류가 발생했습니다',
            },
            { status: 500 }
        );
    }
}
