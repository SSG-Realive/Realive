"use client";
import { useParams, useRouter } from "next/navigation";

// 더미 데이터
const dummyReported = [
  { id: "1", product: "노트북", user: "user1", reason: "부적절한 내용", status: "신고됨", userImage: "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=facearea&w=256&q=80" },
  { id: "2", product: "키보드", user: "user2", reason: "광고성", status: "신고처리됨", userImage: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=facearea&w=256&q=80" },
  { id: "3", product: "마우스", user: "user3", reason: "비방", status: "신고됨", userImage: "https://images.unsplash.com/photo-1599566150163-29194dcaad36?auto=format&fit=facearea&w=256&q=80" },
];

export default function ReportedReviewDetailPage() {
  const params = useParams();
  const router = useRouter();
  const { id } = params;

  const reported = dummyReported.find(r => r.id === id);

  if (!reported) {
    return <div className="p-8">신고된 리뷰 정보를 찾을 수 없습니다.</div>;
  }

  return (
    <div className="p-8 max-w-2xl mx-auto">
      <h2 className="text-2xl font-bold mb-6">신고된 리뷰 상세</h2>
      <div className="bg-white rounded-lg p-6 shadow">
        <div className="flex items-center gap-4 mb-6">
          <img src={reported.userImage} alt={reported.user} className="w-16 h-16 rounded-full border" />
          <div>
            <p className="text-lg font-semibold">{reported.user}</p>
            <p className="text-gray-500">작성자</p>
          </div>
        </div>
        <div className="space-y-4">
          <div>
            <p className="font-semibold">상품명</p>
            <p>{reported.product}</p>
          </div>
          <div>
            <p className="font-semibold">신고 사유</p>
            <p>{reported.reason}</p>
          </div>
          <div>
            <p className="font-semibold">상태</p>
            <p>{reported.status}</p>
          </div>
        </div>
        <div className="mt-6 flex gap-2">
          <button 
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
            onClick={() => router.push('/admin/review-management/reported')}
          >
            목록으로
          </button>
        </div>
      </div>
    </div>
  );
} 