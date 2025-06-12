'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import SellerLayout from '@/components/layouts/SellerLayout';
import Header from '@/components/Header';
import { getSellerSettlementList } from '@/service/sellerSettlementService';
import { SellerSettlementResponse } from '@/types/sellerSettlement';

export default function SellerSettlementPage() {
    const router = useRouter();
    const [payouts, setPayouts] = useState<SellerSettlementResponse[]>([]);
    const [page, setPage] = useState(1);
    const [totalPages, setTotalPages] = useState(1);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                const res = await getSellerSettlementList({ page, size: 10 });
                setPayouts(res.content || []);
                setTotalPages(res.totalPages || 1);
                setError(null);
            } catch (err) {
                console.error('정산 목록 조회 실패:', err);
                setError('정산 데이터를 불러오는 데 실패했습니다.');
            } finally {
                setLoading(false);
            }
        };
        fetchData();
    }, [page]);

    return (
        <SellerLayout>
            <Header />
            <div className="max-w-4xl mx-auto p-6">
                <h1 className="text-2xl font-bold mb-6">판매자 정산 내역</h1>

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

                <div className="mt-6 flex justify-center items-center gap-2">
                    <button
                        onClick={() => setPage((prev) => Math.max(prev - 1, 1))}
                        disabled={page === 1}
                        className="px-3 py-1 border rounded disabled:opacity-50"
                    >
                        이전
                    </button>
                    <span className="text-sm">
                        {page} / {totalPages}
                    </span>
                    <button
                        onClick={() => setPage((prev) => Math.min(prev + 1, totalPages))}
                        disabled={page === totalPages}
                        className="px-3 py-1 border rounded disabled:opacity-50"
                    >
                        다음
                    </button>
                </div>
            </div>
        </SellerLayout>
    );
}