'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { getQnaList } from '@/service/seller/sellerQnaService';
import { SellerQnaResponse } from '@/types/seller/sellerqna/sellerQnaResponse';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import SellerHeader from '@/components/seller/SellerHeader';
import { MessageCircle, CheckCircle, Clock, Plus, Eye, Search, Filter } from 'lucide-react';

export default function SellerQnaPage() {
    const checking = useSellerAuthGuard(); // ✅ 가드 적용 (일관성 유지)

    const router = useRouter();

    const [qnaList, setQnaList] = useState<SellerQnaResponse[]>([]);
    const [totalPages, setTotalPages] = useState(1);
    const [page, setPage] = useState(0);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [searchKeyword, setSearchKeyword] = useState('');
    const [statusFilter, setStatusFilter] = useState('');

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    useEffect(() => {
        if (checking) return;
        
        const fetchData = async () => {
            try {
                setLoading(true);
                const res = await getQnaList({ page, size: 10 });
                setQnaList(res.content || []);
                setTotalPages(res.totalPages || 1);
                setError(null);
            } catch (err) {
                console.error('QnA 목록 조회 실패:', err);
                setError('QnA 데이터를 불러오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [page, checking]);

    // 통계 계산
    const totalQna = qnaList.length;
    const answeredQna = qnaList.filter(qna => qna.isAnswered).length;
    const unansweredQna = qnaList.filter(qna => !qna.isAnswered).length;
    const activeQna = qnaList.filter(qna => qna.isActive).length;

    // 필터링된 QnA 목록
    const filteredQnaList = qnaList.filter(qna => {
        const matchesKeyword = searchKeyword === '' || 
            qna.title.toLowerCase().includes(searchKeyword.toLowerCase()) ||
            qna.content.toLowerCase().includes(searchKeyword.toLowerCase());
        
        const matchesStatus = statusFilter === '' || 
            (statusFilter === 'answered' && qna.isAnswered) ||
            (statusFilter === 'unanswered' && !qna.isAnswered);
        
        return matchesKeyword && matchesStatus;
    });

    const getStatusBadge = (isAnswered: boolean) => {
        return isAnswered ? (
            <span className="px-2 py-1 rounded text-xs font-bold bg-[#e9dec7] text-[#5b4636] flex items-center gap-1">
                <CheckCircle className="w-3 h-3" />
                답변 완료
            </span>
        ) : (
            <span className="px-2 py-1 rounded text-xs font-bold bg-[#fbeee0] text-[#b94a48] flex items-center gap-1">
                <Clock className="w-3 h-3" />
                미답변
            </span>
        );
    };

    if (checking || loading) {
        return (
            <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-[#a89f91] flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#bfa06a] mx-auto mb-4"></div>
                    <p className="text-[#5b4636]">문의 정보를 불러오는 중...</p>
                </div>
            </div>
        );
    }

    return (
        <>
            <div className="hidden">
                <SellerHeader toggleSidebar={toggleSidebar} />
            </div>
            <SellerLayout>
                <div className="flex-1 w-full h-full px-4 py-8 bg-[#a89f91]">
                    <h1 className="text-xl md:text-2xl font-bold mb-6 text-[#5b4636]">고객 문의 관리</h1>

                    {/* 상단 통계 카드 */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-6">
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">총 문의 수</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#5b4636]">{totalQna}건</p>
                            </div>
                            <MessageCircle className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">답변 완료</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#388e3c]">{answeredQna}건</p>
                            </div>
                            <CheckCircle className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">미답변</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#b94a48]">{unansweredQna}건</p>
                            </div>
                            <Clock className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">활성 문의</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#bfa06a]">{activeQna}건</p>
                            </div>
                            <div className="flex items-center gap-2">
                                <button
                                    onClick={() => router.push('/seller/qna/new')}
                                    className="bg-[#bfa06a] text-[#4b3a2f] px-3 py-1.5 rounded hover:bg-[#5b4636] hover:text-[#e9dec7] flex items-center gap-1 text-sm"
                                >
                                    <Plus className="w-3 h-3" />
                                    문의 등록
                                </button>
                            </div>
                        </section>
                    </div>

                    {/* 검색/필터 영역 */}
                    <div className="flex flex-col md:flex-row gap-3 md:gap-4 mb-6 items-center">
                        <input
                            type="text"
                            placeholder="제목, 내용으로 검색"
                            value={searchKeyword}
                            onChange={(e) => setSearchKeyword(e.target.value)}
                            className="flex-1 border border-[#bfa06a] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
                        />
                        <select
                            value={statusFilter}
                            onChange={(e) => setStatusFilter(e.target.value)}
                            className="border border-[#bfa06a] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
                        >
                            <option value="">전체 상태</option>
                            <option value="answered">답변 완료</option>
                            <option value="unanswered">미답변</option>
                        </select>
                        <button 
                            className="bg-[#bfa06a] text-[#4b3a2f] px-4 py-2 rounded-md hover:bg-[#5b4636] hover:text-[#e9dec7] flex items-center gap-2"
                        >
                            <Search className="w-4 h-4" />
                            검색
                        </button>
                    </div>

                    {/* QnA 리스트 */}
                    {error ? (
                        <div className="bg-[#fbeee0] border border-[#bfa06a] rounded-lg p-4">
                            <p className="text-[#b94a48]">{error}</p>
                        </div>
                    ) : filteredQnaList.length === 0 ? (
                        <div className="bg-[#e9dec7] border border-[#bfa06a] rounded-lg p-8 text-center">
                            <MessageCircle className="w-12 h-12 text-[#bfa06a] mx-auto mb-4" />
                            <p className="text-[#bfa06a] text-lg">문의가 없습니다.</p>
                        </div>
                    ) : (
                        <div className="overflow-x-auto bg-[#e9dec7] rounded-lg shadow-sm border border-[#bfa06a]">
                            <table className="min-w-full divide-y divide-[#bfa06a]">
                                <thead className="bg-[#e9dec7]">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">제목</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">상태</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">등록일</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">답변일</th>
                                        <th className="px-6 py-3 text-center text-xs font-medium text-[#bfa06a] uppercase tracking-wider">액션</th>
                                    </tr>
                                </thead>
                                <tbody className="bg-[#e9dec7] divide-y divide-[#bfa06a]">
                                    {filteredQnaList.map((qna) => (
                                        <tr key={qna.id} className="hover:bg-[#bfa06a] transition-colors">
                                            <td className="px-6 py-4 whitespace-nowrap font-medium text-[#5b4636] max-w-xs truncate">
                                                {qna.title}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                {getStatusBadge(qna.isAnswered)}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-[#5b4636]">
                                                {qna.createdAt}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-[#5b4636]">
                                                {qna.answeredAt || '-'}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-center">
                                                <button
                                                    onClick={() => router.push(`/seller/qna/${qna.id}`)}
                                                    className="inline-flex items-center gap-1 bg-[#bfa06a] text-[#4b3a2f] px-3 py-1.5 rounded hover:bg-[#5b4636] hover:text-[#e9dec7] text-sm"
                                                >
                                                    <Eye className="w-4 h-4" /> 상세 보기
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}

                    {/* 페이지네이션 */}
                    {totalPages > 1 && (
                        <div className="mt-6 flex gap-2 justify-center items-center">
                            <button
                                onClick={() => setPage((p) => Math.max(p - 1, 0))}
                                disabled={page === 0}
                                className="px-3 py-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                            >
                                이전
                            </button>
                            <span className="text-sm px-4 py-2 bg-white border border-gray-300 rounded-md">
                                {page + 1} / {totalPages}
                            </span>
                            <button
                                onClick={() => setPage((p) => Math.min(p + 1, totalPages - 1))}
                                disabled={page >= totalPages - 1}
                                className="px-3 py-2 border border-gray-300 rounded-md disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
                            >
                                다음
                            </button>
                        </div>
                    )}
                </div>
            </SellerLayout>
        </>
    );
}