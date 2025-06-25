// src/app/components/SellerHeader.tsx (최종 수정본)

'use client';

import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { getProfile } from '@/service/seller/sellerService'; // 불필요한 logout 임포트 제거
import { useEffect, useState } from 'react';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore';
import { requestSellerLogout } from '@/service/seller/logoutService'; // 정확한 로그아웃 서비스 함수

interface SellerHeaderProps {
  toggleSidebar?: () => void;
}

export default function SellerHeader({ toggleSidebar }: SellerHeaderProps) {
    const router = useRouter();
    const [name, setName] = useState<string>('');

    // ✨ 1. Zustand 스토어에서 필요한 값들을 한번에, 올바른 이름으로 가져옵니다.
    const accessToken = useSellerAuthStore((state) => state.accessToken);
    const logout = useSellerAuthStore((state) => state.logout);
    
    useEffect(() => {
        const fetchName = async () => {
            try {
                const profile = await getProfile();
                setName(profile.name);
            } catch (err) {
                console.error('프로필 정보 가져오기 실패', err);
            }
        };

        // ✨ 2. 'accessToken' 변수를 사용하여 토큰 존재 여부를 확인합니다.
        if (accessToken) {
            fetchName();
        } else {
            // 토큰이 없으면(로그아웃 시) 이름을 비워줍니다.
            setName('');
        }
    }, [accessToken]); // ✨ 3. 의존성 배열에도 'accessToken'을 사용합니다.

    const handleLogout = async () => {
        try {
            await requestSellerLogout();
            alert('안전하게 로그아웃 되었습니다.');
        } catch (error) {
            console.error("판매자 로그아웃 요청 실패:", error);
            alert('로그아웃 처리 중 오류가 발생했지만, 클라이언트에서는 로그아웃됩니다.');
        } finally {
            // ✨ 4. 스토어에서 직접 가져온 'logout' 함수를 호출합니다.
            logout(); 
            router.push('/seller/login');
        }
    };

    return (
        <header className="flex items-center justify-between px-8 py-3 bg-[#5b4636] border-b border-[#bfa06a] shadow-sm">
            <div className="flex items-center gap-3">
                {toggleSidebar && (
                    <button
                        onClick={toggleSidebar}
                        className="block lg:hidden text-2xl text-[#e9dec7] hover:text-[#bfa06a] transition-colors"
                        style={{ background: 'none', border: 'none' }}
                    >
                        ☰
                    </button>
                )}
                <Link href="/seller/dashboard" className="text-2xl font-extrabold text-[#e9dec7] tracking-tight hover:text-[#bfa06a] transition-colors">
                    Realive
                </Link>
            </div>
            <div className="flex items-center gap-6">
                {accessToken ? (
                    <>
                        <span className="font-semibold text-[#e9dec7]">{name}님</span>
                        <button
                            onClick={handleLogout}
                            className="text-[#bfa06a] hover:text-[#e9dec7] font-semibold transition-colors"
                            style={{ background: 'none', border: 'none', fontSize: '1rem', cursor: 'pointer' }}
                        >
                            로그아웃
                        </button>
                    </>
                ) : (
                    <Link href="/seller/login" className="font-semibold text-[#bfa06a] hover:text-[#e9dec7] transition-colors">
                        로그인
                    </Link>
                )}
            </div>
        </header>
    );
}