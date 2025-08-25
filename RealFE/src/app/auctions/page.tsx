// /app/main/auctions/page.tsx
import { Suspense } from 'react';
import AuctionPage from './AuctionPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <AuctionPage/>
    </Suspense>
  );
}
