"use client";
import React, { useState } from 'react';

const qnaList = [
  { id: 1, user: 'user4', title: '배송은 언제 오나요?', created: '2024-06-23', answered: '2024-06-24', status: '답변완료', detail: '배송은 2~3일 소요됩니다.' },
  { id: 2, user: 'user3', title: '환불 문의합니다', created: '2024-06-22', answered: '', status: '미답변', detail: '환불은 영업일 기준 3일 내 처리됩니다.' },
];

export default function QnaPage() {
  const [selected, setSelected] = useState(null as null | typeof qnaList[0]);
  return (
    <div style={{ minHeight: '100vh', background: '#f4f4f4', padding: 24 }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 24 }}>
        <h2 style={{ fontWeight: 'bold', fontSize: 24 }}>Q&amp;A</h2>
        <span style={{ fontWeight: 'bold' }}>admin</span>
      </header>
      {/* 테이블 */}
      <table style={{ width: '100%', borderCollapse: 'collapse', background: '#fff', fontSize: 15, marginBottom: 24 }}>
        <thead>
          <tr style={{ background: '#f7f7f7' }}>
            <th style={{ padding: 8, border: '1px solid #eee' }}>번호</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>작성자</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>제목</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>작성일</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>답변일</th>
            <th style={{ padding: 8, border: '1px solid #eee' }}>상태</th>
          </tr>
        </thead>
        <tbody>
          {qnaList.map((q, idx) => (
            <tr key={q.id} onClick={() => setSelected(q)} style={{ cursor: 'pointer' }}>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{idx + 1}</td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{q.user}</td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{q.title}</td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{q.created}</td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{q.answered || '-'}</td>
              <td style={{ padding: 8, border: '1px solid #eee' }}>{q.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
      {/* 상세 모달 */}
      {selected && (
        <div style={{ position: 'fixed', left: 0, top: 0, width: '100vw', height: '100vh', background: 'rgba(0,0,0,0.2)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
          <div style={{ background: '#fff', borderRadius: 8, minWidth: 320, minHeight: 180, padding: 24, position: 'relative', boxShadow: '0 2px 8px #aaa' }}>
            <button onClick={() => setSelected(null)} style={{ position: 'absolute', top: 12, right: 12, background: 'none', border: 'none', fontSize: 20, cursor: 'pointer' }}>×</button>
            <h3>QnA 상세 보기</h3>
            <div style={{ marginBottom: 8 }}><b>제목</b> {selected.title}</div>
            <div style={{ marginBottom: 8 }}><b>내용</b> {selected.detail}</div>
            <div style={{ marginBottom: 8 }}><b>작성자</b> {selected.user}</div>
            <div style={{ marginBottom: 8 }}><b>작성일</b> {selected.created}</div>
            <div style={{ marginBottom: 8 }}><b>답변일</b> {selected.answered || '-'}</div>
            <div style={{ marginTop: 16, display: 'flex', gap: 8 }}>
              <button style={{ background: '#222', color: '#fff', border: 'none', borderRadius: 4, padding: '4px 16px' }}>수정</button>
              <button style={{ background: '#ff5252', color: '#fff', border: 'none', borderRadius: 4, padding: '4px 16px' }}>삭제</button>
              <button style={{ background: '#bdbdbd', color: '#222', border: 'none', borderRadius: 4, padding: '4px 16px' }} onClick={() => setSelected(null)}>닫기</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
} 