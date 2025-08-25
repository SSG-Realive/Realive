'use client';

import MyPageSidebar from '@/components/customer/common/MyPageSidebar';

interface Props {
    children: React.ReactNode;
}

export default function MyPageLayout({ children }: Props) {
    return (
        <div className="flex w-full max-w-screen-xl mx-auto px-4 py-6">
            {/* 사이드바: 데스크탑만 표시 */}
            <MyPageSidebar />
            {/* 오른쪽 컨텐츠 */}
            <main className="flex-1 ml-0 md:ml-6 pl-0 md:pl-0">
                {children}
            </main>
        </div>
    );
}

