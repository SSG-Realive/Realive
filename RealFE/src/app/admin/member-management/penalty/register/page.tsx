

import { Suspense } from 'react';
import PenaltyRegisterPage from './PenaltyRegisterPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <PenaltyRegisterPage/>
    </Suspense>
  );
}
