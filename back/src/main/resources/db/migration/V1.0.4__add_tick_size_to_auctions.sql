-- 경매 테이블에 최소 입찰 단위 컬럼 추가
ALTER TABLE auctions ADD COLUMN tick_size INTEGER DEFAULT 100;

-- 기존 데이터의 tick_size를 기본값(100)으로 설정
UPDATE auctions SET tick_size = 100 WHERE tick_size IS NULL; 