// /app/main/auctions/page.tsx
import { Suspense } from 'react';
import AdminCustomersDashboard from './AdminCustomersDashboard';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <AdminCustomersDashboard/>
    </Suspense>
  );
}
