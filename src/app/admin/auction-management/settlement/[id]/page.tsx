"use client";
import { useParams, useRouter } from "next/navigation";

// 더미 데이터
const dummySettlements = [
  { 
    id: "1", 
    seller: "판매자1", 
    product: "노트북", 
    amount: 1500000, 
    status: "정산완료", 
    date: "2024-03-01",
    sellerImage: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=facearea&w=256&q=80"
  },
  { 
    id: "2", 
    seller: "판매자2", 
    product: "키보드", 
    amount: 120000, 
    status: "정산대기", 
    date: "2024-03-02",
    sellerImage: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=facearea&w=256&q=80"
  },
  { 
    id: "3", 
    seller: "판매자3", 
    product: "마우스", 
    amount: 45000, 
    status: "정산완료", 
    date: "2024-03-03",
    sellerImage: "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=facearea&w=256&q=80"
  },
];

export default function SettlementDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { id } = params;

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
            <p className="font-semibold">상품명</p>
            <p>{settlement.product}</p>
          </div>
          <div>
            <p className="font-semibold">정산금액</p>
            <p>{settlement.amount.toLocaleString()}원</p>
          </div>
          <div>
            <p className="font-semibold">상태</p>
            <p>{settlement.status}</p>
          </div>
          <div>
            <p className="font-semibold">정산일</p>
            <p>{settlement.date}</p>
          </div>
        </div>
        <div className="mt-6 flex gap-2">
          <button 
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            onClick={() => router.push('/admin/auction-management/settlement')}
          >
            목록으로
          </button>
        </div>
      </div>
    </div>
  );
} 