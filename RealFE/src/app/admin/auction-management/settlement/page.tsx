import { Suspense } from "react";
import SettlementPage from "./SettlementPage";

export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <SettlementPage />
    </Suspense>
  );
}
