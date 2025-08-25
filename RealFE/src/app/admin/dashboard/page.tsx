import { Suspense } from "react";
import AdminDashboardPage from "./AdminDashboardPage";

export default function Page() {
  return (
    <Suspense fallback={<div className="text-center py-20">로딩 중...</div>}>
      <AdminDashboardPage />
    </Suspense>
  );
}