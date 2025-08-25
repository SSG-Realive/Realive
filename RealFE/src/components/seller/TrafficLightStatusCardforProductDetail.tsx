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

export default function TrafficLightStatusCardforProductDetail({
                                                                   rating,
                                                                   count,
                                                                   className = "",
                                                               }: Omit<TrafficLightStatusCardProps, "title">) {
    const isNoReview = !count || rating === 0;

    return (
        <div className={`flex items-center gap-2 text-sm text-gray-800 ${className}`}>
            {/* 신호등 아이콘 */}
            <span style={{ display: 'inline-block' }}>
        <svg width="20" height="20" viewBox="0 0 56 56">
          <circle
              cx="28"
              cy="28"
              r="10"
              fill={getCircleColor(rating)}
              stroke="#d6ccc2"
              strokeWidth="4"
              style={{ filter: isNoReview ? 'grayscale(1)' : 'drop-shadow(0 0 1px #fff)' }}
          />
        </svg>
      </span>

            {/* 평가 텍스트 */}
            <span className="font-medium">
        {isNoReview ? '평가 없음' : getTrafficLightText(rating)}
      </span>

            {/* 리뷰 수 */}
            <span className="text-xs text-gray-400 ml-1 whitespace-nowrap">
        리뷰 {count ?? 0}건
      </span>
        </div>
    );
}
