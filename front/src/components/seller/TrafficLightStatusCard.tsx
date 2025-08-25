'use client';

import { getTrafficLightText } from '@/types/admin/review';

interface TrafficLightStatusCardProps {
  title: string;
  rating: number;
  count?: number;
  className?: string;
}

function getCircleColor(rating: number) {
  if (rating <= 2 && rating > 0) return '#ef4444'; // 빨강
  if (rating === 3) return '#facc15'; // 노랑
  if (rating >= 4) return '#22c55e'; // 초록
  return '#d1d5db'; // 회색
}

export default function TrafficLightStatusCard({ 
  title, 
  rating, 
  count,
  className = "" 
}: TrafficLightStatusCardProps) {
  const isNoReview = !count || rating === 0;
  return (
    <div className={`bg-white rounded-2xl shadow-xl border border-gray-200 flex flex-col items-center justify-center py-8 px-6 min-h-[180px] min-w-[220px] transition-all ${className}`}>
      <div className="flex flex-col items-center justify-center w-full">
        <span className="mb-2 animate-pulse" style={{ display: 'inline-block' }}>
          <svg width="56" height="56" viewBox="0 0 56 56">
            <circle
              cx="28"
              cy="28"
              r="24"
              fill={getCircleColor(rating)}
              stroke="#e5e7eb"
              strokeWidth="4"
              style={{ filter: isNoReview ? 'grayscale(1)' : 'drop-shadow(0 0 8px #fff)' }}
            />
          </svg>
        </span>
        <div className="text-lg font-bold text-gray-800 mb-1">{title}</div>
        <div className="text-xl font-extrabold text-gray-900 mb-1">{isNoReview ? '평가 없음' : getTrafficLightText(rating)}</div>
        <div className="text-sm text-gray-400 mt-1">리뷰 {count ?? 0}건</div>
      </div>
    </div>
  );
} 