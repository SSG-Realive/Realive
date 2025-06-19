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

        const token = useSellerAuthStore.getState().token;
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

    if (checking) return <div className="p-8">인증 확인 중...</div>;
    if (loading) return <div className="p-4">로딩 중...</div>;
    if (error) return <div className="p-4 text-red-600">{error}</div>;
    if (!product) return <div className="p-4">상품 정보를 불러올 수 없습니다.</div>;

    return (
        <>
            <SellerHeader />
            <SellerLayout>
                <div className="max-w-3xl mx-auto p-6">
                    <h1 className="text-2xl font-bold mb-4">{product.name}</h1>
                    <p className="mb-2">상품 설명: {product.description}</p>
                    <p className="mb-2">가격: {product.price.toLocaleString()}원</p>
                    <p className="mb-2">재고: {product.stock}</p>
                    <p className="mb-2">크기: {product.width} x {product.depth} x {product.height}</p>
                    <p className="mb-2">상태: {product.status}</p>
                    <p className="mb-2">활성화 여부: {product.active ? '활성' : '비활성'}</p>
                    <p className="mb-2">카테고리 : {product.categoryName}</p>

                    <h2 className="mt-6 font-semibold">배송 정보</h2>
                    {product.deliveryPolicy ? (
                        <ul className="list-disc list-inside ml-4">
                            <li>유형: {product.deliveryPolicy.type}</li>
                            <li>비용: {product.deliveryPolicy.cost}원</li>
                            <li>지역 제한: {product.deliveryPolicy.regionLimit}</li>
                        </ul>
                    ) : (
                        <p className="text-gray-600">배송 정보가 없습니다.</p>
                    )}

                    <div className="flex gap-4 mt-8">
                        <button
                            onClick={handleEdit}
                            className="bg-blue-600 text-white px-4 py-2 rounded"
                        >
                            수정하기
                        </button>
                        <button
                            onClick={handleDelete}
                            className="bg-red-600 text-white px-4 py-2 rounded"
                        >
                            삭제하기
                        </button>
                    </div>
                </div>
            </SellerLayout>
        </>
    );
}
