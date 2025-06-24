"use client";
import React, { useState, useEffect } from "react";
import { X, Package, DollarSign, Calendar, User, ShoppingCart } from "lucide-react";

interface Product {
  id: number;
  name: string;
  categoryName: string;
  price: number;
  stock: number;
  status: string;
  createdAt: string;
  sellerName?: string;
  description?: string;
  productImages?: string[];
}

interface ProductDetailModalProps {
  product: Product | null;
  isOpen: boolean;
  onClose: () => void;
  onPurchase: (productId: number, purchasePrice: number, quantity: number) => void;
}

export default function ProductDetailModal({ 
  product, 
  isOpen, 
  onClose, 
  onPurchase 
}: ProductDetailModalProps) {
  const [purchasePrice, setPurchasePrice] = useState<number>(0);
  const [purchaseQuantity, setPurchaseQuantity] = useState<number>(1);
  const [isPurchasing, setIsPurchasing] = useState(false);

  // 모달이 열릴 때마다 매입 가격 초기화
  useEffect(() => {
    if (isOpen) {
      setPurchasePrice(0);
      setPurchaseQuantity(1);
    }
  }, [isOpen]);

  if (!isOpen || !product) return null;

  const handlePurchase = async () => {
    if (purchasePrice <= 0) {
      alert("매입 가격을 입력해주세요.");
      return;
    }
    if (purchaseQuantity <= 0) {
      alert("매입 수량을 입력해주세요.");
      return;
    }
    if (purchaseQuantity > product.stock) {
      alert("매입 수량이 재고보다 많습니다.");
      return;
    }

    setIsPurchasing(true);
    try {
      await onPurchase(product.id, purchasePrice, purchaseQuantity);
      onClose();
    } catch (error) {
      console.error("매입 처리 중 오류:", error);
      alert("매입 처리 중 오류가 발생했습니다.");
    } finally {
      setIsPurchasing(false);
    }
  };

  const handlePriceChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = Number(e.target.value);
    setPurchasePrice(value);
  };

  const addToPrice = (amount: number) => {
    setPurchasePrice(prev => prev + amount);
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "판매중": return "bg-green-100 text-green-800";
      case "품절": return "bg-red-100 text-red-800";
      case "숨김": return "bg-gray-100 text-gray-800";
      default: return "bg-blue-100 text-blue-800";
    }
  };

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        {/* 헤더 */}
        <div className="flex items-center justify-between p-6 border-b">
          <h2 className="text-xl font-bold flex items-center gap-2">
            <Package className="w-5 h-5" />
            상품 상세 정보
          </h2>
          <button
            onClick={onClose}
            className="text-gray-500 hover:text-gray-700 transition-colors"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* 상품 정보 */}
        <div className="p-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* 상품 이미지 */}
            <div className="space-y-4">
              <div className="aspect-square bg-gray-100 rounded-lg flex items-center justify-center">
                {product.productImages && product.productImages.length > 0 ? (
                  <img
                    src={product.productImages[0]}
                    alt={product.name}
                    className="w-full h-full object-cover rounded-lg"
                  />
                ) : (
                  <div className="text-gray-400 text-center">
                    <Package className="w-16 h-16 mx-auto mb-2" />
                    <p>이미지 없음</p>
                  </div>
                )}
              </div>
            </div>

            {/* 상품 정보 */}
            <div className="space-y-4">
              <div>
                <h3 className="text-lg font-semibold text-gray-900 mb-2">
                  {product.name}
                </h3>
                <div className="flex items-center gap-2 mb-3">
                  <span className={`px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(product.status)}`}>
                    {product.status}
                  </span>
                </div>
              </div>

              <div className="space-y-3">
                <div className="flex items-center gap-2 text-sm">
                  <Package className="w-4 h-4 text-gray-500" />
                  <span className="text-gray-600">카테고리:</span>
                  <span className="font-medium">{product.categoryName}</span>
                </div>

                <div className="flex items-center gap-2 text-sm">
                  <DollarSign className="w-4 h-4 text-gray-500" />
                  <span className="text-gray-600">판매가:</span>
                  <span className="font-medium text-lg text-blue-600">
                    {product.price.toLocaleString()}원
                  </span>
                </div>

                <div className="flex items-center gap-2 text-sm">
                  <ShoppingCart className="w-4 h-4 text-gray-500" />
                  <span className="text-gray-600">재고:</span>
                  <span className="font-medium">{product.stock}개</span>
                </div>

                <div className="flex items-center gap-2 text-sm">
                  <Calendar className="w-4 h-4 text-gray-500" />
                  <span className="text-gray-600">등록일:</span>
                  <span className="font-medium">{product.createdAt}</span>
                </div>

                {product.sellerName && (
                  <div className="flex items-center gap-2 text-sm">
                    <User className="w-4 h-4 text-gray-500" />
                    <span className="text-gray-600">판매자:</span>
                    <span className="font-medium">{product.sellerName}</span>
                  </div>
                )}
              </div>

              {product.description && (
                <div className="mt-4">
                  <h4 className="font-medium text-gray-900 mb-2">상품 설명</h4>
                  <p className="text-sm text-gray-600 leading-relaxed">
                    {product.description}
                  </p>
                </div>
              )}
            </div>
          </div>

          {/* 매입 섹션 */}
          <div className="mt-8 p-4 bg-blue-50 rounded-lg">
            <h4 className="font-semibold text-blue-900 mb-4 flex items-center gap-2">
              <ShoppingCart className="w-5 h-5" />
              상품 매입
            </h4>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  매입 가격 (원)
                </label>
                <div className="space-y-2">
                  <div className="text-xs text-gray-500 mb-1">
                    최소 매입가: {product.price.toLocaleString()}원 (상품 판매가)
                  </div>
                  <input
                    type="number"
                    value={purchasePrice || ''}
                    onChange={handlePriceChange}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder={`최소 ${product.price.toLocaleString()}원`}
                    min={product.price}
                    step="100"
                  />
                  <div className="flex gap-2">
                    <button
                      type="button"
                      onClick={() => addToPrice(1000)}
                      className="flex-1 px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded hover:bg-blue-200 transition-colors"
                    >
                      +1,000원
                    </button>
                    <button
                      type="button"
                      onClick={() => addToPrice(10000)}
                      className="flex-1 px-3 py-1 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 transition-colors"
                    >
                      +10,000원
                    </button>
                  </div>
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">
                  매입 수량 (최대: {product.stock}개)
                </label>
                <input
                  type="number"
                  value={purchaseQuantity}
                  onChange={(e) => setPurchaseQuantity(Number(e.target.value))}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                  placeholder="매입 수량을 입력하세요"
                  min="1"
                  max={product.stock}
                />
              </div>
            </div>

            {purchasePrice > 0 && purchaseQuantity > 0 && (
              <div className="mt-4 p-3 bg-white rounded border">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">총 매입 금액:</span>
                  <span className="text-lg font-bold text-blue-600">
                    {(purchasePrice * purchaseQuantity).toLocaleString()}원
                  </span>
                </div>
              </div>
            )}

            <div className="mt-4 flex gap-3">
              <button
                onClick={handlePurchase}
                disabled={isPurchasing || purchasePrice <= 0 || purchaseQuantity <= 0}
                className="flex-1 bg-blue-600 text-white py-2 px-4 rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors flex items-center justify-center gap-2"
              >
                {isPurchasing ? (
                  <>
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                    매입 처리 중...
                  </>
                ) : (
                  <>
                    <ShoppingCart className="w-4 h-4" />
                    매입하기
                  </>
                )}
              </button>
              
              <button
                onClick={onClose}
                className="px-4 py-2 border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 transition-colors"
              >
                취소
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
} 