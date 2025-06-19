"use client";
import React, { useState, useEffect } from "react";
import Link from "next/link";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { ScrollArea, ScrollBar } from "@/components/ui/scroll-area";
import { useRouter } from "next/navigation";

// 더미 데이터 타입
interface Review {
  id: number;
  product: string;
  user: string;
  content: string;
  date: string;
  status: "정상" | "신고됨";
  productImage: string;
  userImage: string;
}
interface Qna {
  id: number;
  user: string;
  title: string;
  created: string;
  answered: boolean;
  userImage: string;
}

const dummyReviews: Review[] = [
  { id: 1, product: "노트북", user: "user1", content: "좋아요!", date: "2024-06-01", status: "정상", productImage: "https://randomuser.me/api/portraits/men/41.jpg", userImage: "https://randomuser.me/api/portraits/men/51.jpg" },
  { id: 2, product: "키보드", user: "user2", content: "별로예요", date: "2024-06-02", status: "신고됨", productImage: "https://randomuser.me/api/portraits/men/42.jpg", userImage: "https://randomuser.me/api/portraits/women/52.jpg" },
  { id: 3, product: "마우스", user: "user3", content: "만족", date: "2024-06-03", status: "정상", productImage: "https://randomuser.me/api/portraits/men/43.jpg", userImage: "https://randomuser.me/api/portraits/men/53.jpg" },
  { id: 4, product: "모니터", user: "user4", content: "화질 좋아요", date: "2024-06-04", status: "정상", productImage: "https://randomuser.me/api/portraits/men/44.jpg", userImage: "https://randomuser.me/api/portraits/women/54.jpg" },
  { id: 5, product: "의자", user: "user5", content: "편해요", date: "2024-06-05", status: "정상", productImage: "https://randomuser.me/api/portraits/men/45.jpg", userImage: "https://randomuser.me/api/portraits/men/55.jpg" },
  { id: 6, product: "책상", user: "user6", content: "튼튼합니다", date: "2024-06-06", status: "신고됨", productImage: "https://randomuser.me/api/portraits/men/46.jpg", userImage: "https://randomuser.me/api/portraits/women/56.jpg" },
  { id: 7, product: "스피커", user: "user7", content: "음질 좋아요", date: "2024-06-07", status: "정상", productImage: "https://randomuser.me/api/portraits/men/47.jpg", userImage: "https://randomuser.me/api/portraits/men/57.jpg" },
  { id: 8, product: "프린터", user: "user8", content: "빠릅니다", date: "2024-06-08", status: "정상", productImage: "https://randomuser.me/api/portraits/men/48.jpg", userImage: "https://randomuser.me/api/portraits/women/58.jpg" },
  { id: 9, product: "마우스패드", user: "user9", content: "부드러워요", date: "2024-06-09", status: "정상", productImage: "https://randomuser.me/api/portraits/men/49.jpg", userImage: "https://randomuser.me/api/portraits/men/59.jpg" },
  { id: 10, product: "램프", user: "user10", content: "밝아요", date: "2024-06-10", status: "신고됨", productImage: "https://randomuser.me/api/portraits/men/50.jpg", userImage: "https://randomuser.me/api/portraits/women/60.jpg" },
];

interface ReportedReview {
  product: string;
  user: string;
  reason: string;
  status: string;
  userImage: string;
}

const dummyReported: ReportedReview[] = [
  { product: "키보드", user: "user2", reason: "욕설", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/52.jpg" },
  { product: "의자", user: "user5", reason: "광고성", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/55.jpg" },
  { product: "책상", user: "user6", reason: "도배", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/56.jpg" },
  { product: "마우스", user: "user3", reason: "부적절한 내용", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/53.jpg" },
  { product: "모니터", user: "user4", reason: "욕설", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/54.jpg" },
  { product: "노트북", user: "user1", reason: "도배", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/51.jpg" },
  { product: "스피커", user: "user7", reason: "욕설", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/57.jpg" },
  { product: "프린터", user: "user8", reason: "광고성", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/58.jpg" },
  { product: "마우스패드", user: "user9", reason: "도배", status: "신고됨", userImage: "https://randomuser.me/api/portraits/men/59.jpg" },
  { product: "램프", user: "user10", reason: "욕설", status: "신고됨", userImage: "https://randomuser.me/api/portraits/women/60.jpg" },
];

const dummyQna: Qna[] = [
  { id: 1, user: "user4", title: "배송은 언제 오나요?", created: "2024-06-01", answered: true, userImage: "https://randomuser.me/api/portraits/women/54.jpg" },
  { id: 2, user: "user5", title: "환불 문의", created: "2024-06-02", answered: false, userImage: "https://randomuser.me/api/portraits/men/55.jpg" },
  { id: 3, user: "user6", title: "제품 사용법 문의", created: "2024-06-03", answered: true, userImage: "https://randomuser.me/api/portraits/women/56.jpg" },
  { id: 4, user: "user7", title: "A/S 신청 방법?", created: "2024-06-04", answered: false, userImage: "https://randomuser.me/api/portraits/men/57.jpg" },
  { id: 5, user: "user8", title: "재입고 일정", created: "2024-06-05", answered: true, userImage: "https://randomuser.me/api/portraits/women/58.jpg" },
  { id: 6, user: "user9", title: "쿠폰 적용 문의", created: "2024-06-06", answered: false, userImage: "https://randomuser.me/api/portraits/men/59.jpg" },
  { id: 7, user: "user10", title: "제품 호환성 문의", created: "2024-06-07", answered: true, userImage: "https://randomuser.me/api/portraits/women/60.jpg" },
  { id: 8, user: "user11", title: "배송지 변경", created: "2024-06-08", answered: false, userImage: "https://randomuser.me/api/portraits/men/61.jpg" },
  { id: 9, user: "user12", title: "결제 오류", created: "2024-06-09", answered: true, userImage: "https://randomuser.me/api/portraits/women/62.jpg" },
  { id: 10, user: "user13", title: "포인트 적립 문의", created: "2024-06-10", answered: false, userImage: "https://randomuser.me/api/portraits/men/63.jpg" },
];

export default function ReviewManagementPage() {
  const [tab, setTab] = useState<'review'|'reported'|'qna'>('review');
  const [reviewSearch, setReviewSearch] = useState("");
  const [reportedSearch, setReportedSearch] = useState("");
  const [qnaSearch, setQnaSearch] = useState("");
  const [selectedReview, setSelectedReview] = useState<Review | null>(null);
  const [selectedReported, setSelectedReported] = useState<ReportedReview | null>(null);
  const [selectedQna, setSelectedQna] = useState<Qna | null>(null);
  const router = useRouter();

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  const filteredReviews = dummyReviews.filter(r => r.product.includes(reviewSearch) || r.user.includes(reviewSearch));
  const filteredReported = dummyReported.filter(r => r.product.includes(reportedSearch) || r.user.includes(reportedSearch) || r.reason.includes(reportedSearch));
  const filteredQna = dummyQna.filter(q => q.title.includes(qnaSearch) || q.user.includes(qnaSearch));

  return (
    <div>
      <div className="p-8 flex flex-row gap-8 overflow-x-auto">
        {/* 리뷰 목록 요약 - 테이블형 */}
        <div className="block bg-white rounded shadow p-6 min-w-[400px] cursor-pointer hover:bg-gray-50 transition">
          <h2 className="text-lg font-bold mb-4">리뷰</h2>
          <table className="min-w-full border text-sm">
            <thead>
              <tr className="bg-gray-100">
                <th className="px-2 py-1">상품</th>
                <th className="px-2 py-1">작성자</th>
                <th className="px-2 py-1">날짜</th>
                <th className="px-2 py-1">상태</th>
              </tr>
            </thead>
            <tbody>
              {filteredReviews.slice(0, 5).map(r => (
                <tr key={r.id}>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{r.product}</td>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{r.user}</td>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{r.date}</td>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{r.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {/* 리뷰 신고 관리 요약 - 테이블형 */}
        <div className="block bg-white rounded shadow p-6 min-w-[400px] cursor-pointer hover:bg-gray-50 transition">
          <h2 className="text-lg font-bold mb-4">리뷰 신고 관리</h2>
          <table className="min-w-full border text-sm">
            <thead>
              <tr className="bg-gray-100">
                <th className="px-2 py-1">상품</th>
                <th className="px-2 py-1">작성자</th>
                <th className="px-2 py-1">사유</th>
                <th className="px-2 py-1">상태</th>
              </tr>
            </thead>
            <tbody>
              {filteredReported.slice(0, 5).map((r, idx) => (
                <tr key={idx}>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{r.product}</td>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{r.user}</td>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{r.reason}</td>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{r.status}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        {/* Q&A 관리 요약 - 테이블형 */}
        <div className="block bg-white rounded shadow p-6 min-w-[400px] cursor-pointer hover:bg-gray-50 transition">
          <h2 className="text-lg font-bold mb-4">Q&A 관리</h2>
          <table className="min-w-full border text-sm">
            <thead>
              <tr className="bg-gray-100">
                <th className="px-2 py-1">제목</th>
                <th className="px-2 py-1">작성자</th>
                <th className="px-2 py-1">날짜</th>
                <th className="px-2 py-1">답변상태</th>
              </tr>
            </thead>
            <tbody>
              {filteredQna.slice(0, 5).map(q => (
                <tr key={q.id}>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{q.title}</td>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{q.user}</td>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{q.created}</td>
                  <td style={{ color: 'inherit', textDecoration: 'none', fontWeight: 'normal' }}>{q.answered ? "답변완료" : "미답변"}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
      {selectedReview && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[320px]">
            <h2 className="text-xl font-bold mb-4">리뷰 상세</h2>
            <div className="flex items-center gap-4 mb-4">
              <img src={selectedReview.productImage} alt={selectedReview.product} className="w-16 h-16 rounded-full border" />
              <img src={selectedReview.userImage} alt={selectedReview.user} className="w-16 h-16 rounded-full border" />
              <div>
                <p><b>상품명:</b> {selectedReview.product}</p>
                <p><b>작성자:</b> {selectedReview.user}</p>
                <p><b>내용:</b> {selectedReview.content}</p>
                <p><b>작성일:</b> {selectedReview.date}</p>
                <p><b>상태:</b> {selectedReview.status}</p>
              </div>
            </div>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelectedReview(null)}>닫기</button>
          </div>
        </div>
      )}
      {selectedReported && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[320px]">
            <h2 className="text-xl font-bold mb-4">신고 상세</h2>
            <div className="flex items-center gap-4 mb-4">
              <img src={selectedReported.userImage} alt={selectedReported.user} className="w-16 h-16 rounded-full border" />
              <div>
                <p><b>상품명:</b> {selectedReported.product}</p>
                <p><b>작성자:</b> {selectedReported.user}</p>
                <p><b>사유:</b> {selectedReported.reason}</p>
                <p><b>상태:</b> {selectedReported.status}</p>
              </div>
            </div>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelectedReported(null)}>닫기</button>
          </div>
        </div>
      )}
      {selectedQna && (
        <div className="fixed inset-0 bg-black bg-opacity-30 flex items-center justify-center z-50">
          <div className="bg-white rounded-lg p-8 min-w-[320px]">
            <h2 className="text-xl font-bold mb-4">Q&A 상세</h2>
            <div className="flex items-center gap-4 mb-4">
              <img src={selectedQna.userImage} alt={selectedQna.user} className="w-16 h-16 rounded-full border" />
              <div>
                <p><b>작성자:</b> {selectedQna.user}</p>
                <p><b>제목:</b> {selectedQna.title}</p>
                <p><b>작성일:</b> {selectedQna.created}</p>
                <p><b>답변상태:</b> {selectedQna.answered ? "답변완료" : "미답변"}</p>
              </div>
            </div>
            <button className="mt-4 px-4 py-2 bg-blue-500 text-white rounded" onClick={() => setSelectedQna(null)}>닫기</button>
          </div>
        </div>
      )}
    </div>
  );
} 