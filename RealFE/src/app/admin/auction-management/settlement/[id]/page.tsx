import { Suspense } from "react";
import SettlementDetailPage from "./SettlementDetailPage";


export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <SettlementDetailPage />
    </Suspense>
  );
}