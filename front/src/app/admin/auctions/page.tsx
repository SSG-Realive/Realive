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
            a.name?.toLowerCase().includes(search.toLowerCase()) ||
            a.productName?.toLowerCase().includes(search.toLowerCase()) ||
            a.sellerName?.toLowerCase().includes(search.toLowerCase())
        );

    if (loading) return <div>로딩 중...</div>;

    return (
        <div>
            <input
                type="text"
                placeholder="검색어를 입력하세요"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="border border-gray-300 p-2 mb-4 rounded"
            />
            <table className="w-full border-collapse border border-gray-300">
                <thead>
                <tr>
                    <th>ID</th>
                    <th>경매명</th>
                    <th>상품명</th>
                    <th>시작가</th>
                    <th>현재가</th>
                    <th>상태</th>
                    <th>시작일</th>
                    <th>마감일</th>
                    <th>판매자</th>
                </tr>
                </thead>
                <tbody>
                {filteredAuctions.map(a => (
                    <tr key={a.id}>
                        <td>
                            <Link href={`/admin/auctions/${a.id}`}>{a.id}</Link>
                        </td>
                        <td>{a.name}</td>
                        <td>{a.productName}</td>
                        <td>{a.startPrice?.toLocaleString()}원</td>
                        <td>{a.currentPrice ? `${a.currentPrice.toLocaleString()}원` : '-'}</td>
                        <td>
                            {a.status === 'ACTIVE' ? '진행중' :
                                a.status === 'ENDED' ? '종료' :
                                    a.status === 'CANCELLED' ? '취소됨' : a.status}
                        </td>
                        <td>{a.startTime?.split('T')[0]}</td>
                        <td>{a.endTime?.split('T')[0]}</td>
                        <td>{a.sellerName}</td>
                    </tr>
                ))}
                </tbody>
            </table>
        </div>
    );
}