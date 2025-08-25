"use client";
import { useParams, useRouter } from "next/navigation";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { 
  Gavel, 
  Eye, 
  Edit, 
  XCircle, 
  ArrowLeft,
  Package,
  Calendar,
  DollarSign,
  User,
  Clock
} from "lucide-react";
import Link from "next/link";
import { useEffect, useState } from "react";
import { adminAuctionService } from '@/service/admin/auctionService';
import { AuctionResponseDTO } from '@/types/admin/auction';

export default function AuctionDetailPage() {
  const params = useParams();
  const router = useRouter();
  const id = Number(params.id);
  const [auction, setAuction] = useState<AuctionResponseDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      fetchAuctionDetail();
    }
  }, [id]);

  const fetchAuctionDetail = async () => {
    try {
      setLoading(true);
      const data = await adminAuctionService.getAuctionById(id);
      setAuction(data);
      setError(null);
    } catch (err) {
      console.error('경매 상세 정보 조회 실패:', err);
      setError('경매 정보를 불러오는데 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 이미지 URL 생성 함수
  const getImageUrl = (imagePath: string): string => {
    if (!imagePath) return '/images/placeholder.png';
    if (imagePath.startsWith('http')) return imagePath;
    return `https://www.realive-ssg.click/api${imagePath}`;
  };

  const getStatusColor = (status: string, startTime?: string) => {
    // 취소되거나 실패한 경매는 상태를 우선
    if (status === 'CANCELLED') return 'bg-red-500 text-white';
    if (status === 'FAILED') return 'bg-gray-500 text-white';
    if (status === 'COMPLETED') return 'bg-blue-500 text-white';
    
    // 시작 시간이 현재보다 뒤면 예정
    if (startTime && new Date(startTime) > new Date()) {
      return 'bg-yellow-500 text-white';
    }
    
    switch (status) {
      case 'PROCEEDING': return 'bg-green-500 text-white';
      default: return 'bg-gray-500 text-white';
    }
  };

  const getStatusText = (status: string, startTime?: string) => {
    // 취소되거나 실패한 경매는 상태를 우선
    if (status === 'CANCELLED') return '취소됨';
    if (status === 'FAILED') return '실패';
    if (status === 'COMPLETED') return '완료';
    
    // 시작 시간이 현재보다 뒤면 예정
    if (startTime && new Date(startTime) > new Date()) {
      return '예정';
    }
    
    switch (status) {
      case 'PROCEEDING': return '진행중';
      default: return status;
    }
  };

  const handleViewBids = () => {
    router.push(`/admin/auction-management/bid/auction/${auction?.id}`);
  };

  const handleEditAuction = () => {
    router.push(`/admin/auction-management/list/edit/${auction?.id}`);
  };

  const handleCancelAuction = async () => {
    if (!auction) return;
    
    if (confirm('정말로 이 경매를 취소하시겠습니까?')) {
      try {
        const reason = prompt('취소 사유를 입력해주세요 (선택사항):');
        
        // 경매 취소 API 호출
        await adminAuctionService.cancelAuction(auction.id, reason || undefined);
        
        alert('경매가 성공적으로 취소되었습니다.');
        
        // 페이지 새로고침하여 상태 업데이트
        window.location.reload();
      } catch (err: any) {
        console.error('경매 취소 실패:', err);
        const errorMessage = err.response?.data?.message || '경매 취소에 실패했습니다.';
        alert(errorMessage);
      }
    }
  };

  if (typeof window !== 'undefined' && !localStorage.getItem('adminToken')) {
    window.location.replace('/admin/login');
    return null;
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">경매 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-32 h-32 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <XCircle className="w-16 h-16 text-red-500" />
          </div>
          <h3 className="text-2xl font-bold text-gray-800 mb-4">오류가 발생했습니다</h3>
          <p className="text-gray-600 text-lg mb-8">{error}</p>
          <Button onClick={fetchAuctionDetail} className="bg-gray-800 hover:bg-gray-700">
            다시 시도
          </Button>
        </div>
      </div>
    );
  }

  if (!auction) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-32 h-32 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <Package className="w-16 h-16 text-gray-400" />
          </div>
          <h3 className="text-2xl font-bold text-gray-800 mb-4">경매를 찾을 수 없습니다</h3>
          <p className="text-gray-600 text-lg mb-8">요청하신 경매 정보가 존재하지 않습니다.</p>
          <Link href="/admin/auction-management/list">
            <Button className="bg-gray-800 hover:bg-gray-700">
              목록으로 돌아가기
            </Button>
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-6xl mx-auto p-6">
        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-4">
              <div className="relative">
                <div className="w-16 h-16 bg-gray-800 rounded-2xl flex items-center justify-center shadow-lg">
                  <Gavel className="w-8 h-8 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-bold text-gray-800">
                  경매 상세 정보
                </h1>
                <p className="text-gray-600 text-lg mt-2">
                  경매의 상세 정보를 확인하고 관리할 수 있습니다.
                </p>
              </div>
            </div>
            <Link href="/admin/auction-management/list">
              <Button variant="outline" className="flex items-center space-x-2">
                <ArrowLeft className="w-4 h-4" />
                <span>목록으로</span>
              </Button>
            </Link>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 상품 이미지 */}
          <div className="lg:col-span-1">
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Package className="w-5 h-5" />
                  <span>상품 이미지</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                {auction.adminProduct?.imageThumbnailUrl ? (
                  <img 
                    src={getImageUrl(auction.adminProduct.imageThumbnailUrl)} 
                    alt={auction.adminProduct.productName} 
                    className="w-full h-64 object-cover rounded-lg shadow-sm"
                    onError={(e) => {
                      e.currentTarget.src = '/images/placeholder.png';
                    }}
                  />
                ) : (
                  <div className="w-full h-64 bg-gray-200 flex items-center justify-center rounded-lg">
                    <span className="text-gray-500">이미지 없음</span>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>

          {/* 경매 정보 */}
          <div className="lg:col-span-2 space-y-6">
            {/* 경매 기본 정보 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Gavel className="w-5 h-5" />
                  <span>경매 정보</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2">
                      <Package className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">상품명:</span>
                      <span className="text-gray-900">{auction.adminProduct?.productName}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <DollarSign className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">시작가:</span>
                      <span className="text-gray-900">{auction.startPrice?.toLocaleString()}원</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <DollarSign className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">현재가:</span>
                      <span className="text-blue-600 font-semibold">{auction.currentPrice?.toLocaleString()}원</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <span className="font-medium text-gray-700">상태:</span>
                      <Badge className={getStatusColor(auction.status, auction.startTime)}>
                        {getStatusText(auction.status, auction.startTime)}
                      </Badge>
                    </div>
                  </div>
                  <div className="space-y-3">
                    <div className="flex items-center space-x-2">
                      <Calendar className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">시작시간:</span>
                      <span className="text-gray-900">{new Date(auction.startTime).toLocaleString()}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Calendar className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">종료시간:</span>
                      <span className="text-gray-900">{new Date(auction.endTime).toLocaleString()}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Clock className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">등록시간:</span>
                      <span className="text-gray-900">{new Date(auction.createdAt).toLocaleString()}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Clock className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">수정시간:</span>
                      <span className="text-gray-900">{new Date(auction.updatedAt).toLocaleString()}</span>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* 상품 상세 정보 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Package className="w-5 h-5" />
                  <span>상품 상세 정보</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  <div>
                    <span className="font-medium text-gray-700">상품 설명:</span>
                    <p className="text-gray-900 mt-1">{auction.adminProduct?.productDescription}</p>
                  </div>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div className="flex items-center space-x-2">
                      <span className="font-medium text-gray-700">상품 ID:</span>
                      <span className="text-gray-900">{auction.adminProduct?.productId}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <DollarSign className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">구매가:</span>
                      <span className="text-gray-900">{auction.adminProduct?.purchasePrice?.toLocaleString()}원</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <User className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">판매자 ID:</span>
                      <span className="text-gray-900">{auction.adminProduct?.purchasedFromSellerId || 'N/A'}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Calendar className="w-4 h-4 text-gray-500" />
                      <span className="font-medium text-gray-700">구매일:</span>
                      <span className="text-gray-900">{auction.adminProduct?.purchasedAt ? new Date(auction.adminProduct.purchasedAt).toLocaleDateString() : 'N/A'}</span>
                    </div>
                  </div>
                  <div className="flex items-center space-x-2">
                    <span className="font-medium text-gray-700">경매 등록 여부:</span>
                    <Badge variant={auction.adminProduct?.auctioned ? 'default' : 'secondary'}>
                      {auction.adminProduct?.auctioned ? '등록됨' : '미등록'}
                    </Badge>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* 액션 버튼들 */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center space-x-2">
                  <Gavel className="w-5 h-5" />
                  <span>관리 액션</span>
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="flex flex-wrap gap-4">
                  <Button 
                    onClick={handleViewBids}
                    className="flex items-center space-x-2 bg-blue-600 hover:bg-blue-700"
                  >
                    <Eye className="w-4 h-4" />
                    <span>입찰 내역 보기</span>
                  </Button>
                  <Button 
                    onClick={handleEditAuction}
                    variant="outline"
                    className="flex items-center space-x-2"
                  >
                    <Edit className="w-4 h-4" />
                    <span>경매 수정</span>
                  </Button>
                  {getStatusText(auction.status, auction.startTime) !== '완료' && 
                   getStatusText(auction.status, auction.startTime) !== '취소됨' && (
                    <Button 
                      onClick={handleCancelAuction}
                      variant="destructive"
                      className="flex items-center space-x-2"
                    >
                      <XCircle className="w-4 h-4" />
                      <span>경매 취소</span>
                    </Button>
                  )}
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
} 