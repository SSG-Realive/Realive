"use client";
import React, { useState, useEffect } from "react";
import { X, Package, DollarSign, Calendar, User, ShoppingCart, Eye, Star } from "lucide-react";
import { useGlobalDialog } from "@/app/context/dialogContext";

interface Product {
  id: number;
  name: string;
  categoryId: number;
  categoryName: string;
  price: number;
  stock: number;
  createdAt: string;
  status: "상" | "중" | "하";
  imageThumbnailUrl?: string;  // 백엔드 응답과 일치하도록 수정
  imageUrls?: string[];        // 백엔드 응답과 일치하도록 수정
  description?: string;
  sellerName?: string;
  width?: number;
  depth?: number;
  height?: number;
}

interface ProductDetailModalProps {
  product: Product | null;
  isOpen: boolean;
  onClose: () => void;
  onPurchase?: (productId: number, purchasePrice: number, quantity: number) => void;
}

export default function ProductDetailModal({ 
  product, 
  isOpen, 
  onClose, 
  onPurchase 
}: ProductDetailModalProps) {
  const [purchasePrice, setPurchasePrice] = useState<string>("");
  const [purchaseQuantity, setPurchaseQuantity] = useState<number>(1);
  const [isPurchasing, setIsPurchasing] = useState(false);
  const [showPurchaseForm, setShowPurchaseForm] = useState(false);
  const {show} = useGlobalDialog();
  // 모달이 열릴 때마다 초기화
  useEffect(() => {
    if (isOpen) {
      setPurchasePrice("");
      setPurchaseQuantity(1);
      setShowPurchaseForm(false);
    }
  }, [isOpen]);

  if (!isOpen || !product) return null;

  const handlePurchase = async () => {
    if (!onPurchase) return;
    
    // 쉼표 제거 후 숫자로 변환
    const numericPrice = Number(purchasePrice.replace(/,/g, ''));
    console.log('매입 가격 변환:', {
      original: purchasePrice,
      numeric: numericPrice,
      type: typeof numericPrice
    });
    
    if (numericPrice <= 0 || isNaN(numericPrice)) {
      show("유효한 매입 가격을 입력해주세요.");
      return;
    }
    if (purchaseQuantity <= 0) {
      show("매입 수량을 입력해주세요.");
      return;
    }
    if (purchaseQuantity > product.stock) {
      show("매입 수량이 재고보다 많습니다.");
      return;
    }

    setIsPurchasing(true);
    try {
      await onPurchase(product.id, numericPrice, purchaseQuantity);
      onClose();
    } catch (error) {
      console.error("매입 처리 중 오류:", error);
      await show("매입 처리 중 오류가 발생했습니다.");
    } finally {
      setIsPurchasing(false);
    }
  };

  const handlePriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    // 숫자와 쉼표만 허용
    const numericValue = value.replace(/[^0-9,]/g, '');
    setPurchasePrice(numericValue);
  };

  const addToPrice = (amount: number) => {
    const currentPrice = Number(purchasePrice.replace(/,/g, '')) || 0;
    const newPrice = currentPrice + amount;
    setPurchasePrice(newPrice.toLocaleString());
  };

  const handlePriceFocus = (e: React.ChangeEvent<HTMLInputElement>) => {
    // 포커스 시 전체 선택
    e.target.select();
  };

  const formatPriceForDisplay = (price: string) => {
    const numericPrice = Number(price.replace(/,/g, ''));
    return numericPrice > 0 ? numericPrice.toLocaleString() : '';
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
      <div className="bg-white rounded-3xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
        {/* 헤더 */}
        <div className="flex items-center justify-between p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold flex items-center gap-3 text-gray-800">
            <Package className="w-6 h-6 text-gray-600" />
            상품 상세 정보
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
                    onError={(e) => {
                      // 이미지 로드 실패 시 기본 박스로 대체
                      e.currentTarget.style.display = 'none';
                      e.currentTarget.nextElementSibling?.classList.remove('hidden');
                    }}
                  />
                ) : null}
                {/* 기본 이미지 박스 */}
                <div className={`w-full h-full bg-gradient-to-br from-gray-100 to-gray-200 flex items-center justify-center ${product.imageThumbnailUrl ? 'hidden' : ''}`}>
                  <div className="text-center">
                    <Package className="w-20 h-20 text-gray-400 mx-auto mb-4" />
                    <p className="text-lg text-gray-500">이미지 없음</p>
                  </div>
                </div>
              </div>
              
              {/* 추가 이미지가 있다면 썸네일 표시 */}
              {product.imageUrls && product.imageUrls.length > 1 && (
                <div className="flex gap-2 overflow-x-auto">
                  {product.imageUrls.slice(1, 5).map((image, index) => (
                    <div key={index} className="w-16 h-16 bg-gray-100 rounded-xl overflow-hidden flex-shrink-0">
                      <img
                        src={image}
                        alt={`${product.name} ${index + 2}`}
                        className="w-full h-full object-cover"
                      />
                    </div>
                  ))}
                </div>
              )}
            </div>

            {/* 상품 정보 섹션 */}
            <div className="space-y-6">
              {/* 상품명과 상태 */}
              <div>
                <h3 className="text-2xl font-bold text-gray-800 mb-3 leading-tight">
                  {product.name}
                </h3>
                <div className="flex items-center gap-3">
                  <span className={`px-3 py-1 rounded-full text-sm font-bold ${getStatusColor(product.status)}`}>
                    {product.status}
                  </span>
                  <div className="flex items-center gap-1 text-yellow-500">
                    <Star className="w-4 h-4 fill-current" />
                    <span className="text-sm text-gray-600">품질 등급</span>
                  </div>
                </div>
              </div>

              {/* 기본 정보 */}
              <div className="space-y-4">
                <div className="grid grid-cols-2 gap-4">
                  <div className="bg-gray-50 p-4 rounded-xl border border-gray-200">
                    <div className="flex items-center gap-2 text-sm text-gray-600 mb-1">
                      <DollarSign className="w-4 h-4" />
                      판매가
                    </div>
                    <div className="text-xl font-bold text-green-600">
                      {product.price.toLocaleString()}원
                    </div>
                  </div>
                  
                  <div className="bg-gray-50 p-4 rounded-xl border border-gray-200">
                    <div className="flex items-center gap-2 text-sm text-gray-600 mb-1">
                      <ShoppingCart className="w-4 h-4" />
                      재고
                    </div>
                    <div className="text-xl font-bold text-gray-800">
                      {product.stock}개
                    </div>
                  </div>
                </div>

                {/* 상세 정보 */}
                <div className="space-y-3">
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
                    <span className="text-gray-600 w-16">등록일:</span>
                    <span className="font-medium text-gray-800">{formatDate(product.createdAt)}</span>
                  </div>

                  {product.sellerName && (
                    <div className="flex items-center gap-3 text-sm">
                      <User className="w-4 h-4 text-gray-500" />
                      <span className="text-gray-600 w-16">판매자:</span>
                      <span className="font-medium text-gray-800">{product.sellerName}</span>
                    </div>
                  )}
                </div>
              </div>

              {/* 상품 설명 */}
              {product.description && (
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

              {/* 매입 버튼 */}
              {onPurchase && (
                <div className="pt-4 border-t border-gray-200">
                  {!showPurchaseForm ? (
                    <button
                      onClick={() => setShowPurchaseForm(true)}
                      className="w-full bg-gray-800 text-white py-3 px-4 rounded-xl hover:bg-gray-700 transition-colors font-medium flex items-center justify-center gap-2"
                    >
                      <ShoppingCart className="w-5 h-5" />
                      상품 매입하기
                    </button>
                  ) : (
                    <div className="space-y-4">
                      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">
                            매입 가격 (원)
                          </label>
                          <div className="space-y-2">
                            <input
                              type="text"
                              value={purchasePrice}
                              onChange={handlePriceChange}
                              onFocus={handlePriceFocus}
                              className="w-full px-3 py-2 border border-gray-300 rounded-xl focus:ring-2 focus:ring-gray-400 focus:border-transparent"
                              placeholder="매입 가격을 입력하세요"
                            />
                            <div className="flex gap-2">
                              <button
                                type="button"
                                onClick={() => addToPrice(1000)}
                                className="flex-1 px-3 py-1 text-sm bg-gray-100 text-gray-700 rounded-lg hover:bg-gray-200 transition-colors"
                              >
                                +1,000원
                              </button>
                              <button
                                type="button"
                                onClick={() => addToPrice(10000)}
                                className="flex-1 px-3 py-1 text-sm bg-gray-800 text-white rounded-lg hover:bg-gray-700 transition-colors"
                              >
                                +10,000원
                              </button>
                            </div>
                          </div>
                        </div>
                        
                        <div>
                          <label className="block text-sm font-medium text-gray-700 mb-2">
                            매입 수량
                          </label>
                          <input
                            type="number"
                            value={purchaseQuantity}
                            onChange={(e) => setPurchaseQuantity(Number(e.target.value))}
                            min="1"
                            max={product.stock}
                            className="w-full px-3 py-2 border border-gray-300 rounded-xl focus:ring-2 focus:ring-gray-400 focus:border-transparent"
                          />
                          <p className="text-xs text-gray-500 mt-1">최대 {product.stock}개</p>
                        </div>
                      </div>
                      
                      <div className="flex gap-3">
                        <button
                          onClick={handlePurchase}
                          disabled={isPurchasing}
                          className="flex-1 bg-gray-800 text-white py-2 px-4 rounded-xl hover:bg-gray-700 transition-colors disabled:opacity-50"
                        >
                          {isPurchasing ? "처리 중..." : "매입 완료"}
                        </button>
                        <button
                          onClick={() => setShowPurchaseForm(false)}
                          className="px-4 py-2 border border-gray-300 text-gray-700 rounded-xl hover:bg-gray-50 transition-colors"
                        >
                          취소
                        </button>
                      </div>
                    </div>
                  )}
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 