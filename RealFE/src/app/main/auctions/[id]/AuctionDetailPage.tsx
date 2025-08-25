'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { useAuthStore } from '@/store/customer/authStore';
// import { publicAuctionService, customerAuctionService } from '@/service/customer/auctionService';
import Link from 'next/link';

interface Auction {
  id: number;
  productId: number;
  startPrice: number;
  currentPrice: number;
  startTime: string;
  endTime: string;
  status: string;
  adminProduct: {
    productName: string | null;
    imageUrl?: string | null;
  };
}

interface Bid {
  id: number;
  auctionId: number;
  customerId: number;
  bidPrice: number;
  bidTime: string;
  customerName: string;
}

export default function AuctionDetailPage() {
  const params = useParams();
  const router = useRouter();
  const auctionId = Number(params.id);
  
  const { isAuthenticated, userName } = useAuthStore();
  const [mounted, setMounted] = useState(false);
  const [auction, setAuction] = useState<Auction | null>(null);
  const [bids, setBids] = useState<Bid[]>([]);
  const [tickSize, setTickSize] = useState<number>(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [bidAmount, setBidAmount] = useState<string>('');
  const [bidding, setBidding] = useState(false);

//   // 클라이언트 사이드에서만 실행
//   useEffect(() => {
//     setMounted(true);
//   }, []);

//   // 데이터 로딩
//   useEffect(() => {
//     if (!mounted || !auctionId) return;

//     const fetchData = async () => {
//       try {
//         setLoading(true);
//         const loggedIn = isAuthenticated();

//         // 로그인 상태에 따라 적절한 API 사용
//         if (loggedIn) {
//           const [auctionData, bidsData, tickData] = await Promise.all([
//             customerAuctionService.getAuctionDetail(auctionId),
//             customerAuctionService.getAuctionBids(auctionId),
//             customerAuctionService.getTickSize(auctionId)
//           ]);
          
//           setAuction(auctionData.data);
//           setBids(bidsData.data.content || []);
//           setTickSize(tickData.data);
//         } else {
//           const [auctionData, bidsData, tickData] = await Promise.all([
//             publicAuctionService.getPublicAuctionDetail(auctionId),
//             publicAuctionService.getPublicAuctionBids(auctionId),
//             publicAuctionService.getPublicTickSize(auctionId)
//           ]);
          
//           setAuction(auctionData.data);
//           setBids(bidsData.data.content || []);
//           setTickSize(tickData.data);
//         }
//       } catch (err) {
//         console.error('경매 상세 정보 로딩 실패:', err);
//         setError('경매 정보를 불러올 수 없습니다.');
//       } finally {
//         setLoading(false);
//       }
//     };

//     fetchData();
//   }, [mounted, auctionId, isAuthenticated]);

//   // 입찰하기
//   const handleBid = async () => {
//     if (!isAuthenticated()) {
//       router.push('/login');
//       return;
//     }

//     if (!bidAmount || isNaN(Number(bidAmount))) {
//       alert('올바른 입찰 금액을 입력해주세요.');
//       return;
//     }

//     try {
//       setBidding(true);
//       await customerAuctionService.placeBid(auctionId, Number(bidAmount));
      
//       // 입찰 후 데이터 새로고침
//       const [bidsData] = await Promise.all([
//         customerAuctionService.getAuctionBids(auctionId),
//         // 경매 정보도 새로고침 (현재가 업데이트)
//         customerAuctionService.getAuctionDetail(auctionId).then(data => setAuction(data.data))
//       ]);
      
//       setBids(bidsData.data.content || []);
//       setBidAmount('');
//       alert('입찰이 완료되었습니다!');
//     } catch (err) {
//       console.error('입찰 실패:', err);
//       alert('입찰에 실패했습니다. 다시 시도해주세요.');
//     } finally {
//       setBidding(false);
//     }
//   };

//   if (!mounted) {
//     return <div className="min-h-screen flex items-center justify-center">로딩 중...</div>;
//   }

//   if (loading) {
//     return <div className="min-h-screen flex items-center justify-center">경매 정보를 불러오는 중...</div>;
//   }

//   if (error || !auction) {
//     return (
//       <div className="min-h-screen flex items-center justify-center">
//         <div className="text-center">
//           <p className="text-red-500 mb-4">{error || '경매 정보를 찾을 수 없습니다.'}</p>
//           <Link href="/main/auctions" className="text-blue-600 hover:underline">
//             경매 목록으로 돌아가기
//           </Link>
//         </div>
//       </div>
//     );
//   }

//   const loggedIn = isAuthenticated();
//   const isActive = auction.status === 'PROCEEDING';
//   const minBidAmount = auction.currentPrice + tickSize;

  return (
    // <div className="min-h-screen bg-gray-50">
    //   <div className="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
    //     {/* 뒤로가기 */}
    //     <div className="mb-6">
    //       <Link href="/main/auctions" className="text-blue-600 hover:underline">
    //         ← 경매 목록으로 돌아가기
    //       </Link>
    //     </div>

    //     <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
    //       {/* 왼쪽: 상품 정보 */}
    //       <div className="bg-white rounded-lg shadow-lg p-6">
    //         <div className="mb-6">
    //           <img
    //             src={auction.adminProduct.imageUrl || '/images/placeholder.png'}
    //             alt={auction.adminProduct.productName || '상품 이미지'}
    //             className="w-full h-64 object-cover rounded-lg"
    //           />
    //         </div>
            
    //         <h1 className="text-2xl font-light text-gray-900 mb-4">
    //           {auction.adminProduct.productName}
    //         </h1>
            
    //         <div className="space-y-3 text-gray-600">
    //           <div className="flex justify-between">
    //             <span>시작가:</span>
    //             <span className="font-light">{auction.startPrice.toLocaleString()}원</span>
    //           </div>
    //           <div className="flex justify-between">
    //             <span>현재가:</span>
    //             <span className="font-light text-2xl text-blue-600">
    //               {auction.currentPrice.toLocaleString()}원
    //             </span>
    //           </div>
    //           <div className="flex justify-between">
    //             <span>입찰 단위:</span>
    //             <span className="font-light">{tickSize.toLocaleString()}원</span>
    //           </div>
    //           <div className="flex justify-between">
    //             <span>경매 상태:</span>
    //             <span className={`font-light ${
    //               isActive ? 'text-green-600' : 'text-gray-500'
    //             }`}>
    //               {isActive ? '진행중' : '종료'}
    //             </span>
    //           </div>
    //           <div className="flex justify-between">
    //             <span>시작 시간:</span>
    //             <span>{new Date(auction.startTime).toLocaleString()}</span>
    //           </div>
    //           <div className="flex justify-between">
    //             <span>종료 시간:</span>
    //             <span>{new Date(auction.endTime).toLocaleString()}</span>
    //           </div>
    //         </div>
    //       </div>

    //       {/* 오른쪽: 입찰 영역 */}
    //       <div className="space-y-6">
    //         {/* 입찰 폼 */}
    //         <div className="bg-white rounded-lg shadow-lg p-6">
    //           <h2 className="text-xl font-light text-gray-900 mb-4">입찰하기</h2>
              
    //           {loggedIn ? (
    //             isActive ? (
    //               <div className="space-y-4">
    //                 <div>
    //                   <label className="block text-sm font-light text-gray-700 mb-2">
    //                     입찰 금액 (최소: {minBidAmount.toLocaleString()}원)
    //                   </label>
    //                   <input
    //                     type="number"
    //                     value={bidAmount}
    //                     onChange={(e) => setBidAmount(e.target.value)}
    //                     min={minBidAmount}
    //                     step={tickSize}
    //                     className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
    //                     placeholder={`${minBidAmount.toLocaleString()}원 이상 입력`}
    //                   />
    //                 </div>
    //                 <button
    //                   onClick={handleBid}
    //                   disabled={bidding || !bidAmount}
    //                   className="w-full py-3 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed font-light"
    //                 >
    //                   {bidding ? '입찰 중...' : '입찰하기'}
    //                 </button>
    //               </div>
    //             ) : (
    //               <div className="text-center py-4">
    //                 <p className="text-gray-500">이 경매는 종료되었습니다.</p>
    //               </div>
    //             )
    //           ) : (
    //             <div className="text-center py-4">
    //               <p className="text-gray-600 mb-4">입찰하려면 로그인이 필요합니다.</p>
    //               <div className="space-y-2">
    //                 <Link
    //                   href="/login"
    //                   className="block w-full py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition-colors"
    //                 >
    //                   로그인
    //                 </Link>
    //                 <Link
    //                   href="/customer/signup"
    //                   className="block w-full py-2 border border-blue-600 text-blue-600 rounded-md hover:bg-blue-50 transition-colors"
    //                 >
    //                   회원가입
    //                 </Link>
    //               </div>
    //             </div>
    //           )}
    //         </div>

    //         {/* 입찰 내역 */}
    //         <div className="bg-white rounded-lg shadow-lg p-6">
    //           <h2 className="text-xl font-light text-gray-900 mb-4">
    //             입찰 내역 ({bids.length}건)
    //           </h2>
              
    //           <div className="max-h-96 overflow-y-auto">
    //             {bids.length > 0 ? (
    //               <div className="space-y-2">
    //                 {bids.map((bid) => (
    //                   <div
    //                     key={bid.id}
    //                     className="flex justify-between items-center py-2 px-3 bg-gray-50 rounded-md"
    //                   >
    //                     <div>
    //                       <span className="font-light">
    //                         {loggedIn ? bid.customerName : '***'}
    //                       </span>
    //                       <span className="text-sm text-gray-500 ml-2">
    //                         {new Date(bid.bidTime).toLocaleString()}
    //                       </span>
    //                     </div>
    //                     <span className="font-light text-blue-600">
    //                       {bid.bidPrice.toLocaleString()}원
    //                     </span>
    //                   </div>
    //                 ))}
    //               </div>
    //             ) : (
    //               <p className="text-gray-500 text-center py-4">아직 입찰 내역이 없습니다.</p>
    //             )}
    //           </div>
    //         </div>
    //       </div>
    //     </div>
    //   </div>
    // </div>
    <></>
  );
} 