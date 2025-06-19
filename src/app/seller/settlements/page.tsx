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

export default function SellerSettlementPage() {
    useSellerAuthGuard();

    const router = useRouter();
    const [payouts, setPayouts] = useState<SellerSettlementResponse[]>([]);
    const [filterDate, setFilterDate] = useState(''); // ✅ 날짜 필터 상태
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

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

        fetchAll(); // 초기 전체 조회
    }, []);

    return (
        <SellerLayout>
            <SellerHeader />
            <div className="max-w-4xl mx-auto p-6">
                <h1 className="text-2xl font-bold mb-6">판매자 정산 내역</h1>

                {/* ✅ 날짜 필터 */}
                <div className="mb-4 flex items-center gap-2">
                    <input
                        type="date"
                        value={filterDate}

                        onChange={(e) => setFilterDate(e.target.value)}
                        className="border px-2 py-1 rounded"
                    />
                    <button
                        onClick={() => fetchFiltered(filterDate)}
                        disabled={!filterDate}
                        className="px-3 py-1 border rounded bg-blue-100 hover:bg-blue-200"
                    >
                        날짜 필터 조회
                    </button>
                    <button
                        onClick={() => {
                            setFilterDate('');
                            fetchAll();
                        }}
                        className="px-3 py-1 border rounded bg-gray-100 hover:bg-gray-200"
                    >
                        전체 보기
                    </button>
                </div>

                {/* ✅ 테이블  변경 사항이 있습니다*/}
                {loading ? (
                    <p className="text-gray-500">로딩 중...</p>
                ) : error ? (
                    <p className="text-red-500">{error}</p>
                ) : (
                    <div className="overflow-x-auto">
                        <table className="min-w-full text-sm text-left border">
                            <thead className="bg-gray-100">
                            <tr>
                                <th className="px-4 py-2 border">정산 기간</th>
                                <th className="px-4 py-2 border">총 매출</th>
                                <th className="px-4 py-2 border">수수료</th>
                                <th className="px-4 py-2 border">지급액</th>
                                <th className="px-4 py-2 border">처리일시</th>
                            </tr>
                            </thead>
                            <tbody>
                            {payouts.length > 0 ? (
                                payouts.map((item) => (
                                    <tr key={item.id} className="border-b">
                                        <td className="px-4 py-2 border">
                                            {item.periodStart} ~ {item.periodEnd}
                                        </td>
                                        <td className="px-4 py-2 border">{item.totalSales.toLocaleString()}원</td>
                                        <td className="px-4 py-2 border">{item.totalCommission.toLocaleString()}원</td>
                                        <td className="px-4 py-2 border font-semibold text-green-600">
                                            {item.payoutAmount.toLocaleString()}원
                                        </td>
                                        <td className="px-4 py-2 border">{item.processedAt}</td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan={5} className="text-center text-gray-400 py-4">
                                        정산 내역이 없습니다.
                                    </td>
                                </tr>
                            )}
                            </tbody>
                        </table>
                    </div>
                )}
            </div>
        </SellerLayout>
    );
}
