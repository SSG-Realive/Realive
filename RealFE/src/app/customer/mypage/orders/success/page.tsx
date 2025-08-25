

import { Suspense } from 'react';
import PaymentSuccessPage from './PaymentSuccessPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <PaymentSuccessPage/>
    </Suspense>
  );
}
