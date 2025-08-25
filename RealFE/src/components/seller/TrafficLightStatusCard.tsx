'use client';

interface TrafficLightStatusCardProps {
  title: string;
  rating: number;
  count?: number;
  className?: string;
  onClick?: () => void;
}

function getCircleColor(rating: number, reviewCount: number) {
  // 리뷰 없음
  if (reviewCount === 0) return '#d1d5db'; // 회색
  
  // 신규 판매자 보호 (1~4개)
  if (reviewCount < 5) return '#facc15'; // 노랑 고정
  
  // 5개 이상부터 실제 평점 적용
  if (rating >= 0.1 && rating <= 2.0) return '#ef4444'; // 빨강 (0.1~2.0)
  if (rating >= 2.1 && rating <= 3.5) return '#facc15'; // 노랑 (2.1~3.5)
  if (rating >= 3.6 && rating <= 5.0) return '#22c55e'; // 초록 (3.6~5.0)
  return '#d1d5db'; // 회색 (예외 상황)
}

function getStatusText(rating: number, reviewCount: number) {
  // 리뷰 없음
  if (reviewCount === 0) return '평가없음';
  
  // 신규 판매자 보호 (1~4개)
  if (reviewCount < 5) return '신규판매자';
  
  // 5개 이상부터 실제 평점 적용
  if (rating === 5.0) return '최고'; // 완벽한 5점
  if (rating >= 0.1 && rating <= 2.0) return '부정적'; // 빨강 구간
  if (rating >= 2.1 && rating <= 3.5) return '보통'; // 노랑 구간
  if (rating >= 3.6 && rating < 5.0) return '긍정적'; // 초록 구간
  return '평가없음'; // 예외 상황
}

export default function TrafficLightStatusCard({ 
  title, 
  rating, 
  count,
  className = "",
  onClick
}: TrafficLightStatusCardProps) {
  const isNoReview = !count || rating === 0;
  return (
    <div 
      className={`bg-[#f3f4f6] rounded-2xl shadow-xl border-2 border-[#d1d5db] flex flex-col items-center justify-center py-8 px-6 min-h-[180px] min-w-[220px] transition-all hover:border-[#a89f91] hover:shadow-2xl ${onClick ? 'cursor-pointer hover:scale-105' : ''} ${className}`}
      onClick={onClick}
    >
      <div className="flex flex-col items-center justify-center w-full">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-lg font-semibold text-gray-700">{title}</h3>
        </div>
        <span className="mb-2 animate-pulse" style={{ display: 'inline-block' }}>
          <svg width="56" height="56" viewBox="0 0 56 56">
            <circle
              cx="28"
              cy="28"
              r="24"
              fill={getCircleColor(rating, count ?? 0)}
              stroke="#d6ccc2"
              strokeWidth="4"
              style={{ filter: isNoReview ? 'grayscale(1)' : 'drop-shadow(0 0 8px #fff)' }}
            />
          </svg>
        </span>
        <div className="text-lg font-bold text-[#374151] mb-1">{title}</div>
        <div className="text-xl font-extrabold text-[#374151] mb-1">{isNoReview ? '평가 없음' : getStatusText(rating, count ?? 0)}</div>
        <div className="text-sm text-[#a89f91] mt-1">리뷰 {count ?? 0}건</div>
      </div>
    </div>
  );
} 