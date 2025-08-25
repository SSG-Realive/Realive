

import { Suspense } from 'react';
import IntegratedLoginPage from './IntegratedLoginPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <IntegratedLoginPage/>
    </Suspense>
  );
}
