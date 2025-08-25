

import { Suspense } from 'react';
import WriteQnaPage from './WriteQnaPage';



export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <WriteQnaPage/>
    </Suspense>
  );
}
