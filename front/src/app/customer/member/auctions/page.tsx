'use client';

import Navbar from '@/components/customer/common/Navbar';
import AuctionCard from '@/components/customer/auctions/AuctionCard';
import CategoryFilter from '@/components/customer/auctions/CategoryFilter';
import DebugPanel from '@/components/customer/auctions/DebugPanel';

import { useThrottle } from '@/hooks/useThrottle';
import useRequireAuth from '@/hooks/useRequireAuth';
import { useAuctionStore } from '@/store/customer/auctionStore';

import { useCallback, useEffect, useRef } from 'react';

export default function AuctionsPage() {
  const token = useRequireAuth();
  const {
    auctions, category, page, hasNext, loading, error,
    setCategory, reset, fetchAuctions
  } = useAuctionStore();

  const observer = useRef<IntersectionObserver | null>(null);
  const isInitialized = useRef(false);

  const throttledFetch = useThrottle(() => {
    if (hasNext && !loading && token) fetchAuctions();
  }, 500);

  const lastAuctionRef = useCallback((node: HTMLLIElement | null) => {
    if (loading || !hasNext) return;
    if (observer.current) observer.current.disconnect();

    observer.current = new IntersectionObserver(entries => {
      if (entries[0].isIntersecting) throttledFetch();
    }, { rootMargin: '50px' });

    if (node) observer.current.observe(node);
  }, [loading, hasNext, throttledFetch]);

  useEffect(() => {
    if (token && !isInitialized.current && auctions.length === 0 && !loading) {
      isInitialized.current = true;
      fetchAuctions();
    }
  }, [token, auctions.length, loading, fetchAuctions]);

  useEffect(() => () => {
    if (observer.current) observer.current.disconnect();
    reset();
  }, [reset]);

  if (!token) return <div>로그인 확인 중...</div>;
  if (error) return <div style={{ color: 'red' }}>에러: {error}</div>;

  return (
    <>
      <Navbar />
      <div style={{ maxWidth: 900, margin: '0 auto', padding: 20 }}>
        <header style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
          <h1>경매 목록</h1>
        </header>

        <CategoryFilter
          category={category}
          onChange={(c) => {
            setCategory(c);
            isInitialized.current = false;
          }}
          disabled={loading}
        />

        <ul style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(180px, 1fr))',
          gap: 20,
          listStyle: 'none',
          padding: 0,
        }}>
          {auctions.map((auction, idx) => (
            <AuctionCard
              key={auction.id}
              auction={auction}
              isLast={idx === auctions.length - 1}
              refCallback={lastAuctionRef}
            />
          ))}
        </ul>

        {loading && <div style={{ textAlign: 'center' }}>로딩중...</div>}
        {!hasNext && auctions.length > 0 && (
          <div style={{ textAlign: 'center', color: '#666' }}>
            모든 경매를 확인했습니다. (총 {auctions.length}개)
          </div>
        )}
        {!loading && auctions.length === 0 && (
          <div style={{ textAlign: 'center', color: '#666', padding: 40 }}>
            경매가 없습니다.
          </div>
        )}

        <DebugPanel
          page={page}
          hasNext={hasNext}
          loading={loading}
          total={auctions.length}
          category={category}
        />
      </div>
    </>
  );
}
