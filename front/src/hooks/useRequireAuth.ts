import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';

export default function useRequireAuth() {
  const token = useAuthStore((state) => state.token);
  const router = useRouter();

  useEffect(() => {
    if (!token) {
      const currentPath = window.location.pathname;
      router.replace(`/customer/member/login?redirectTo=${encodeURIComponent(currentPath)}`);
    }
  }, [token, router]);

  return token;
}
