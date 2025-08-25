

import { Suspense } from 'react';
import PaymentFailPage from './PaymentFailPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <PaymentFailPage/>
    </Suspense>
  );
}
