"use client";
import React from "react";

interface Report {
  id: number;
  reporter: string;
  reason: string;
  date: string;
  status: "처리중" | "완료";
}

const dummyReports: Report[] = [
  { id: 1, reporter: "user1", reason: "욕설", date: "2024-06-01", status: "처리중" },
  { id: 2, reporter: "user2", reason: "스팸", date: "2024-06-02", status: "완료" },
  { id: 3, reporter: "user3", reason: "광고", date: "2024-06-03", status: "처리중" },
  { id: 4, reporter: "user4", reason: "도배", date: "2024-06-04", status: "완료" },
];

export default function ReportedListPage() {
  return (
    <div className="p-8">
      <h1 className="text-2xl font-bold mb-4">신고된 목록</h1>
      <table className="min-w-full border">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-4 py-2">신고ID</th>
            <th className="px-4 py-2">신고자</th>
            <th className="px-4 py-2">사유</th>
            <th className="px-4 py-2">신고일</th>
            <th className="px-4 py-2">상태</th>
          </tr>
        </thead>
        <tbody>
          {dummyReports.map(r => (
            <tr key={r.id}>
              <td className="px-4 py-2">{r.id}</td>
              <td className="px-4 py-2">{r.reporter}</td>
              <td className="px-4 py-2">{r.reason}</td>
              <td className="px-4 py-2">{r.date}</td>
              <td className="px-4 py-2">{r.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
} 