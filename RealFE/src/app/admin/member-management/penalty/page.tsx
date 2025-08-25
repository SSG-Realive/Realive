

import { Suspense } from 'react';
import PenaltyListPage from './PenaltyListPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <PenaltyListPage/>
    </Suspense>
  );
}
