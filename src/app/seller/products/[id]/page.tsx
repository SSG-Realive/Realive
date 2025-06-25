'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getProductDetail, deleteProduct } from '@/service/seller/productService';
import { ProductDetail } from '@/types/seller/product/product';
import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore';
import { ArrowLeft, Edit, Trash2, Package, DollarSign, Layers, Tag, Ruler, Eye, AlertCircle } from 'lucide-react';

export default function ProductDetailPage() {
    const checking = useSellerAuthGuard();

    const params = useParams();
    const router = useRouter();
    const productId = Number(params?.id);

    const [product, setProduct] = useState<ProductDetail | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        if (checking) return;

        const token = useSellerAuthStore.getState().accessToken;
        if (!token) {
            router.push('/seller/login');
            return;
        }

        if (!productId) return;

        const fetchProduct = async () => {
            try {
                const data = await getProductDetail(productId);
                setProduct(data);
                setError(null);
            } catch (err) {
                console.error(err);
                setError('상품 정보를 불러오지 못했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchProduct();
    }, [productId, checking]);

    const handleDelete = async () => {
        if (!confirm('정말로 이 상품을 삭제하시겠습니까?')) return;

        try {
            await deleteProduct(productId);
            alert('삭제 완료');
            router.push('/seller/products');
        } catch (err) {
            console.error(err);
            alert('삭제 실패');
        }
    };

    const handleEdit = () => {
        router.push(`/seller/products/${productId}/edit`);
    };

    if (checking) return (
        <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-gray-50 flex items-center justify-center">
            <div className="text-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
                <p className="text-gray-600">인증 확인 중...</p>
            </div>
        </div>
    );
    
    if (loading) return (
        <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-gray-50 flex items-center justify-center">
            <div className="text-center">
                <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
                <p className="text-gray-600">상품 정보를 불러오는 중...</p>
            </div>
        </div>
    );
    
    if (error) return (
        <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-gray-50 flex items-center justify-center">
            <div className="text-center">
                <AlertCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
                <p className="text-red-600">{error}</p>
            </div>
        </div>
    );
    
    if (!product) return (
        <div className="w-full max-w-full min-h-screen overflow-x-hidden bg-gray-50 flex items-center justify-center">
            <div className="text-center">
                <Package className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                <p className="text-gray-600">상품 정보를 불러올 수 없습니다.</p>
            </div>
        </div>
    );

    return (
        <>
            <div className="hidden">
                <SellerHeader />
            </div>
            <SellerLayout>
                <div className="flex-1 w-full h-full px-4 py-8 bg-[#a89f91]">
                    {/* 헤더 */}
                    <div className="flex items-center gap-4 mb-6">
                        <button
                            onClick={() => router.push('/seller/products')}
                            className="flex items-center gap-2 text-[#bfa06a] hover:text-[#5b4636] transition-colors font-bold"
                        >
                            <ArrowLeft className="w-5 h-5" />
                            상품 목록으로
                        </button>
                        <h1 className="text-xl md:text-2xl font-bold text-[#5b4636]">상품 상세</h1>
                    </div>

                    {/* 상품 기본 정보 카드 */}
                    <div className="bg-[#e9dec7] rounded-xl shadow border border-[#bfa06a] p-8 mb-6">
                        <div className="flex flex-col lg:flex-row gap-6">
                            {/* 상품 이미지 */}
                            <div className="lg:w-1/3">
                                <div className="aspect-square bg-[#e9dec7] rounded-xl overflow-hidden border border-[#bfa06a]">
                                    {product.imageThumbnailUrl ? (
                                        <img
                                            src={product.imageThumbnailUrl}
                                            alt={product.name}
                                            className="w-full h-full object-cover"
                                        />
                                    ) : (
                                        <div className="w-full h-full flex items-center justify-center">
                                            <Package className="w-16 h-16 text-[#bfa06a]" />
                                        </div>
                                    )}
                                </div>
                            </div>

                            {/* 상품 기본 정보 */}
                            <div className="lg:w-2/3">
                                <h2 className="text-2xl font-bold text-[#5b4636] mb-4">{product.name}</h2>
                                
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                                    <div className="flex items-center gap-3 p-3 bg-[#e9dec7] rounded-xl border border-[#bfa06a]">
                                        <DollarSign className="w-5 h-5 text-[#bfa06a]" />
                                        <div>
                                            <p className="text-sm text-[#5b4636]">가격</p>
                                            <p className="font-bold text-[#5b4636]">{product.price.toLocaleString()}원</p>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3 p-3 bg-[#e9dec7] rounded-xl border border-[#bfa06a]">
                                        <Layers className="w-5 h-5 text-[#bfa06a]" />
                                        <div>
                                            <p className="text-sm text-[#5b4636]">재고</p>
                                            <p className="font-bold text-[#5b4636]">{product.stock}개</p>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3 p-3 bg-[#e9dec7] rounded-xl border border-[#bfa06a]">
                                        <Tag className="w-5 h-5 text-[#bfa06a]" />
                                        <div>
                                            <p className="text-sm text-[#5b4636]">상태</p>
                                            <span className={`px-2 py-1 rounded text-xs font-bold ${
                                                product.status === '상' ? 'bg-green-100 text-green-700' : 
                                                product.status === '중' ? 'bg-yellow-100 text-yellow-700' : 
                                                'bg-red-100 text-red-700'
                                            }`}>
                                                {product.status}
                                            </span>
                                        </div>
                                    </div>
                                    <div className="flex items-center gap-3 p-3 bg-[#e9dec7] rounded-xl border border-[#bfa06a]">
                                        <Eye className="w-5 h-5 text-[#bfa06a]" />
                                        <div>
                                            <p className="text-sm text-[#5b4636]">활성화</p>
                                            <span className={`px-2 py-1 rounded text-xs font-bold ${
                                                product.isActive 
                                                    ? 'bg-green-100 text-green-700' 
                                                    : 'bg-red-100 text-red-700'
                                            }`}>
                                                {product.isActive ? '활성' : '비활성'}
                                            </span>
                                        </div>
                                    </div>
                                </div>

                                {/* 액션 버튼 */}
                                <div className="flex flex-col sm:flex-row gap-3">
                                    <button
                                        onClick={handleEdit}
                                        className="flex items-center justify-center gap-2 bg-[#bfa06a] hover:bg-[#5b4636] text-[#4b3a2f] hover:text-[#e9dec7] px-6 py-3 rounded-lg font-medium transition-colors"
                                    >
                                        <Edit className="w-4 h-4" />
                                        상품 수정
                                    </button>
                                    <button
                                        onClick={handleDelete}
                                        className="flex items-center justify-center gap-2 bg-red-100 hover:bg-red-200 text-red-700 px-6 py-3 rounded-lg font-medium transition-colors"
                                    >
                                        <Trash2 className="w-4 h-4" />
                                        상품 삭제
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* 상품 상세 정보 */}
                    <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                        {/* 상품 정보 */}
                        <div className="bg-[#e9dec7] rounded-xl shadow border border-[#bfa06a] p-8">
                            <h3 className="text-lg font-semibold text-[#5b4636] mb-4 flex items-center gap-2">
                                <Package className="w-5 h-5 text-[#bfa06a]" />
                                상품 정보
                            </h3>
                            <div className="space-y-4">
                                <div>
                                    <p className="text-sm font-medium text-[#5b4636] mb-1">상품 설명</p>
                                    <p className="text-[#5b4636] break-words">{product.description}</p>
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-[#5b4636] mb-1">카테고리</p>
                                    <p className="text-[#5b4636]">{product.categoryName}</p>
                                </div>
                                {product.width && product.depth && product.height && (
                                    <div>
                                        <p className="text-sm font-medium text-[#5b4636] mb-1 flex items-center gap-2">
                                            <Ruler className="w-4 h-4 text-[#bfa06a]" />
                                            크기 (가로 x 세로 x 높이)
                                        </p>
                                        <p className="text-[#5b4636]">{product.width} x {product.depth} x {product.height}</p>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* 판매자 정보 */}
                        <div className="bg-[#e9dec7] rounded-xl shadow border border-[#bfa06a] p-8">
                            <h3 className="text-lg font-semibold text-[#5b4636] mb-4">판매자 정보</h3>
                            <div className="space-y-4">
                                <div>
                                    <p className="text-sm font-medium text-[#5b4636] mb-1">판매자명</p>
                                    <p className="text-[#5b4636]">{product.sellerName}</p>
                                </div>
                                <div>
                                    <p className="text-sm font-medium text-[#5b4636] mb-1">판매자 ID</p>
                                    <p className="text-[#5b4636]">{product.sellerId}</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </SellerLayout>
        </>
    );
}
