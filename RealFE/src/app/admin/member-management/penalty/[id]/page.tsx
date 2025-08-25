

import { Suspense } from 'react';
import PenaltyDetailPage from './PenaltyDetailPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <PenaltyDetailPage/>
    </Suspense>
  );
}
