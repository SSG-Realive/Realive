// /app/main/auctions/page.tsx
import { Suspense } from 'react';
import CustomerHomePage from './CustomerHomePage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <CustomerHomePage/>
    </Suspense>
  );
}
