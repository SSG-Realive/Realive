'use client';

import SellerLayout from '@/components/layouts/SellerLayout';
import { useEffect, useState } from 'react';

// 임시 API 함수 예시 (실제 서비스 함수로 교체 필요)
async function fetchSellerQnaList() {
  const res = await fetch('/api/seller/admin-qna');
  return res.json();
}
async function postSellerQna({ title, content }: { title: string; content: string }) {
  await fetch('/api/seller/admin-qna', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ title, content }),
  });
}

export default function SellerAdminQnaPage() {
  const [qnaList, setQnaList] = useState<any[]>([]);
  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchSellerQnaList().then(data => setQnaList(data.content || []));
  }, []);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    await postSellerQna({ title, content });
    setTitle('');
    setContent('');
    fetchSellerQnaList().then(data => setQnaList(data.content || []));
    setLoading(false);
  };

  return (
    <SellerLayout>
      <main className="min-h-screen w-full px-4 py-8 bg-white">
        <h1 className="text-2xl font-bold mb-6">관리자 문의</h1>
        <form onSubmit={handleSubmit} className="mb-8 space-y-3">
          <input
            className="w-full border rounded px-3 py-2"
            value={title}
            onChange={e => setTitle(e.target.value)}
            placeholder="제목"
            required
          />
          <textarea
            className="w-full border rounded px-3 py-2"
            value={content}
            onChange={e => setContent(e.target.value)}
            placeholder="문의 내용"
            required
          />
          <button
            type="submit"
            className="bg-blue-600 text-white px-4 py-2 rounded"
            disabled={loading}
          >
            {loading ? '등록 중...' : '문의 등록'}
          </button>
        </form>
        <h2 className="text-xl font-semibold mb-4">문의 내역</h2>
        <ul className="space-y-4">
          {qnaList.length === 0 && <li className="text-gray-500">문의 내역이 없습니다.</li>}
          {qnaList.map(qna => (
            <li key={qna.id} className="border rounded p-4">
              <div className="font-bold">{qna.title}</div>
              <div className="text-gray-700 mb-2">{qna.content}</div>
              <div className="text-xs text-gray-400 mb-1">작성일: {qna.created_at}</div>
              {qna.is_answered && (
                <div className="mt-2 bg-green-50 border-l-4 border-green-400 p-2">
                  <div className="font-semibold text-green-700">관리자 답변</div>
                  <div>{qna.answer}</div>
                  <div className="text-xs text-gray-400">답변일: {qna.answered_at}</div>
                </div>
              )}
            </li>
          ))}
        </ul>
      </main>
    </SellerLayout>
  );
} 