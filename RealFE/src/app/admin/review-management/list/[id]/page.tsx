"use client";
import React, { useState, useEffect } from "react";
import { useRouter, useParams } from "next/navigation";
import { getAdminReview } from "@/service/admin/reviewService";
import { AdminReview } from "@/types/admin/review";
import { useAdminAuthStore } from "@/store/admin/useAdminAuthStore";
import { getTrafficLightEmoji, getTrafficLightText, getTrafficLightBgClass } from "@/types/admin/review";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { 
  ArrowLeft, 
  MessageSquare, 
  User, 
  Store, 
  Calendar, 
  Star, 
  Eye, 
  EyeOff,
  Package,
  AlertTriangle,
  RefreshCw,
  Image
} from "lucide-react";

export default function ReviewDetailPage() {
  const router = useRouter();
  const params = useParams();
  const { accessToken } = useAdminAuthStore();
  
  // params.id를 안전하게 파싱
  const reviewId = params.id ? Number(params.id) : null;
  
  const [review, setReview] = useState<AdminReview | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchReviewDetail = async () => {
    if (!accessToken || !reviewId || isNaN(reviewId)) {
      setError("유효하지 않은 리뷰 ID입니다.");
      setLoading(false);
      return;
    }

    try {
      setLoading(true);
      setError(null);
      const data = await getAdminReview(reviewId);
      setReview(data);
    } catch (err: any) {
      console.error("리뷰 상세 조회 실패:", err);
      setError(err.message || "리뷰 정보를 불러오는데 실패했습니다.");
      if (err.response?.status === 403) {
        router.replace('/admin/login');
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (accessToken) {
      fetchReviewDetail();
    } else {
      router.replace('/admin/login');
    }
  }, [accessToken, reviewId]);

  // 신호등 색상 반환 함수
  const getTrafficLightColor = (rating: number): string => {
    if (rating <= 2) return '#ef4444'; // 빨강 (1-2점: 부정적)
    if (rating === 3) return '#facc15'; // 노랑 (3점: 보통)
    return '#22c55e'; // 초록 (4-5점: 긍정적)
  };

  if (loading) {
    return (
      <div className="h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="relative">
            <div className="w-16 h-16 border-4 border-gray-200 border-t-gray-600 rounded-full animate-spin mx-auto mb-4"></div>
            <div className="absolute inset-0 w-16 h-16 border-4 border-transparent border-t-gray-400 rounded-full animate-spin mx-auto" style={{ animationDelay: '0.5s' }}></div>
          </div>
          <p className="text-gray-600 text-lg font-medium">리뷰 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-32 h-32 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <AlertTriangle className="w-16 h-16 text-red-500" />
          </div>
          <h3 className="text-2xl font-bold text-gray-800 mb-4">오류가 발생했습니다</h3>
          <p className="text-gray-600 text-lg mb-8">{error}</p>
          <div className="flex gap-4 justify-center">
            <Button onClick={() => fetchReviewDetail()} className="bg-gray-800 w-full !hover:bg-gray-800 !hover:shadow-none !hover:text-inherit">
              <RefreshCw className="w-4 h-4 mr-2" />
              다시 시도
            </Button>
            <Button onClick={() => router.back()} variant="outline">
              <ArrowLeft className="w-4 h-4 mr-2" />
              목록으로 돌아가기
            </Button>
          </div>
        </div>
      </div>
    );
  }

  if (!review) {
    return (
      <div className="h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="w-32 h-32 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-8">
            <MessageSquare className="w-16 h-16 text-gray-400" />
          </div>
          <h3 className="text-2xl font-bold text-gray-800 mb-4">리뷰를 찾을 수 없습니다</h3>
          <p className="text-gray-600 text-lg mb-8">요청하신 리뷰 정보가 존재하지 않습니다.</p>
          <Button onClick={() => router.back()} className="bg-gray-800 w-full !hover:bg-gray-800 !hover:shadow-none !hover:text-inherit">
            <ArrowLeft className="w-4 h-4 mr-2" />
            목록으로 돌아가기
          </Button>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-50">
      <div className="max-w-6xl mx-auto p-6">
        {/* 헤더 섹션 */}
        <div className="mb-8">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center space-x-4">
              <Button 
                onClick={() => router.back()}
                variant="outline"
                className="flex items-center space-x-2"
              >
                <ArrowLeft className="w-4 h-4" />
                <span>목록으로</span>
              </Button>
              <div className="relative">
                <div className="w-16 h-16 bg-gray-800 rounded-2xl flex items-center justify-center shadow-lg">
                  <MessageSquare className="w-8 h-8 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-bold text-gray-800">
                  리뷰 상세
                </h1>
                <p className="text-gray-600 text-lg mt-2">
                  리뷰 ID: {review.reviewId}
                </p>
              </div>
            </div>
            <div className="flex items-center space-x-3">
              <Badge variant={review.isHidden ? "secondary" : "default"} className="flex items-center gap-1 px-3 py-2">
                {review.isHidden ? <EyeOff className="w-4 h-4" /> : <Eye className="w-4 h-4" />}
                {review.isHidden ? '숨김' : '공개'}
              </Badge>
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* 메인 콘텐츠 */}
          <div className="lg:col-span-2 space-y-6">
            {/* 리뷰 정보 카드 */}
            <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
              <CardHeader>
                <CardTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
                  <MessageSquare className="w-5 h-5" />
                  리뷰 정보
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-6">
                {/* 상품 정보 */}
                <div className="flex items-start gap-4 p-4 bg-gray-50 rounded-xl">
                  <div className="w-12 h-12 bg-blue-100 rounded-lg flex items-center justify-center">
                    <Package className="w-6 h-6 text-blue-600" />
                  </div>
                  <div className="flex-1">
                    <h3 className="font-semibold text-lg text-gray-800 mb-1">
                      {review.productName || '상품명 없음'}
                    </h3>
                    <p className="text-sm text-gray-600">상품 ID: {review.productId}</p>
                  </div>
                </div>

                {/* 사용자 정보 */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-xl">
                    <div className="w-10 h-10 bg-green-100 rounded-lg flex items-center justify-center">
                      <User className="w-5 h-5 text-green-600" />
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">작성자</p>
                      <p className="font-semibold text-gray-800">{review.customerName}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-xl">
                    <div className="w-10 h-10 bg-purple-100 rounded-lg flex items-center justify-center">
                      <Store className="w-5 h-5 text-purple-600" />
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">판매자</p>
                      <p className="font-semibold text-gray-800">{review.sellerName}</p>
                    </div>
                  </div>
                </div>

                {/* 평점 정보 */}
                <div className="flex items-center gap-4 p-4 bg-gray-50 rounded-xl">
                  <div className="flex items-center gap-3">
                    <div className="relative">
                      <svg width="48" height="48" viewBox="0 0 48 48">
                        <circle
                          cx="24"
                          cy="24"
                          r="20"
                          fill={getTrafficLightColor(review.rating)}
                          stroke="#d6ccc2"
                          strokeWidth="3"
                          style={{ 
                            filter: 'drop-shadow(0 0 6px rgba(0,0,0,0.1))',
                            transition: 'all 0.3s ease'
                          }}
                        />
                      </svg>
                      <div className="absolute inset-0 flex items-center justify-center">
                        <span className="text-sm font-bold text-white drop-shadow-sm">
                          {review.rating.toFixed(1)}
                        </span>
                      </div>
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">평점</p>
                      <p className="font-semibold text-lg text-gray-800">{getTrafficLightText(review.rating)}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-2">
                    <Star className="w-5 h-5 text-yellow-500" />
                    <span className="font-semibold text-gray-800">{review.rating.toFixed(1)}점</span>
                  </div>
                </div>

                {/* 날짜 정보 */}
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-xl">
                    <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                      <Calendar className="w-5 h-5 text-blue-600" />
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">작성일</p>
                      <p className="font-semibold text-gray-800">
                        {new Date(review.createdAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                  
                  <div className="flex items-center gap-3 p-4 bg-gray-50 rounded-xl">
                    <div className="w-10 h-10 bg-orange-100 rounded-lg flex items-center justify-center">
                      <Calendar className="w-5 h-5 text-orange-600" />
                    </div>
                    <div>
                      <p className="text-sm text-gray-600">수정일</p>
                      <p className="font-semibold text-gray-800">
                        {new Date(review.updatedAt).toLocaleDateString()}
                      </p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>

            {/* 리뷰 내용 카드 */}
            <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
              <CardHeader>
                <CardTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
                  <MessageSquare className="w-5 h-5" />
                  리뷰 내용
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="p-6 bg-gray-50 rounded-xl min-h-[200px]">
                  <p className="text-gray-700 leading-relaxed whitespace-pre-wrap">
                    {review.content || '내용이 없습니다.'}
                  </p>
                </div>
              </CardContent>
            </Card>

            {/* 리뷰 이미지 카드 */}
            {review.imageUrls && review.imageUrls.length > 0 && (
              <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
                <CardHeader>
                  <CardTitle className="text-xl font-bold text-gray-800 flex items-center gap-2">
                    <Image className="w-5 h-5" />
                    첨부 이미지 ({review.imageUrls.length}개)
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="grid grid-cols-2 gap-4">
                    {review.imageUrls.map((url, index) => (
                      <div key={index} className="aspect-square rounded-xl overflow-hidden bg-gray-100">
                        <img 
                          src={url} 
                          alt={`리뷰 이미지 ${index + 1}`} 
                          className="w-full h-full object-cover"
                          onError={(e) => {
                            e.currentTarget.src = '/images/placeholder.jpg';
                            e.currentTarget.alt = '이미지를 불러올 수 없습니다';
                          }}
                        />
                      </div>
                    ))}
                  </div>
                </CardContent>
              </Card>
            )}
          </div>

          {/* 사이드바 */}
          <div className="space-y-6">
            {/* 상품 이미지 카드 */}
            {review.productImage && (
              <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
                <CardHeader>
                  <CardTitle className="text-lg font-bold text-gray-800 flex items-center gap-2">
                    <Package className="w-4 h-4" />
                    상품 이미지
                  </CardTitle>
                </CardHeader>
                <CardContent>
                  <div className="aspect-square rounded-xl overflow-hidden bg-gray-100">
                    <img 
                      src={review.productImage} 
                      alt="상품 이미지" 
                      className="w-full h-full object-cover"
                    />
                  </div>
                </CardContent>
              </Card>
            )}

            

            {/* 액션 카드 */}
            <Card className="bg-white rounded-xl shadow-sm border border-gray-200 !shadow-sm !hover:shadow-none">
              <CardHeader>
                <CardTitle className="text-lg font-bold text-gray-800">
                  액션
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <Button 
                  variant="outline" 
                  className="w-full justify-start"
                  onClick={() => router.push(`/admin/review-management/list`)}
                >
                  <ArrowLeft className="w-4 h-4 mr-2" />
                  목록으로 돌아가기
                </Button>
                <Button 
                  variant="outline" 
                  className="w-full justify-start"
                  onClick={() => window.print()}
                >
                  <MessageSquare className="w-4 h-4 mr-2" />
                  인쇄
                </Button>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
} 