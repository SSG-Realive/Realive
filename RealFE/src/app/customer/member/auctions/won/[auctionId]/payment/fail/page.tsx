

import { Suspense } from 'react';
import PaymentFailComponent from './PaymentFailComponent';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <PaymentFailComponent/>
    </Suspense>
  );
}
