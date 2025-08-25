"use client";
import { useParams, useRouter } from "next/navigation";

// 더미 데이터
const dummyPenalties = [
  { id: "1", user: "user1", reason: "부적절한 행동", date: "2024-03-01", userImage: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=facearea&w=256&q=80" },
  { id: "2", user: "user2", reason: "비매너", date: "2024-03-02", userImage: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=facearea&w=256&q=80" },
  { id: "3", user: "user3", reason: "규정 위반", date: "2024-03-03", userImage: "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=facearea&w=256&q=80" },
];

export default function AuctionPenaltyDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { id } = params;

  const penalty = dummyPenalties.find(p => p.id === id);

  if (!penalty) {
    return <div className="p-8">패널티 정보를 찾을 수 없습니다.</div>;
  }

  return (
    <div className="p-8 max-w-2xl mx-auto">
      <h2 className="text-2xl font-bold mb-6">경매 패널티 상세</h2>
      <div className="bg-white rounded-lg p-6 shadow">
        <div className="flex items-center gap-4 mb-6">
          <img src={penalty.userImage} alt={penalty.user} className="w-16 h-16 rounded-full border" />
          <div>
            <p className="text-lg font-semibold">{penalty.user}</p>
            <p className="text-gray-500">사용자</p>
          </div>
        </div>
        <div className="space-y-4">
          <div>
            <p className="font-semibold">사유</p>
            <p>{penalty.reason}</p>
          </div>
          <div>
            <p className="font-semibold">일자</p>
            <p>{penalty.date}</p>
          </div>
        </div>
        <div className="mt-6 flex gap-2">
          <button 
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            onClick={() => router.push('/admin/auction-management/penalty')}
          >
            목록으로
          </button>
        </div>
      </div>
    </div>
  );
} 