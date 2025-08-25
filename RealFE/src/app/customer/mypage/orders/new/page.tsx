

import { Suspense } from 'react';
import NewOrderPage from './NewOrderPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <NewOrderPage/>
    </Suspense>
  );
}
