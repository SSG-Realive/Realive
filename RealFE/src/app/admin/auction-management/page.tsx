

import { Suspense } from 'react';
import AuctionManagementPage from './AuctionManagementPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <AuctionManagementPage/>
    </Suspense>
  );
}
