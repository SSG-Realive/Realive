// src/components/CustomerHeader.tsx
'use client';

import Link from 'next/link';

export default function CustomerHeader() {
    return (
        <header
            style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                padding: '1rem 2rem',
                borderBottom: '1px solid #ddd',
                marginBottom: '2rem',
            }}
        >
            {/* 로고 및 홈 이동 */}
            <Link
                href="/public"
                style={{
                    fontSize: '1.25rem',
                    fontWeight: 'bold',
                    textDecoration: 'none',
                    color: '#333',
                }}
            >
                Realive
            </Link>

            {/* 우측 메뉴 (고객센터, 마이페이지 등) */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '1rem' }}>
                <span style={{ fontSize: '1rem', color: '#333' }}>고객님</span>
                <Link href="/login" style={{ fontSize: '1rem', color: '#333' }}>
                    로그인
                </Link>
                <Link href="/mypage" style={{ fontSize: '1rem', color: '#333' }}>
                    마이페이지
                </Link>
                <Link href="/cart" style={{ fontSize: '1rem', color: '#333' }}>
                    장바구니
                </Link>
            </div>
        </header>
    );
}
