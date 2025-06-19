"use client";
import React from "react";

const dummyReported: { product: string; user: string; reason: string; status: string }[] = [
  { product: "키보드", user: "user2", reason: "욕설", status: "신고됨" },
  { product: "의자", user: "user5", reason: "광고성", status: "신고됨" },
  { product: "책상", user: "user6", reason: "도배", status: "신고됨" },
  { product: "마우스", user: "user3", reason: "부적절한 내용", status: "신고됨" },
  { product: "모니터", user: "user4", reason: "욕설", status: "신고됨" },
  { product: "노트북", user: "user1", reason: "도배", status: "신고됨" },
  { product: "스피커", user: "user7", reason: "욕설", status: "신고됨" },
  { product: "프린터", user: "user8", reason: "광고성", status: "신고됨" },
  { product: "마우스패드", user: "user9", reason: "도배", status: "신고됨" },
  { product: "램프", user: "user10", reason: "욕설", status: "신고됨" },
];

export default function ReviewReportedPage() {
  return (
    <div className="p-8">
      <h2 className="text-lg font-bold mb-4">리뷰 신고 관리</h2>
      <form className="flex gap-2 mb-2">
        <input className="border px-2 py-1 flex-1" placeholder="상품명/작성자 검색" />
        <button className="bg-blue-500 text-white px-4 py-2 rounded" type="button">Search</button>
      </form>
      <table className="min-w-full border text-sm">
        <thead>
          <tr className="bg-gray-100">
            <th className="px-2 py-1">상품명</th>
            <th className="px-2 py-1">작성자</th>
            <th className="px-2 py-1">사유</th>
            <th className="px-2 py-1">상태</th>
            <th className="px-2 py-1">상태변경</th>
          </tr>
        </thead>
        <tbody>
          {dummyReported.map((r, idx) => (
            <tr key={idx}>
              <td className="px-2 py-1">{r.product}</td>
              <td className="px-2 py-1">{r.user}</td>
              <td className="px-2 py-1">{r.reason}</td>
              <td className="px-2 py-1">{r.status}</td>
              <td className="px-2 py-1">
                <button className="bg-yellow-400 text-white px-2 py-1 rounded">정상처리</button>
                <button className="bg-red-500 text-white px-2 py-1 rounded ml-2">삭제</button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
} 