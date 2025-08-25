'use client';

import { useEffect, useState } from 'react';
import { useAuctionStore } from '@/store/customer/auctionStore';
import AuctionCard from '@/components/customer/auctions/AuctionCard';
import CategoryFilter from '@/components/customer/auctions/CategoryFilter';

export default function MainAuctionsPage() {
  const { auctions, loading, fetchAuctions, category, setCategory } = useAuctionStore();

  useEffect(() => {
    fetchAuctions();
  }, [fetchAuctions]);

  useEffect(() => {
    if (category) {
      fetchAuctions();
    }
  }, [category, fetchAuctions]);

  if (loading && auctions.length === 0) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-lg">경매 목록을 불러오는 중...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-4">진행중인 경매</h1>
          <CategoryFilter 
            category={category}
            onChange={setCategory}
            disabled={loading}
          />
        </div>

        {auctions.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-500 text-lg">진행중인 경매가 없습니다.</p>
          </div>
        ) : (
          <ul className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
            {auctions.map((auction, index) => (
              <AuctionCard
                key={auction.id}
                auction={auction}
                isLast={index === auctions.length - 1}
                refCallback={() => {}}
              />
            ))}
          </ul>
        )}
      </div>
    </div>
  );
} 