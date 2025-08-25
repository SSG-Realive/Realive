

import { Suspense } from 'react';
import CustomerListPage from './CustomerListPage';



export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <CustomerListPage/>
    </Suspense>
  );
}
