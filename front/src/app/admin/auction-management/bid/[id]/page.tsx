"use client";
import { useParams, useRouter } from "next/navigation";

const dummyBids = [
  { id: "1", user: "user1", amount: 100000, date: "2024-06-01", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/1.jpg" },
  { id: "2", user: "user2", amount: 120000, date: "2024-06-02", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/2.jpg" },
  { id: "3", user: "user3", amount: 150000, date: "2024-06-02", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/3.jpg" },
  { id: "4", user: "user4", amount: 200000, date: "2024-06-03", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/4.jpg" },
  { id: "5", user: "user5", amount: 90000, date: "2024-06-04", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/5.jpg" },
  { id: "6", user: "user6", amount: 110000, date: "2024-06-05", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/6.jpg" },
  { id: "7", user: "user7", amount: 130000, date: "2024-06-06", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/7.jpg" },
  { id: "8", user: "user8", amount: 170000, date: "2024-06-07", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/8.jpg" },
  { id: "9", user: "user9", amount: 95000, date: "2024-06-08", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/9.jpg" },
  { id: "10", user: "user10", amount: 105000, date: "2024-06-09", winner: "user4", winAmount: 200000, userImage: "https://randomuser.me/api/portraits/men/10.jpg" },
];

export default function BidDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { id } = params;

  const bid = dummyBids.find(b => b.id === id);

  if (!bid) {
    return <div className="p-8">입찰 정보를 찾을 수 없습니다.</div>;
  }

  return (
    <div className="p-8 max-w-2xl mx-auto">
      <h2 className="text-2xl font-bold mb-6">입찰 상세</h2>
      <div className="bg-white rounded-lg p-6 shadow">
        <div className="flex items-center gap-4 mb-6">
          <img src={bid.userImage} alt={bid.user} className="w-16 h-16 rounded-full border" />
          <div>
            <p className="text-lg font-semibold">{bid.user}</p>
            <p className="text-gray-500">입찰자</p>
          </div>
        </div>
        <div className="space-y-4">
          <div>
            <p className="font-semibold">입찰 금액</p>
            <p>{bid.amount.toLocaleString()}원</p>
          </div>
          <div>
            <p className="font-semibold">입찰 일자</p>
            <p>{bid.date}</p>
          </div>
          <div>
            <p className="font-semibold">낙찰자</p>
            <p>{bid.winner}</p>
          </div>
          <div>
            <p className="font-semibold">낙찰가</p>
            <p>{bid.winAmount.toLocaleString()}원</p>
          </div>
        </div>
        <div className="mt-6 flex gap-2">
          <button 
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            onClick={() => router.push('/admin/auction-management/bid')}
          >
            목록으로
          </button>
        </div>
      </div>
    </div>
  );
} 