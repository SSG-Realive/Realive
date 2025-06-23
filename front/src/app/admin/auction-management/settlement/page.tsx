"use client";
import { useRouter } from "next/navigation";

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

export default function SettlementPage() {
  const router = useRouter();

  return (
    <div className="p-8">
      <h2 className="text-2xl font-bold mb-6">정산 관리</h2>
      <div className="bg-white rounded-lg shadow">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="bg-gray-50">
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">판매자</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상품명</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">정산금액</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">상태</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">정산일</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">관리</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {dummySettlements.map((settlement) => (
                <tr key={settlement.id}>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <img className="h-10 w-10 rounded-full" src={settlement.sellerImage} alt="" />
                      <div className="ml-4">
                        <div className="text-sm font-medium text-gray-900">{settlement.seller}</div>
                      </div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{settlement.product}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{settlement.amount.toLocaleString()}원</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      settlement.status === '정산완료' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                    }`}>
                      {settlement.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {settlement.date}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <button
                      onClick={() => router.push(`/admin/auction-management/settlement/${settlement.id}`)}
                      className="text-blue-600 hover:text-blue-900"
                    >
                      상세
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
} 