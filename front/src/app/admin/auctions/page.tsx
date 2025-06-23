'use client';

import { useEffect, useState } from 'react';
import { useAuctionStore } from '@/store/admin/auctionStore';
import Link from 'next/link';

export default function AuctionListPage() {
    const { auctions, loading, fetchAuctions } = useAuctionStore();
    const [search, setSearch] = useState('');

    useEffect(() => {
        fetchAuctions();
    }, [fetchAuctions]);

    const filteredAuctions = auctions
        .filter(a => a && typeof a === 'object')
        .filter(a =>
            a.adminProduct?.productName?.toLowerCase().includes(search.toLowerCase()) ||
            a.statusText?.toLowerCase().includes(search.toLowerCase()) ||
            a.status?.toLowerCase().includes(search.toLowerCase())
        );

    if (loading) return <div>로딩 중...</div>;

    return (
        <div className="p-8">
            <h1 className="text-2xl font-bold mb-6">경매 목록</h1>
            <input
                type="text"
                placeholder="상품명 또는 상태로 검색"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="border border-gray-300 p-2 mb-4 rounded w-64"
            />
            <table className="w-full border-collapse border border-gray-300">
                <thead>
                <tr className="bg-gray-100">
                    <th className="border border-gray-300 p-2">ID</th>
                    <th className="border border-gray-300 p-2">상품명</th>
                    <th className="border border-gray-300 p-2">시작가</th>
                    <th className="border border-gray-300 p-2">현재가</th>
                    <th className="border border-gray-300 p-2">상태</th>
                    <th className="border border-gray-300 p-2">시작일</th>
                    <th className="border border-gray-300 p-2">마감일</th>
                    <th className="border border-gray-300 p-2">판매자 ID</th>
                </tr>
                </thead>
                <tbody>
                {filteredAuctions.map(a => (
                    <tr key={a.id} className="hover:bg-gray-50">
                        <td className="border border-gray-300 p-2">
                            <Link href={`/admin/auctions/${a.id}`} className="text-blue-600 hover:text-blue-800">
                                {a.id}
                            </Link>
                        </td>
                        <td className="border border-gray-300 p-2">{a.adminProduct?.productName || 'N/A'}</td>
                        <td className="border border-gray-300 p-2">{a.startPrice?.toLocaleString()}원</td>
                        <td className="border border-gray-300 p-2">{a.currentPrice ? `${a.currentPrice.toLocaleString()}원` : '-'}</td>
                        <td className="border border-gray-300 p-2">
                            <span className={`px-2 py-1 rounded text-sm font-medium ${
                                a.statusText === '진행중' ? 'bg-green-100 text-green-800' :
                                a.statusText === '예정' ? 'bg-blue-100 text-blue-800' :
                                a.statusText === '종료' ? 'bg-gray-100 text-gray-800' :
                                'bg-red-100 text-red-800'
                            }`}>
                                {a.statusText || a.status}
                            </span>
                        </td>
                        <td className="border border-gray-300 p-2">{a.startTime ? new Date(a.startTime).toLocaleDateString() : 'N/A'}</td>
                        <td className="border border-gray-300 p-2">{a.endTime ? new Date(a.endTime).toLocaleDateString() : 'N/A'}</td>
                        <td className="border border-gray-300 p-2">{a.adminProduct?.purchasedFromSellerId || 'N/A'}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}