

import { Suspense } from 'react';
import SellerAdminQnaPage from './SellerAdminQnaPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <SellerAdminQnaPage/>
    </Suspense>
  );
}
