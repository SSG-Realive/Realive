

import { Suspense } from 'react';
import AdminIntroPage from './AdminIntroPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <AdminIntroPage/>
    </Suspense>
  );
}
