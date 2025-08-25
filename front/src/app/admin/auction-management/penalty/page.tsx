"use client";
import React, { useState } from "react";
import { useRouter } from "next/navigation";

interface Penalty {
  id: number;
  user: string;
  reason: string;
  date: string;
  userImage: string;
}

const dummyPenalties: Penalty[] = [
  { id: 1, user: "user1", reason: "부정입찰", date: "2024-06-01", userImage: "https://randomuser.me/api/portraits/men/21.jpg" },
  { id: 2, user: "user2", reason: "허위정보", date: "2024-06-02", userImage: "https://randomuser.me/api/portraits/women/22.jpg" },
  { id: 3, user: "user3", reason: "욕설", date: "2024-06-03", userImage: "https://randomuser.me/api/portraits/men/23.jpg" },
  { id: 4, user: "user4", reason: "도배", date: "2024-06-04", userImage: "https://randomuser.me/api/portraits/women/24.jpg" },
  { id: 5, user: "user5", reason: "광고성", date: "2024-06-05", userImage: "https://randomuser.me/api/portraits/men/25.jpg" },
  { id: 6, user: "user6", reason: "부정입찰", date: "2024-06-06", userImage: "https://randomuser.me/api/portraits/women/26.jpg" },
  { id: 7, user: "user7", reason: "허위정보", date: "2024-06-07", userImage: "https://randomuser.me/api/portraits/men/27.jpg" },
  { id: 8, user: "user8", reason: "욕설", date: "2024-06-08", userImage: "https://randomuser.me/api/portraits/women/28.jpg" },
  { id: 9, user: "user9", reason: "도배", date: "2024-06-09", userImage: "https://randomuser.me/api/portraits/men/29.jpg" },
  { id: 10, user: "user10", reason: "광고성", date: "2024-06-10", userImage: "https://randomuser.me/api/portraits/women/30.jpg" },
];

export default function PenaltyListPage() {
  const router = useRouter();
  const [search, setSearch] = useState("");
  const filtered = dummyPenalties.filter(p => 
    p.user.includes(search) || 
    p.reason.includes(search)
  );

  return (
    <div className="p-8">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-bold">사용자 패널티 목록</h2>
        <button
          className="bg-red-500 text-white px-4 py-2 rounded hover:bg-red-600"
          onClick={() => router.push("/admin/auction-management/penalty/register")}
        >
          사용자 패널티 등록
        </button>
      </div>
      <div className="mb-4">
        <input
          type="text"
          placeholder="사용자/사유 검색"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-2 py-1">User</th>
            <th className="px-2 py-1">사유</th>
            <th className="px-2 py-1">일자</th>
            <th className="px-2 py-1">View</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(penalty => (
            <tr key={penalty.id}>
              <td className="px-2 py-1">{penalty.user}</td>
              <td className="px-2 py-1">{penalty.reason}</td>
              <td className="px-2 py-1">{penalty.date}</td>
              <td className="px-2 py-1">
                <button 
                  className="text-blue-600 underline" 
                  onClick={() => router.push(`/admin/auction-management/penalty/${penalty.id}`)}
                >
                  View
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
} 