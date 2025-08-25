"use client";
import React, { useState } from "react";
import { useRouter } from "next/navigation";

export default function PenaltyRegisterPage() {
  const router = useRouter();
  const [user, setUser] = useState("");
  const [reason, setReason] = useState("");
  const [date, setDate] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    // 실제 등록 로직 없이 목록으로 이동
    router.push("/admin/auction-management/penalty");
  };

  return (
    <div className="p-8 max-w-md mx-auto">
      <h2 className="text-lg font-bold mb-4">사용자 패널티 등록</h2>
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block mb-1 font-medium">User</label>
          <input
            type="text"
            className="w-full border px-3 py-2 rounded"
            value={user}
            onChange={e => setUser(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="block mb-1 font-medium">사유</label>
          <input
            type="text"
            className="w-full border px-3 py-2 rounded"
            value={reason}
            onChange={e => setReason(e.target.value)}
            required
          />
        </div>
        <div>
          <label className="block mb-1 font-medium">일자</label>
          <input
            type="date"
            className="w-full border px-3 py-2 rounded"
            value={date}
            onChange={e => setDate(e.target.value)}
            required
          />
        </div>
        <button
          type="submit"
          className="w-full bg-red-500 text-white py-2 rounded hover:bg-red-600"
        >
          등록
        </button>
      </form>
    </div>
  );
} 