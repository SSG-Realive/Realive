'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import SellerLayout from '@/components/layouts/SellerLayout';
import SellerHeader from '@/components/seller/SellerHeader';
import {
    getSellerSettlementList,
    getSellerSettlementListByDate,
} from '@/service/seller/sellerSettlementService';
import { SellerSettlementResponse } from '@/types/seller/sellersettlement/sellerSettlement';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { DollarSign, TrendingUp, Percent, CreditCard, Calendar, Search, RefreshCw } from 'lucide-react';

export default function SellerSettlementPage() {
    const checking = useSellerAuthGuard();

    const router = useRouter();
    const [payouts, setPayouts] = useState<SellerSettlementResponse[]>([]);
    const [filterDate, setFilterDate] = useState(''); // ✅ 날짜 필터 상태
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);
    const [sidebarOpen, setSidebarOpen] = useState(false);

    const toggleSidebar = () => {
        setSidebarOpen(!sidebarOpen);
    };

    const fetchAll = async () => {
        try {
            setLoading(true);
            const res = await getSellerSettlementList();
            setPayouts(res || []);
            setError(null);
        } catch (err) {
            console.error('정산 목록 조회 실패:', err);
            setError('정산 데이터를 불러오는 데 실패했습니다.');
        } finally {
            setLoading(false);
        }
    };

    const fetchFiltered = async (date: string) => {
        try {
            setLoading(true);
            const res = await getSellerSettlementListByDate(date);
            setPayouts(res || []);
            setError(null);
        } catch (err) {
            console.error('날짜 필터 조회 실패:', err);
            setError('해당 날짜의 정산 데이터를 불러오지 못했습니다.');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (checking) return;
        fetchAll(); // 초기 전체 조회
    }, [checking]);

    // 통계 계산
    const totalSettlements = payouts.length;
    const totalSales = payouts.reduce((sum, item) => sum + item.totalSales, 0);
    const totalCommission = payouts.reduce((sum, item) => sum + item.totalCommission, 0);
    const totalPayout = payouts.reduce((sum, item) => sum + item.payoutAmount, 0);

    if (checking || loading) {
        return (
            <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-[#a89f91] flex items-center justify-center">
                <div className="text-center">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-[#bfa06a] mx-auto mb-4"></div>
                    <p className="text-[#5b4636]">정산 정보를 불러오는 중...</p>
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
                    <h1 className="text-xl md:text-2xl font-bold mb-6 text-[#5b4636]">정산 관리</h1>

                    {/* 상단 통계 카드 */}
                    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 md:gap-6 mb-6">
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">총 정산 건수</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#5b4636]">{totalSettlements}건</p>
                            </div>
                            <CreditCard className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">총 매출</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#388e3c]">{totalSales.toLocaleString()}원</p>
                            </div>
                            <TrendingUp className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">총 수수료</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#b94a48]">{totalCommission.toLocaleString()}원</p>
                            </div>
                            <Percent className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                        <section className="bg-[#e9dec7] p-4 md:p-6 rounded-lg shadow-sm border border-[#bfa06a] flex items-center justify-between">
                            <div>
                                <h2 className="text-[#5b4636] text-sm font-semibold mb-2">총 지급액</h2>
                                <p className="text-xl md:text-2xl font-bold text-[#bfa06a]">{totalPayout.toLocaleString()}원</p>
                            </div>
                            <DollarSign className="w-8 h-8 text-[#bfa06a]" />
                        </section>
                    </div>

                    {/* 날짜 필터 */}
                    <div className="flex flex-col md:flex-row gap-3 md:gap-4 mb-6 items-center">
                        <div className="flex items-center gap-2 flex-1">
                            <Calendar className="w-5 h-5 text-[#bfa06a]" />
                            <input
                                type="date"
                                value={filterDate}
                                onChange={(e) => setFilterDate(e.target.value)}
                                className="flex-1 border border-[#bfa06a] rounded-md px-3 py-2 focus:outline-none focus:ring-2 focus:ring-[#bfa06a] bg-[#e9dec7] text-[#5b4636]"
                            />
                        </div>
                        <button
                            onClick={() => fetchFiltered(filterDate)}
                            disabled={!filterDate}
                            className="flex items-center gap-2 bg-[#bfa06a] text-[#4b3a2f] px-4 py-2 rounded-md hover:bg-[#5b4636] hover:text-[#e9dec7] disabled:opacity-50 disabled:cursor-not-allowed"
                        >
                            <Search className="w-4 h-4" />
                            날짜 필터
                        </button>
                        <button
                            onClick={() => {
                                setFilterDate('');
                                fetchAll();
                            }}
                            className="flex items-center gap-2 bg-[#5b4636] text-[#e9dec7] px-4 py-2 rounded-md hover:bg-[#bfa06a] hover:text-[#4b3a2f]"
                        >
                            <RefreshCw className="w-4 h-4" />
                            전체 보기
                        </button>
                    </div>

                    {/* 정산 리스트 */}
                    {error ? (
                        <div className="bg-[#fbeee0] border border-[#bfa06a] rounded-lg p-4">
                            <p className="text-[#b94a48]">{error}</p>
                        </div>
                    ) : payouts.length === 0 ? (
                        <div className="bg-[#e9dec7] border border-[#bfa06a] rounded-lg p-8 text-center">
                            <CreditCard className="w-12 h-12 text-[#bfa06a] mx-auto mb-4" />
                            <p className="text-[#bfa06a] text-lg">정산 내역이 없습니다.</p>
                        </div>
                    ) : (
                        <div className="overflow-x-auto bg-[#e9dec7] rounded-lg shadow-sm border border-[#bfa06a]">
                            <table className="min-w-full divide-y divide-[#bfa06a]">
                                <thead className="bg-[#e9dec7]">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">정산 기간</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">총 매출</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">수수료</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">지급액</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-[#bfa06a] uppercase tracking-wider">처리일시</th>
                                    </tr>
                                </thead>
                                <tbody className="bg-[#e9dec7] divide-y divide-[#bfa06a]">
                                    {payouts.map((item) => (
                                        <tr key={item.id} className="hover:bg-[#bfa06a] transition-colors">
                                            <td className="px-6 py-4 whitespace-nowrap font-medium text-[#5b4636]">
                                                {item.periodStart} ~ {item.periodEnd}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-[#5b4636]">
                                                {item.totalSales.toLocaleString()}원
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-[#b94a48]">
                                                {item.totalCommission.toLocaleString()}원
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap font-semibold text-[#388e3c]">
                                                {item.payoutAmount.toLocaleString()}원
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-[#5b4636]">
                                                {item.processedAt}
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>
            </SellerLayout>
        </>
    );
}
