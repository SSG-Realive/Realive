-- auctions 테이블에 status 컬럼 추가
ALTER TABLE auctions
ADD COLUMN status VARCHAR(20) DEFAULT 'PROCEEDING' NOT NULL;

-- 기존 is_closed 값에 따라 status 업데이트
UPDATE auctions
SET status = CASE
    WHEN is_closed = true THEN 'COMPLETED'
    ELSE 'PROCEEDING'
END;

-- is_closed 컬럼 제거
ALTER TABLE auctions
DROP COLUMN is_closed; 