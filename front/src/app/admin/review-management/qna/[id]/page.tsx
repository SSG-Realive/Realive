"use client";
import { useParams } from "next/navigation";

// 더미 Q&A 데이터
const dummyQna = [
  { id: "1", user: "user4", title: "배송은 언제 오나요?", created: "2024-06-01", answered: true, answer: "배송은 2~3일 소요됩니다." },
  { id: "2", user: "user5", title: "환불 문의", created: "2024-06-02", answered: false, answer: "" },
  { id: "3", user: "user6", title: "제품 사용법 문의", created: "2024-06-03", answered: true, answer: "제품 설명서를 참고해 주세요." },
  { id: "4", user: "user7", title: "A/S 신청 방법?", created: "2024-06-04", answered: false, answer: "" },
  { id: "5", user: "user8", title: "재입고 일정", created: "2024-06-05", answered: true, answer: "6월 말 예정입니다." },
];

export default function QnaDetailPage() {
  const params = useParams();
  const { id } = params;

  const qna = dummyQna.find(q => q.id === id);

  if (!qna) {
    return <div style={{ padding: 32 }}>Q&A 정보를 찾을 수 없습니다.</div>;
  }

  return (
    <div style={{ padding: 32 }}>
      <h2 style={{ fontWeight: 'bold', fontSize: 24 }}>Q&amp;A 상세 페이지</h2>
      <p><b>제목:</b> {qna.title}</p>
      <p><b>작성자:</b> {qna.user}</p>
      <p><b>작성일:</b> {qna.created}</p>
      <p><b>답변상태:</b> {qna.answered ? "답변완료" : "미답변"}</p>
      {qna.answered && <p><b>답변:</b> {qna.answer}</p>}
    </div>
  );
} 