

import { Suspense } from 'react';
import ReviewPage from './ReviewPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <ReviewPage/>
    </Suspense>
  );
}
