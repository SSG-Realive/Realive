'use client';

import { useRef, useState } from 'react';

interface StarRatingProps {
  rating: number;
  setRating: (value: number) => void;
  setTempRating?: (value: number | null) => void; // 새로 추가
}

export default function StarRating({ rating, setRating, setTempRating }: StarRatingProps) {
  const [hoverRating, setHoverRating] = useState<number | null>(null);
  const containerRef = useRef<HTMLDivElement>(null);

  const handleMouseMove = (e: React.MouseEvent) => {
    const rect = containerRef.current?.getBoundingClientRect();
    if (!rect) return;
    const offsetX = e.clientX - rect.left;
    const percent = offsetX / rect.width;
    const newRating = Math.round(percent * 10) / 2;
    const clamped = newRating < 1 ? 1 : newRating > 5 ? 5 : newRating;
    setHoverRating(clamped);
    setTempRating?.(clamped); // 마우스 이동 시 임시로 별점 설정

  };

  const handleClick = () => {
    if (hoverRating !== null) {
      setRating(hoverRating);
    }
  };

  const handleMouseLeave = () => {
    setHoverRating(null);
    setTempRating?.(null); // 마우스 떠나면 초기화
  };

  const displayRating = hoverRating ?? rating;

  return (
    <div
      ref={containerRef}
      className="relative flex w-[160px] h-10 cursor-pointer"
      onMouseMove={handleMouseMove}
      onClick={handleClick}
      onMouseLeave={handleMouseLeave}
    >
      {/* 배경 빈 별 */}
      {[...Array(5)].map((_, i) => (
        <div key={i} className="text-gray-300 text-4xl pointer-events-none">
          ★
        </div>
      ))}

      {/* 채워진 별 */}
      <div
        className="absolute top-0 left-0 flex overflow-hidden text-yellow-400 text-4xl pointer-events-none"
        style={{ width: `${(displayRating / 5) * 100}%` }}
      >
        {[...Array(5)].map((_, i) => (
          <div key={i}>★</div>
        ))}
      </div>
    </div>
  );
}
