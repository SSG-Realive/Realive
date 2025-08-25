

import { Suspense } from 'react';
import DirectOrderPage from './DirectOrderPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <DirectOrderPage/>
    </Suspense>
  );
}
