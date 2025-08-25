// /customer/mypage/wishlist/page.tsx
import { Suspense } from 'react';
import WishlistPage from './WishlistPage';


export default function WishlistWrapper() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <WishlistPage />
    </Suspense>
  );
}
