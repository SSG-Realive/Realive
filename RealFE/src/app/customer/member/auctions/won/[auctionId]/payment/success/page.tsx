

import { Suspense } from 'react';
import PaymentSuccessComponent from './PaymentSuccessComponent';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <PaymentSuccessComponent/>
    </Suspense>
  );
}
