

import { Suspense } from 'react';
import OAuthCallbackPage from './OAuthCallbackPage';


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <OAuthCallbackPage/>
    </Suspense>
  );
}
