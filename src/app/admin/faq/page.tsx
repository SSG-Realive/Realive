import React from 'react';
import Link from 'next/link';

export default function AdminFAQPage() {
  return (
    <div>
      <h2>FAQ 관리</h2>
      <div style={{ marginBottom: 16 }}>
        <input type="text" placeholder="질문/작성자 검색" style={{ marginRight: 8 }} />
        <button>검색</button>
      </div>
      <table style={{ width: '100%', background: '#eee' }}>
        <thead>
          <tr>
            <th>질문</th>
            <th>작성자</th>
            <th>등록일</th>
            <th>상태</th>
            <th>답변</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>배송은 얼마나 걸리나요?</td>
            <td>user2</td>
            <td>2024-06-10</td>
            <td>미답변</td>
            <td><button>답변</button></td>
          </tr>
        </tbody>
      </table>
      <Link href="/admin/dashboard">Back to Dashboard</Link>
    </div>
  );
} 