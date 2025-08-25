'use client';

import dynamic from 'next/dynamic';
import { Suspense } from 'react';

const Navbar = dynamic(() => import('@/components/customer/common/Navbar'), { ssr: false });

export default function ClientNavbarWrapper() {
  return (
    <Suspense fallback={null}>
      <Navbar />
    </Suspense>
  );
}
