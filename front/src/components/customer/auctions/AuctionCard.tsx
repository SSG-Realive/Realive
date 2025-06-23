import Link from "next/link";

interface AuctionCardProps {
  auction: Auction | null | undefined;
  isLast: boolean;
  refCallback: (node: HTMLLIElement | null) => void;
}

export default function AuctionCard({ auction, isLast, refCallback }: AuctionCardProps) {
  if (!auction) {
    // auction 자체가 없으면 빈 li 반환하거나 로딩/에러 UI 표시 가능
    return (
      <li className="border border-gray-300 rounded-md p-2 bg-white shadow flex flex-col h-full">
        <p>경매 정보가 없습니다.</p>
      </li>
    );
  }

  const { id, startPrice, currentPrice, endTime, status, adminProduct } = auction;
  const productName = adminProduct?.productName ?? '상품 없음';
  const imageUrl = adminProduct?.imageUrl ?? '/images/placeholder.png';

  return (
    <li
      ref={isLast ? refCallback : null}
      className="border border-gray-300 rounded-md p-2 bg-white shadow flex flex-col h-full"
    >
      <Link href={`/auctions/${id}`} className="flex flex-col flex-grow text-inherit no-underline">
        <div className="h-36 mb-2 bg-gray-200 rounded overflow-hidden">
          <img
            src={imageUrl}
            alt={productName}
            className="max-w-full max-h-full object-contain"
            onError={(e) => ((e.target as HTMLImageElement).src = '/placeholder.png')}
          />
        </div>
        <div>
          <strong>{productName}</strong>
          <p>시작가: {startPrice?.toLocaleString() ?? '-'}원</p>
          <p>현재가: {currentPrice?.toLocaleString() ?? '-'}원</p>
          <p>종료일: {endTime ? new Date(endTime).toLocaleString() : '-'}</p>
          <p>상태: {status ?? '-'}</p>
        </div>
      </Link>
    </li>
  );
}
