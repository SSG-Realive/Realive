"use client";
import React, { useState } from "react";

interface Auction {
  id: number;
  title: string;
  startPrice: number;
  currentPrice: number;
  bidCount: number;
  startDate: string;
  endDate: string;
  status: "진행중" | "종료" | "취소됨";
}

const dummyAuctions: Auction[] = [
  { id: 1, title: "노트북 경매", startPrice: 500000, currentPrice: 800000, bidCount: 12, startDate: "2024-03-01", endDate: "2024-03-05", status: "종료" },
  { id: 2, title: "스마트폰 경매", startPrice: 300000, currentPrice: 450000, bidCount: 8, startDate: "2024-03-02", endDate: "2024-03-06", status: "종료" },
  { id: 3, title: "에어팟 경매", startPrice: 100000, currentPrice: 180000, bidCount: 15, startDate: "2024-03-03", endDate: "2024-03-07", status: "종료" },
  { id: 4, title: "자전거 경매", startPrice: 200000, currentPrice: 350000, bidCount: 10, startDate: "2024-03-04", endDate: "2024-03-08", status: "종료" },
  { id: 5, title: "커피머신 경매", startPrice: 150000, currentPrice: 220000, bidCount: 7, startDate: "2024-03-05", endDate: "2024-03-09", status: "종료" },
  { id: 6, title: "캠핑의자 경매", startPrice: 50000, currentPrice: 90000, bidCount: 5, startDate: "2024-03-06", endDate: "2024-03-10", status: "종료" },
  { id: 7, title: "스피커 경매", startPrice: 80000, currentPrice: 120000, bidCount: 9, startDate: "2024-03-07", endDate: "2024-03-11", status: "종료" },
  { id: 8, title: "모니터 경매", startPrice: 200000, currentPrice: 270000, bidCount: 6, startDate: "2024-03-08", endDate: "2024-03-12", status: "종료" },
  { id: 9, title: "의류 경매", startPrice: 30000, currentPrice: 70000, bidCount: 11, startDate: "2024-03-09", endDate: "2024-03-13", status: "종료" },
  { id: 10, title: "책상 경매", startPrice: 100000, currentPrice: 150000, bidCount: 4, startDate: "2024-03-10", endDate: "2024-03-14", status: "진행중" },
];

export default function AuctionManagementPage() {
  const [filter, setFilter] = useState("");
  const [selected, setSelected] = useState<Auction | null>(null);

  const filtered = dummyAuctions.filter(a => a.title.includes(filter));

  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">경매 관리</h1>
      <div className="mb-4">
        <input
          type="text"
          placeholder="경매명 검색"
          value={filter}
          onChange={e => setFilter(e.target.value)}
          className="border rounded px-3 py-2"
        />
      </div>
      <table className="min-w-full border">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-4 py-2">경매명</th>
            <th className="px-4 py-2">시작가</th>
            <th className="px-4 py-2">현재가</th>
            <th className="px-4 py-2">입찰수</th>
            <th className="px-4 py-2">시작일</th>
            <th className="px-4 py-2">종료일</th>
            <th className="px-4 py-2">상태</th>
            <th className="px-4 py-2">상세</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(a => (
            <tr key={a.id}>
              <td className="px-4 py-2">{a.title}</td>
              <td className="px-4 py-2">{a.startPrice.toLocaleString()}원</td>
              <td className="px-4 py-2">{a.currentPrice.toLocaleString()}원</td>
              <td className="px-4 py-2">{a.bidCount}</td>
              <td className="px-4 py-2">{a.startDate}</td>
              <td className="px-4 py-2">{a.endDate}</td>
              <td className="px-4 py-2">{a.status}</td>
              <td className="px-4 py-2">
                <button className="text-blue-600 underline" onClick={() => setSelected(a)}>
                  상세
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
      {selected && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[300px]">
            <h2 className="text-xl font-bold mb-4">경매 상세</h2>
            <p><b>경매명:</b> {selected.title}</p>
            <p><b>시작가:</b> {selected.startPrice.toLocaleString()}원</p>
            <p><b>현재가:</b> {selected.currentPrice.toLocaleString()}원</p>
            <p><b>입찰수:</b> {selected.bidCount}</p>
            <p><b>시작일:</b> {selected.startDate}</p>
            <p><b>종료일:</b> {selected.endDate}</p>
            <p><b>상태:</b> {selected.status}</p>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelected(null)}>
              닫기
            </button>
          </div>
        </div>
      )}
    </div>
  );
} 