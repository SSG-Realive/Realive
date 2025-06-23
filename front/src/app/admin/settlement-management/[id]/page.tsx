"use client";
import { useParams, useRouter, redirect } from "next/navigation";
import { useEffect } from 'react';

const dummySettlements = [
  { id: "SETT001", seller: "홍길동 (hong123)", amount: 100000, date: "2024-06-01", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/men/31.jpg" },
  { id: "SETT002", seller: "김철수 (kim456)", amount: 200000, date: "2024-06-02", status: "Completed", sellerImage: "https://randomuser.me/api/portraits/men/32.jpg" },
  { id: "SETT003", seller: "이영희 (lee789)", amount: 150000, date: "2024-06-03", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/women/33.jpg" },
  { id: "SETT004", seller: "박민수 (park321)", amount: 120000, date: "2024-06-04", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/men/34.jpg" },
  { id: "SETT005", seller: "최지우 (choi654)", amount: 180000, date: "2024-06-05", status: "Completed", sellerImage: "https://randomuser.me/api/portraits/women/35.jpg" },
];

export default function SettlementDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { id } = params;

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    redirect('/admin/login');
  }

  const settlement = dummySettlements.find(s => s.id === id);

  if (!settlement) {
    return <div className="p-8">정산 정보를 찾을 수 없습니다.</div>;
  }

  return (
    <div className="p-8 max-w-2xl mx-auto">
      <h2 className="text-2xl font-bold mb-6">정산 상세</h2>
      <div className="bg-white rounded-lg p-6 shadow">
        <div className="flex items-center gap-4 mb-6">
          <img src={settlement.sellerImage} alt={settlement.seller} className="w-16 h-16 rounded-full border" />
          <div>
            <p className="text-lg font-semibold">{settlement.seller}</p>
            <p className="text-gray-500">판매자</p>
          </div>
        </div>
        <div className="space-y-4">
          <div>
            <p className="font-semibold">정산ID</p>
            <p>{settlement.id}</p>
          </div>
          <div>
            <p className="font-semibold">정산 금액</p>
            <p>{settlement.amount.toLocaleString()}원</p>
          </div>
          <div>
            <p className="font-semibold">정산 일자</p>
            <p>{settlement.date}</p>
          </div>
          <div>
            <p className="font-semibold">상태</p>
            <span className={
              settlement.status === "Pending"
                ? "bg-yellow-200 text-yellow-800 px-2 py-1 rounded"
                : "bg-green-200 text-green-800 px-2 py-1 rounded"
            }>
              {settlement.status}
            </span>
          </div>
        </div>
        <div className="mt-6 flex gap-2">
          <button 
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            onClick={() => router.push('/admin/settlement-management')}
          >
            목록으로
          </button>
        </div>
      </div>
    </div>
  );
} 