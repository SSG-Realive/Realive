

import { Suspense } from 'react';
import AdminQnaDetailPage from './AdminQnaDetailPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <AdminQnaDetailPage/>
    </Suspense>
  );
}
