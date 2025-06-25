'use client';

import { useEffect, useState } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { getProductDetail, deleteProduct } from '@/service/seller/productService';
import { ProductDetail } from '@/types/seller/product/product';
import SellerHeader from '@/components/seller/SellerHeader';
import SellerLayout from '@/components/layouts/SellerLayout';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import { useSellerAuthStore } from '@/store/seller/useSellerAuthStore';

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

    if (checking) return <div className="p-4 sm:p-8">인증 확인 중...</div>;
    if (loading) return <div className="p-4">로딩 중...</div>;
    if (error) return <div className="p-4 text-red-600">{error}</div>;
    if (!product) return <div className="p-4">상품 정보를 불러올 수 없습니다.</div>;

    return (
        <>
            <div className="hidden">
            <SellerHeader />
            </div>
            <SellerLayout>
                <div className="max-w-3xl mx-auto p-4 sm:p-6">
                    <h1 className="text-xl sm:text-2xl font-bold mb-4 break-words">{product.name}</h1>
                    
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6 mb-6">
                        <h2 className="text-lg font-semibold mb-4 text-gray-900">상품 정보</h2>
                        <div className="space-y-3">
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-gray-600 min-w-[80px]">상품 설명:</span>
                                <span className="text-gray-900 break-words">{product.description}</span>
                            </div>
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-gray-600 min-w-[80px]">가격:</span>
                                <span className="text-gray-900 font-semibold">{product.price.toLocaleString()}원</span>
                            </div>
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-gray-600 min-w-[80px]">재고:</span>
                                <span className="text-gray-900">{product.stock}개</span>
                            </div>
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-gray-600 min-w-[80px]">크기:</span>
                                <span className="text-gray-900">{product.width} x {product.depth} x {product.height}</span>
                            </div>
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-gray-600 min-w-[80px]">상태:</span>
                                <span className="text-gray-900">{product.status}</span>
                            </div>
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-gray-600 min-w-[80px]">활성화:</span>
                                <span className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                                    product.isActive 
                                        ? 'bg-green-100 text-green-800' 
                                        : 'bg-red-100 text-red-800'
                                }`}>
                                    {product.isActive ? '활성' : '비활성'}
                                </span>
                            </div>
                            <div className="flex flex-col sm:flex-row sm:items-center gap-2">
                                <span className="text-sm font-medium text-gray-600 min-w-[80px]">카테고리:</span>
                                <span className="text-gray-900">{product.categoryName}</span>
                            </div>
                        </div>
                    </div>

                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6 mb-6">
                        <h2 className="text-lg font-semibold mb-4 text-gray-900">배송 정보</h2>
                        <p className="text-gray-600">배송 정보가 없습니다.</p>
                    </div>

                    <div className="flex flex-col sm:flex-row gap-3 sm:gap-4">
                        <button
                            onClick={handleEdit}
                            className="flex-1 sm:flex-none bg-blue-600 hover:bg-blue-700 text-white px-4 py-3 sm:py-2 rounded-lg font-medium transition-colors"
                        >
                            수정하기
                        </button>
                        <button
                            onClick={handleDelete}
                            className="flex-1 sm:flex-none bg-red-600 hover:bg-red-700 text-white px-4 py-3 sm:py-2 rounded-lg font-medium transition-colors"
                        >
                            삭제하기
                        </button>
                    </div>
                </div>
            </SellerLayout>
        </>
    );
}
