'use client';

import { useState, useEffect } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getProductDetail, deleteProduct } from '@/service/seller/productService';
import { ProductDetail } from '@/types/seller/product/product';
import SellerLayout from '@/components/layouts/SellerLayout';
import { Edit, Trash2, Package, DollarSign, Eye, Armchair } from 'lucide-react';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function SellerProductDetailPage() {
    const params = useParams();
    const router = useRouter();
  const checking = useSellerAuthGuard();
    const [product, setProduct] = useState<ProductDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState(false);

  const productId = params.id as string;

    useEffect(() => {
        if (checking) return;

        const fetchProduct = async () => {
            try {
        setLoading(true);
        const productData = await getProductDetail(Number(productId));
        setProduct(productData);
      } catch (error) {
        console.error('상품 조회 실패:', error);
        alert('상품을 불러오는데 실패했습니다.');
        router.push('/seller/products');
            } finally {
                setLoading(false);
            }
        };

    if (productId) {
        fetchProduct();
    }
  }, [productId, checking, router]);

    const handleEdit = () => {
        router.push(`/seller/products/${productId}/edit`);
    };

  const handleDelete = async () => {
    if (window.confirm('정말로 이 상품을 삭제하시겠습니까?')) {
      try {
        setDeleting(true);
        await deleteProduct(Number(productId));
        alert('상품이 성공적으로 삭제되었습니다.');
        router.push('/seller/products');
      } catch (error) {
        console.error('상품 삭제 실패:', error);
        alert('상품 삭제에 실패했습니다.');
      } finally {
        setDeleting(false);
      }
    }
  };
    
  if (checking || loading) {
    return (
      <SellerLayout>
        <div className="min-h-screen bg-white p-6">
          <div className="flex items-center justify-center py-16">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#374151]"></div>
            <span className="ml-3 text-[#374151] text-lg">상품 정보를 불러오는 중...</span>
            </div>
        </div>
      </SellerLayout>
    );
  }
    
  if (!product) {
    return (
      <SellerLayout>
        <div className="min-h-screen bg-white p-6">
          <div className="text-center py-16">
            <h2 className="text-xl font-bold text-[#374151] mb-4">상품을 찾을 수 없습니다</h2>
            <button
              onClick={() => router.push('/seller/products')}
              className="bg-[#d1d5db] text-[#374151] px-4 py-2 rounded-lg hover:bg-[#e5e7eb] transition-colors"
            >
              상품 목록으로 돌아가기
            </button>
            </div>
        </div>
      </SellerLayout>
    );
  }

    return (
            <SellerLayout>
      <div className="h-screen bg-white p-4 overflow-hidden">
        {/* 헤더 */}
        <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-4">
          <div>
            <h1 className="text-2xl font-extrabold text-[#374151] tracking-wide mb-2">상품 상세 정보</h1>
            <p className="text-sm text-[#6b7280]">상품의 세부 정보를 확인하고 관리할 수 있습니다.</p>
          </div>
          <div className="flex gap-3 mt-4 md:mt-0">
            <button
              onClick={handleEdit}
              className="inline-flex items-center gap-2 bg-[#d1d5db] text-[#374151] px-4 py-2 rounded-lg hover:bg-[#e5e7eb] transition-colors font-medium shadow-sm border border-[#d1d5db]"
            >
              <Edit className="w-4 h-4" />
              수정
            </button>
            <button
              onClick={handleDelete}
              disabled={deleting}
              className="inline-flex items-center gap-2 bg-[#6b7280] text-white px-4 py-2 rounded-lg hover:bg-[#374151] transition-colors font-medium shadow-sm disabled:opacity-50"
            >
              <Trash2 className="w-4 h-4" />
              {deleting ? '삭제 중...' : '삭제'}
            </button>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-10 h-[calc(100vh-12rem)]">
          {/* 왼쪽: 상품 이미지 + 크기 정보 + 판매자 정보 */}
          <div className="space-y-7 h-full overflow-y-auto">
            {/* 상품 이미지 */}
            <div className="bg-[#f3f4f6] rounded-xl shadow border-2 border-[#d1d5db] p-7">
              <h3 className="text-base font-bold text-[#374151] mb-5 flex items-center gap-2">
                <Eye className="w-4 h-4 text-[#6b7280]" />
                상품 이미지
              </h3>
              <div className="aspect-square bg-white rounded-lg border-2 border-[#d1d5db] overflow-hidden w-80 mx-auto">
                {product.imageThumbnailUrl ? (
                  <img
                    src={product.imageThumbnailUrl}
                    alt={product.name}
                    className="w-full h-full object-cover"
                  />
                ) : (
                  <div className="w-full h-full flex items-center justify-center bg-[#f3f4f6]">
                    <Armchair className="w-8 h-8 text-[#6b7280]" />
                  </div>
                )}
              </div>
            </div>

            {/* 크기 정보 */}
            <div className="bg-[#f3f4f6] rounded-xl shadow border-2 border-[#d1d5db] p-7">
              <h3 className="text-base font-bold text-[#374151] mb-5">크기 정보</h3>
              <div className="grid grid-cols-3 gap-5">
                <div className="text-center">
                  <label className="text-xs font-medium text-[#6b7280] block mb-1">너비</label>
                  <p className="text-[#374151] font-semibold text-base">{product.width || 0}cm</p>
                </div>
                <div className="text-center">
                  <label className="text-xs font-medium text-[#6b7280] block mb-1">높이</label>
                  <p className="text-[#374151] font-semibold text-base">{product.height || 0}cm</p>
                </div>
                <div className="text-center">
                  <label className="text-xs font-medium text-[#6b7280] block mb-1">깊이</label>
                  <p className="text-[#374151] font-semibold text-base">{product.depth || 0}cm</p>
                </div>
              </div>
            </div>

            {/* 판매자 정보 */}
            <div className="bg-[#f3f4f6] rounded-xl shadow border-2 border-[#d1d5db] p-7">
              <h3 className="text-base font-bold text-[#374151] mb-5">판매자 정보</h3>
              <div className="grid grid-cols-2 gap-5">
                <div className="text-center">
                  <label className="text-xs font-medium text-[#6b7280] block mb-1">판매자명</label>
                  <p className="text-[#374151] font-semibold text-base">{product.sellerName}</p>
                </div>
                <div className="text-center">
                  <label className="text-xs font-medium text-[#6b7280] block mb-1">판매자 ID</label>
                  <p className="text-[#374151] font-mono text-base">{product.sellerId}</p>
                </div>
              </div>
            </div>
          </div>

          {/* 오른쪽: 상품 기본 정보 + 상품 설명 + 정보 카드들 */}
          <div className="space-y-7 h-full overflow-y-auto">
            <div className="bg-[#f3f4f6] rounded-xl shadow border-2 border-[#d1d5db] p-7">
              <h3 className="text-lg font-bold text-[#374151] mb-5">기본 정보</h3>
              <div className="space-y-5">
                <div>
                  <label className="text-sm font-medium text-[#6b7280] block mb-1">상품명</label>
                  <p className="text-[#374151] font-semibold text-lg">{product.name}</p>
                </div>
                <div>
                  <label className="text-sm font-medium text-[#6b7280] block mb-1">카테고리</label>
                  <p className="text-[#374151] text-lg">{product.categoryName}</p>
                </div>
              </div>
            </div>

            {/* 상품 설명 */}
            <div className="bg-[#f3f4f6] rounded-xl shadow border-2 border-[#d1d5db] p-7">
              <h3 className="text-lg font-bold text-[#374151] mb-5">상품 설명</h3>
              <div className="bg-white rounded-lg p-6 border border-[#d1d5db] max-h-40 overflow-y-auto">
                <p className="text-[#374151] text-base leading-relaxed whitespace-pre-wrap break-words">
                  {product.description}
                </p>
              </div>
            </div>

            {/* 정보 카드들 */}
            <div className="grid grid-cols-2 gap-8">
              <div className="bg-[#f3f4f6] p-7 rounded-xl shadow border-2 border-[#d1d5db] flex items-center gap-5">
                <DollarSign className="w-6 h-6 text-[#6b7280]" />
                <div>
                  <p className="text-xs font-medium text-[#6b7280]">가격</p>
                  <p className="text-base font-bold text-[#374151]">{product.price.toLocaleString()}원</p>
                </div>
              </div>

              <div className="bg-[#f3f4f6] p-7 rounded-xl shadow border-2 border-[#d1d5db] flex items-center gap-5">
                <Package className="w-6 h-6 text-[#6b7280]" />
                <div>
                  <p className="text-xs font-medium text-[#6b7280]">재고</p>
                  <p className="text-base font-bold text-[#374151]">{product.stock}개</p>
                </div>
              </div>

              <div className="bg-[#f3f4f6] p-7 rounded-xl shadow border-2 border-[#d1d5db] flex items-center gap-5">
                <Eye className="w-6 h-6 text-[#6b7280]" />
                <div>
                  <p className="text-xs font-medium text-[#6b7280]">상태</p>
                  <p className="text-base font-bold text-[#374151]">
                    {product.stock === 0
                      ? '품절'
                      : !product.isActive
                        ? '판매중지'
                        : '판매중'}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </SellerLayout>
    );
}
