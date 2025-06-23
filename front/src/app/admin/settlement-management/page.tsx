"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";

interface Settlement {
  id: string;
  seller: string;
  amount: number;
  date: string;
  status: "Pending" | "Completed";
  sellerImage: string;
}

const dummySettlements: Settlement[] = [
  { id: "SETT001", seller: "홍길동 (hong123)", amount: 100000, date: "2024-06-01", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/men/31.jpg" },
  { id: "SETT002", seller: "김철수 (kim456)", amount: 200000, date: "2024-06-02", status: "Completed", sellerImage: "https://randomuser.me/api/portraits/men/32.jpg" },
  { id: "SETT003", seller: "이영희 (lee789)", amount: 150000, date: "2024-06-03", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/women/33.jpg" },
  { id: "SETT004", seller: "박민수 (park321)", amount: 120000, date: "2024-06-04", status: "Pending", sellerImage: "https://randomuser.me/api/portraits/men/34.jpg" },
  { id: "SETT005", seller: "최지우 (choi654)", amount: 180000, date: "2024-06-05", status: "Completed", sellerImage: "https://randomuser.me/api/portraits/women/35.jpg" },
];

export default function SettlementManagementPage() {
  const [sellerFilter, setSellerFilter] = useState("");
  const [dateFilter, setDateFilter] = useState("");
  const [amountFilter, setAmountFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const router = useRouter();

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filtered = dummySettlements.filter(s =>
    (!sellerFilter || s.seller.includes(sellerFilter)) &&
    (!dateFilter || s.date === dateFilter) &&
    (!amountFilter || s.amount.toString() === amountFilter) &&
    (!statusFilter || s.status === statusFilter)
  );

  return (
    <div className="p-8">
      <div className="flex flex-wrap gap-2 mb-4">
        <input
          type="text"
          placeholder="판매자 검색"
          value={sellerFilter}
          onChange={e => setSellerFilter(e.target.value)}
          className="border rounded px-3 py-2"
        />
        <input
          type="date"
          placeholder="정산일"
          value={dateFilter}
          onChange={e => setDateFilter(e.target.value)}
          className="border rounded px-3 py-2"
        />
        <input
          type="text"
          placeholder="금액"
          value={amountFilter}
          onChange={e => setAmountFilter(e.target.value)}
          className="border rounded px-3 py-2"
        />
        <select
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
          className="border rounded px-3 py-2"
        >
          <option value="">All</option>
          <option value="Pending">Pending</option>
          <option value="Completed">Completed</option>
        </select>
        <button className="bg-blue-500 text-white px-4 py-2 rounded">Search</button>
      </div>
      <table className="min-w-full border">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-4 py-2">정산ID</th>
            <th className="px-4 py-2">판매자</th>
            <th className="px-4 py-2">금액</th>
            <th className="px-4 py-2">정산일</th>
            <th className="px-4 py-2">상태</th>
            <th className="px-4 py-2">Actions</th>
          </tr>
        </thead>
        <tbody>
          {filtered.map(s => (
            <tr key={s.id}>
              <td className="px-4 py-2">{s.id}</td>
              <td className="px-4 py-2">{s.seller}</td>
              <td className="px-4 py-2">{s.amount.toLocaleString()}원</td>
              <td className="px-4 py-2">{s.date}</td>
              <td className="px-4 py-2">
                <span className={
                  s.status === "Pending"
                    ? "bg-yellow-200 text-yellow-800 px-2 py-1 rounded"
                    : "bg-green-200 text-green-800 px-2 py-1 rounded"
                }>
                  {s.status}
                </span>
              </td>
              <td className="px-4 py-2">
                <button className="text-blue-600 underline" onClick={() => router.push(`/admin/settlement-management/${s.id}`)}>View</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
} 