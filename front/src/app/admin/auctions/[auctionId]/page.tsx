'use client';

import { useEffect } from 'react';
import { useParams } from 'next/navigation';
import { useAuctionStore } from '@/store/admin/auctionStore';

export default function AuctionDetailPage() {
    const params = useParams<{ auctionId: string }>();
    const auctionId = Number(params.auctionId);
    const {
        auctionDetail,
        auctionDetailLoading,
        fetchAuctionById,
    } = useAuctionStore();

    useEffect(() => {
        if (!isNaN(auctionId)) {
            fetchAuctionById(auctionId);
        }
    }, [auctionId, fetchAuctionById]);

    if (auctionDetailLoading || !auctionDetail) return <div>로딩 중...</div>;

    const auction = auctionDetail;

    return (
        <div>
            <h1>{auction.name}</h1>
            {auction.productImage &&
                <img src={auction.productImage} alt={auction.productName} />}
            <p>{auction.description}</p>
            <div>시작가: {auction.startPrice}</div>
            <div>현재가: {auction.currentPrice}</div>
            <div>상태: {auction.status}</div>
            <div>판매자: {auction.sellerName}</div>
            {/* 필요시 추가 정보 더 표시 가능 */}
        </div>
    );
}