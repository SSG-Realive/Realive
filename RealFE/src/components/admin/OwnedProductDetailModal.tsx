"use client";
import React, { useState, useEffect } from "react";
import { X, Package, DollarSign, Calendar, Eye, Star, Gavel, PlayCircle, StopCircle } from "lucide-react";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import apiClient from '@/lib/apiClient';
import { useGlobalDialog } from "@/app/context/dialogContext";

interface OwnedProduct {
  id: number;
  name: string;
  description?: string;
  price: number;
  stock: number;
  status: string;
  categoryName: string;
  createdAt: string;
  imageThumbnailUrl?: string;  // thumbnailUrl → imageThumbnailUrl로 변경
  imageUrls?: string[];
  purchasePrice?: number;
  purchasedAt?: string;
  isAuctioned?: boolean;
  width?: number;
  depth?: number;
  height?: number;
}

interface OwnedProductDetailModalProps {
  product: OwnedProduct | null;
  isOpen: boolean;
  onClose: () => void;
  onAuctionCreated?: () => void;
}

export default function OwnedProductDetailModal({ 
  product, 
  isOpen, 
  onClose, 
  onAuctionCreated 
}: OwnedProductDetailModalProps) {
  const { show } = useGlobalDialog();
  const [startPrice, setStartPrice] = useState<string>("");
  const [startDate, setStartDate] = useState<string>("");
  const [startHour, setStartHour] = useState<string>("12");
  const [startMinute, setStartMinute] = useState<string>("00");
  const [endDate, setEndDate] = useState<string>("");
  const [endHour, setEndHour] = useState<string>("12");
  const [endMinute, setEndMinute] = useState<string>("00");
  const [isCreatingAuction, setIsCreatingAuction] = useState(false);
  const [showAuctionForm, setShowAuctionForm] = useState(false);

  // 모달이 열릴 때마다 초기화
  useEffect(() => {
    if (isOpen) {
      setStartPrice("");
      setStartDate("");
      setStartHour("12");
      setStartMinute("00");
      setEndDate("");
      setEndHour("12");
      setEndMinute("00");
      setShowAuctionForm(false);
    }
  }, [isOpen]);

  if (!isOpen || !product) return null;

  const handleCreateAuction = async () => {
    if (!product.purchasePrice) {
      await show("매입 가격 정보가 없습니다.");
      return;
    }

    const numericStartPrice = Number(startPrice.replace(/,/g, ''));
    
    if (numericStartPrice <= 0 || isNaN(numericStartPrice)) {
      await show("유효한 시작 가격을 입력해주세요.");
      return;
    }
    if (!startDate) {
      await show("경매 시작 날짜를 입력해주세요.");
      return;
    }
    if (!endDate) {
      await show("경매 종료 날짜를 입력해주세요.");
      return;
    }

    // 날짜와 시간을 조합하여 한국 시간대로 직접 계산
    const createKoreaTime = (date: string, hour: string, minute: string) => {
      const koreaTime = new Date(`${date}T${hour}:${minute}:00+09:00`);
      return koreaTime;
    };
    
    const startDateTime = createKoreaTime(startDate, startHour, startMinute);
    const endDateTime = createKoreaTime(endDate, endHour, endMinute);
    
    // 백엔드로 보낼 때는 한국 시간대로 직접 문자열 생성 (yyyy-MM-dd'T'HH:mm:ss)
    const startTime = `${startDate}T${startHour}:${startMinute}:00`;
    const endTime = `${endDate}T${endHour}:${endMinute}:00`;
    
    // 현재 시간과 비교 (한국 시간대 기준)
    const now = new Date();
    
    console.log('현재 시간:', now.toLocaleString('ko-KR'));
    console.log('시작 시간:', startDateTime.toLocaleString('ko-KR'));
    console.log('종료 시간:', endDateTime.toLocaleString('ko-KR'));
    console.log('백엔드로 보낼 시작 시간:', startTime);
    console.log('백엔드로 보낼 종료 시간:', endTime);
    
    if (startDateTime <= now) {
      await show("시작 시간은 현재 시간 이후여야 합니다.");
      return;
    }
    
    if (endDateTime <= now) {
      await show("종료 시간은 현재 시간 이후여야 합니다.");
      return;
    }
    
    if (startDateTime >= endDateTime) {
      await show("종료 시간은 시작 시간보다 늦어야 합니다.");
      return;
    }

    setIsCreatingAuction(true);
    try {
      const token = localStorage.getItem('adminToken');
      
      console.log('경매 등록 요청 데이터:', {
        adminProductId: product.id,
        startPrice: numericStartPrice,
        startTime: startTime,
        endTime: endTime
      });
      
      const response = await apiClient.post('/admin/auctions', {
        adminProductId: product.id,
        startPrice: numericStartPrice,
        startTime: startTime,
        endTime: endTime
      }, {
        headers: { Authorization: `Bearer ${token}` }
      });

      console.log('경매 등록 성공:', response.data);
      await show("경매가 성공적으로 등록되었습니다.");
      
      if (onAuctionCreated) {
        onAuctionCreated();
      }
      onClose();
    } catch (error: any) {
      console.error("경매 등록 중 오류:", error);
      const errorMessage = error.response?.data?.message || "경매 등록 중 오류가 발생했습니다.";
      await show(errorMessage);
    } finally {
      setIsCreatingAuction(false);
    }
  };

  const handlePriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    // 숫자와 쉼표만 허용
    const numericValue = value.replace(/[^0-9,]/g, '');
    setStartPrice(numericValue);
  };

  const addToPrice = (amount: number) => {
    const currentPrice = Number(startPrice.replace(/,/g, '')) || 0;
    const newPrice = currentPrice + amount;
    setStartPrice(newPrice.toLocaleString());
  };

  const handlePriceFocus = (e: React.ChangeEvent<HTMLInputElement>) => {
    // 포커스 시 전체 선택
    e.target.select();
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "상": return "bg-green-500 text-white";
      case "중": return "bg-yellow-500 text-white";
      case "하": return "bg-red-500 text-white";
      case "판매중": return "bg-green-500 text-white";
      case "품절": return "bg-red-500 text-white";
      case "숨김": return "bg-gray-500 text-white";
      default: return "bg-gray-500 text-white";
    }
  };

  const getAuctionStatusColor = (isAuctioned: boolean | undefined) => {
    return isAuctioned 
      ? 'bg-blue-500 text-white' 
      : 'bg-yellow-500 text-white';
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
      <div className={`bg-white rounded-3xl max-w-4xl w-full ${showAuctionForm ? 'max-h-[95vh]' : 'max-h-[90vh]'} overflow-y-auto`}>
        {/* 헤더 */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold flex items-center gap-3 text-gray-800">
            <Package className="w-6 h-6 text-gray-600" />
            매입 상품 상세 정보
          </h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 transition-colors p-2 hover:bg-gray-100 rounded-xl"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* 상품 정보 */}
        <div className="p-6">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
            {/* 상품 이미지 섹션 */}
            <div className="space-y-4">
              <div className="aspect-square bg-gray-100 rounded-3xl overflow-hidden shadow-lg">
                {product.imageThumbnailUrl ? (
                  <img
                    src={product.imageThumbnailUrl}
                    alt={product.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center text-gray-400">
                    <div className="text-center">
                      <Package className="w-20 h-20 mx-auto mb-4" />
                      <p className="text-lg">이미지 없음</p>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* 상품 정보 섹션 */}
            <div className="space-y-4">
              {/* 상품명과 상태 */}
              <div>
                <h3 className="text-2xl font-bold text-gray-800 mb-3 leading-tight">
                  {product.name}
                </h3>
                <div className="flex items-center gap-3">
                  <span className={`px-3 py-1 rounded-full text-sm font-bold ${getStatusColor(product.status)}`}>
                    {product.status}
                  </span>
                  <span className={`px-3 py-1 rounded-full text-sm font-bold ${getAuctionStatusColor(product.isAuctioned)}`}>
                    {product.isAuctioned ? '경매 등록' : '미등록'}
                  </span>
                  <div className="flex items-center gap-1 text-yellow-500">
                    <Star className="w-4 h-4 fill-current" />
                    <span className="text-sm text-gray-600">품질 등급</span>
                  </div>
                </div>
              </div>

              {/* 기본 정보 */}
              <div className="space-y-3">
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-gray-50 p-4 rounded-xl border border-gray-200">
                    <div className="flex items-center gap-2 text-sm text-gray-600 mb-1">
                      <DollarSign className="w-4 h-4" />
                      매입가
                    </div>
                    <div className="text-xl font-bold text-green-600">
                      {product.purchasePrice ? product.purchasePrice.toLocaleString() : 'N/A'}원
                    </div>
                  </div>
                  
                  <div className="bg-gray-50 p-4 rounded-xl border border-gray-200">
                    <div className="flex items-center gap-2 text-sm text-gray-600 mb-1">
                      <Package className="w-4 h-4" />
                      재고
                    </div>
                    <div className="text-xl font-bold text-gray-800">
                      {product.stock}개
                    </div>
                  </div>
                </div>

                {/* 상세 정보 */}
                <div className="space-y-2">
                  <div className="flex items-center gap-3 text-sm">
                    <Package className="w-4 h-4 text-gray-500" />
                    <span className="text-gray-600 w-16">카테고리:</span>
                    <span className="font-medium text-gray-800">{product.categoryName}</span>
                  </div>

                  {(product.width || product.depth || product.height) && (
                    <div className="flex items-center gap-3 text-sm">
                      <div className="w-4 h-4 flex items-center justify-center">
                        <div className="w-3 h-3 border border-gray-400 rounded-sm"></div>
                      </div>
                      <span className="text-gray-600 w-16">사이즈:</span>
                      <span className="font-medium text-gray-800">
                        {product.width && `${product.width}W`}
                        {product.depth && ` × ${product.depth}D`}
                        {product.height && ` × ${product.height}H`}cm
                      </span>
                    </div>
                  )}

                  <div className="flex items-center gap-3 text-sm">
                    <Calendar className="w-4 h-4 text-gray-500" />
                    <span className="text-gray-600 w-16">매입일:</span>
                    <span className="font-medium text-gray-800">
                      {product.purchasedAt ? formatDate(product.purchasedAt) : 'N/A'}
                    </span>
                  </div>

                  <div className="flex items-center gap-3 text-sm">
                    <Calendar className="w-4 h-4 text-gray-500" />
                    <span className="text-gray-600 w-16">등록일:</span>
                    <span className="font-medium text-gray-800">{formatDate(product.createdAt)}</span>
                  </div>
                </div>
              </div>

              {/* 상품 설명 - 경매 등록 폼이 열려있을 때는 숨김 */}
              {product.description && !showAuctionForm && (
                <div className="bg-gray-50 p-4 rounded-xl border border-gray-200">
                  <h4 className="font-semibold text-gray-800 mb-3 flex items-center gap-2">
                    <Eye className="w-4 h-4" />
                    상품 설명
                  </h4>
                  <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-wrap">
                    {product.description}
                  </p>
                </div>
              )}

              {/* 경매 등록 버튼 */}
              {!product.isAuctioned && (
                <div className="pt-4 border-t border-gray-200">
                  {!showAuctionForm ? (
                    <button
                      onClick={() => setShowAuctionForm(true)}
                      className="w-full bg-gray-800 text-white py-3 px-4 rounded-xl hover:bg-gray-700 transition-colors font-medium flex items-center justify-center gap-2"
                    >
                      <Gavel className="w-5 h-5" />
                      경매 등록하기
                    </button>
                  ) : (
                    <div className="space-y-3">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">
                            시작 가격 (원)
                          </label>
                          <div className="space-y-2">
                            <input
                              type="text"
                              value={startPrice}
                              onChange={handlePriceChange}
                              onFocus={handlePriceFocus}
                              className="w-full px-3 py-2 border border-gray-300 rounded-xl focus:ring-2 focus:ring-gray-400 focus:border-transparent"
                              placeholder="시작 가격을 입력하세요"
                            />
                            <div className="flex gap-2">
                              <button
                                type="button"
                                onClick={() => addToPrice(10000)}
                                className="flex-1 px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
                              >
                                +10,000원
                              </button>
                              <button
                                type="button"
                                onClick={() => addToPrice(50000)}
                                className="flex-1 px-3 py-1 text-sm bg-gray-800 text-white rounded-lg hover:bg-gray-700 transition-colors"
                              >
                                +50,000원
                              </button>
                            </div>
                          </div>
                        </div>
                        
                        <div>
                          <Label className="block text-sm font-medium text-gray-700 mb-2">
                            경매 기간
                          </Label>
                          <div className="space-y-3">
                            {/* 시작시간 */}
                            <div>
                              <Label className="text-sm text-gray-600 mb-1 flex items-center gap-1">
                                <PlayCircle className="w-4 h-4 text-green-600" />
                                시작
                              </Label>
                              <div className="space-y-2">
                                <div>
                                  <Label className="text-xs text-gray-500 mb-1 block">날짜</Label>
                                  <Input
                                    type="date"
                                    value={startDate}
                                    onChange={(e) => setStartDate(e.target.value)}
                                    className="w-full h-8 text-sm"
                                  />
                                </div>
                                <div className="flex gap-2">
                                  <div className="flex-1">
                                    <Label className="text-xs text-gray-500 mb-1 block">시간</Label>
                                    <Select value={startHour} onValueChange={setStartHour}>
                                      <SelectTrigger className="h-10 text-sm">
                                        <SelectValue placeholder="시 선택" />
                                      </SelectTrigger>
                                      <SelectContent className="max-h-48">
                                        {Array.from({ length: 24 }, (_, i) => (
                                          <SelectItem key={i} value={i.toString().padStart(2, '0')}>
                                            {i.toString().padStart(2, '0')}시
                                          </SelectItem>
                                        ))}
                                      </SelectContent>
                                    </Select>
                                  </div>
                                  <div className="flex-1">
                                    <Label className="text-xs text-gray-500 mb-1 block">분</Label>
                                    <Select value={startMinute} onValueChange={setStartMinute}>
                                      <SelectTrigger className="h-10 text-sm">
                                        <SelectValue placeholder="분 선택" />
                                      </SelectTrigger>
                                      <SelectContent className="max-h-48">
                                        {Array.from({ length: 60 }, (_, i) => (
                                          <SelectItem key={i} value={i.toString().padStart(2, '0')}>
                                            {i.toString().padStart(2, '0')}분
                                          </SelectItem>
                                        ))}
                                      </SelectContent>
                                    </Select>
                                  </div>
                                </div>
                              </div>
                            </div>
                            
                            {/* 종료시간 */}
                            <div>
                              <Label className="text-sm text-gray-600 mb-1 flex items-center gap-1">
                                <StopCircle className="w-4 h-4 text-red-600" />
                                종료
                              </Label>
                              <div className="space-y-2">
                                <div>
                                  <Label className="text-xs text-gray-500 mb-1 block">날짜</Label>
                                  <Input
                                    type="date"
                                    value={endDate}
                                    onChange={(e) => setEndDate(e.target.value)}
                                    className="w-full h-8 text-sm"
                                  />
                                </div>
                                <div className="flex gap-2">
                                  <div className="flex-1">
                                    <Label className="text-xs text-gray-500 mb-1 block">시간</Label>
                                    <Select value={endHour} onValueChange={setEndHour}>
                                      <SelectTrigger className="h-10 text-sm">
                                        <SelectValue placeholder="시 선택" />
                                      </SelectTrigger>
                                      <SelectContent className="max-h-48">
                                        {Array.from({ length: 24 }, (_, i) => (
                                          <SelectItem key={i} value={i.toString().padStart(2, '0')}>
                                            {i.toString().padStart(2, '0')}시
                                          </SelectItem>
                                        ))}
                                      </SelectContent>
                                    </Select>
                                  </div>
                                  <div className="flex-1">
                                    <Label className="text-xs text-gray-500 mb-1 block">분</Label>
                                    <Select value={endMinute} onValueChange={setEndMinute}>
                                      <SelectTrigger className="h-10 text-sm">
                                        <SelectValue placeholder="분 선택" />
                                      </SelectTrigger>
                                      <SelectContent className="max-h-48">
                                        {Array.from({ length: 60 }, (_, i) => (
                                          <SelectItem key={i} value={i.toString().padStart(2, '0')}>
                                            {i.toString().padStart(2, '0')}분
                                          </SelectItem>
                                        ))}
                                      </SelectContent>
                                    </Select>
                                  </div>
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                      
                      <div className="flex gap-3">
                        <button
                          onClick={handleCreateAuction}
                          disabled={isCreatingAuction}
                          className="flex-1 bg-gray-800 text-white py-2 px-4 rounded-xl hover:bg-gray-700 transition-colors disabled:opacity-50"
                        >
                          {isCreatingAuction ? "등록 중..." : "경매 등록"}
                        </button>
                        <button
                          onClick={() => setShowAuctionForm(false)}
                          className="px-4 py-2 border border-gray-300 text-gray-700 rounded-xl hover:bg-gray-50 transition-colors"
                        >
                          취소
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}

              {/* 이미 경매 등록된 경우 */}
              {product.isAuctioned && (
                <div className="pt-4 border-t border-gray-200">
                  <div className="bg-blue-50 p-4 rounded-xl border border-blue-200">
                    <div className="flex items-center gap-2 text-blue-800">
                      <Gavel className="w-5 h-5" />
                      <span className="font-medium">이미 경매에 등록되어 있습니다</span>
                    </div>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 