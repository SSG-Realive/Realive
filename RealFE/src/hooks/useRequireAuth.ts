import { useState, useEffect, useRef } from 'react';
import { usePathname, useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
import { useGlobalDialog } from '@/app/context/dialogContext';

// [수정] 훅이 반환할 값의 타입을 정의합니다.
interface AuthStatus {
  isLoading: boolean;     // 인증 확인 절차가 진행 중인지 여부
  isAuthenticated: boolean; // 인증이 성공적으로 완료되었는지 여부
  accessToken: string | null;
}

export default function useRequireAuth(message = '로그인이 필요한 서비스 입니다.'): AuthStatus {
  const hydrated = useAuthStore((s) => s.hydrated);
  const accessToken = useAuthStore((s) => s.accessToken);
  
  const router = useRouter();
  const pathname = usePathname();
  const { show } = useGlobalDialog();

  // [수정] 로딩과 인증 상태를 관리하는 내부 state 추가
  const [authStatus, setAuthStatus] = useState<AuthStatus>({
    isLoading: true, // 처음엔 무조건 로딩 상태
    isAuthenticated: false,
    accessToken: null,
  });

  useEffect(() => {
    // 스토어가 복원될 때까지 기다립니다.
    if (!hydrated) {
      return;
    }

    // 스토어 복원 완료 후, 토큰 유무에 따라 상태를 결정합니다.
    if (accessToken) {
      // 토큰이 있으면, 인증 절차 완료!
      setAuthStatus({
        isLoading: false,
        isAuthenticated: true,
        accessToken,
      });
    } else {
      // 토큰이 없으면, 인증 실패 및 리다이렉트 로직 실행
      setAuthStatus({
        isLoading: false,
        isAuthenticated: false,
        accessToken: null,
      });

      if (!pathname.startsWith('/customer/member/login')) {
        (async () => {
          await show(message);
          router.replace(
            `/customer/member/login?redirectTo=${encodeURIComponent(pathname)}`,
          );
        })();
      }
    }
  }, [hydrated, accessToken, pathname, router, show, message]);

  // [수정] 단순 토큰이 아닌, 상태 객체를 반환합니다.
  return authStatus;
}