

import { Suspense } from 'react';
import AdminSellersPage from './AdminSellersPage';



export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <AdminSellersPage/>
    </Suspense>
  );
}
