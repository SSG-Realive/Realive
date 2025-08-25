

import { Suspense } from 'react';
import AuctionPaymentComponent from './AuctionPaymentComponent';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <AuctionPaymentComponent/>
    </Suspense>
  );
}
