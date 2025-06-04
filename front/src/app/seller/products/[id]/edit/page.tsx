// 상품 수정(PUT)
'use client';

import { useEffect, useState, ChangeEvent, FormEvent } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Header from '@/components/Header';
import { getProductDetail, updateProduct } from '@/service/productService';
import { ProductDetail } from '@/types/product';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function ProductEditPage() {
     // 판매자 인증 가드를 적용
    const checking = useSellerAuthGuard();
    const router = useRouter();
    const params = useParams();
    const productId = params?.id as string;

    const [form, setForm] = useState<ProductDetail | null>(null);
    const [imageThumbnail, setImageThumbnail] = useState<File | null>(null);
    const [videoThumbnail, setVideoThumbnail] = useState<File | null>(null);
    const [subImages, setSubImages] = useState<FileList | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        if (checking) return;
        if (!productId) return;
        const fetchData = async () => {
            try {
                const data = await getProductDetail(Number(productId));
                setForm(data);
            } catch (err) {
                console.error('상품 불러오기 실패', err);
                setError('상품을 불러오지 못했습니다.');
            }
        };
        fetchData();
    }, [productId, checking]);

    const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement>) => {
        if (!form) return;
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!form) return;

        const formData = new FormData();
        formData.append('name', form.name);
        formData.append('description', form.description);
        formData.append('price', String(form.price));
        formData.append('stock', String(form.stock || 1));
        formData.append('width', String(form.width || 0));
        formData.append('depth', String(form.depth || 0));
        formData.append('height', String(form.height || 0));
        formData.append('status', form.status);
        formData.append('active', String(form.isActive));
        if (form.categoryId) formData.append('categoryId', String(form.categoryId));

        // 이미지
        if (imageThumbnail) formData.append('imageThumbnail', imageThumbnail);
        if (videoThumbnail) formData.append('videoThumbnail', videoThumbnail);
        if (subImages) {
            Array.from(subImages).forEach((file) => formData.append('subImages', file));
        }

        // 배송 정책
        formData.append('deliveryPolicy.type', form.deliveryPolicy.type);
        formData.append('deliveryPolicy.cost', String(form.deliveryPolicy.cost));
        formData.append('deliveryPolicy.regionLimit', form.deliveryPolicy.regionLimit);

        try {
            await updateProduct(Number(productId), formData);
            alert('상품이 수정되었습니다.');
            router.push('/seller/products');
        } catch (err) {
            console.error('수정 실패', err);
            setError('상품 수정 중 오류가 발생했습니다.');
        }
    };

    if (!form) return <div className="p-4">로딩 중...</div>;
    if (checking) return <div className="p-8">인증 확인 중...</div>;
    return (
        <>
            <Header />
            <div style={{ maxWidth: 700, margin: '0 auto', padding: '2rem' }}>
                <h1 className="text-xl font-bold mb-4">상품 수정</h1>

                <form onSubmit={handleSubmit} encType="multipart/form-data">
                    <div className="mb-4">
                        <label>상품명</label>
                        <input name="name" value={form.name} onChange={handleChange} required className="w-full p-2 border mt-1" />
                    </div>

                    <div className="mb-4">
                        <label>가격</label>
                        <input type="number" name="price" value={form.price} onChange={handleChange} required className="w-full p-2 border mt-1" />
                    </div>

                    <div className="mb-4">
                        <label>상품 설명</label>
                        <textarea name="description" value={form.description} onChange={handleChange} className="w-full p-2 border mt-1" rows={5} />
                    </div>

                    <div className="mb-4">
                        <label>재고</label>
                        <input type="number" name="stock" value={form.stock} onChange={handleChange} className="w-full p-2 border mt-1" />
                    </div>

                    {/* 썸네일 및 서브 이미지 */}
                    <div className="mb-4">
                        <label>대표 이미지</label>
                        <input type="file" accept="image/*" onChange={(e) => setImageThumbnail(e.target.files?.[0] || null)} className="w-full p-2 border mt-1" />
                    </div>
                    <div className="mb-4">
                        <label>대표 영상</label>
                        <input type="file" accept="video/*" onChange={(e) => setVideoThumbnail(e.target.files?.[0] || null)} className="w-full p-2 border mt-1" />
                    </div>
                    <div className="mb-4">
                        <label>서브 이미지</label>
                        <input type="file" accept="image/*" multiple onChange={(e) => setSubImages(e.target.files)} className="w-full p-2 border mt-1" />
                    </div>

                    {/* 배송 정책 */}
                    <div className="mb-4">
                        <label>배송 방식</label>
                        <select
                            name="deliveryPolicy.type"
                            value={form.deliveryPolicy.type}
                            onChange={(e) =>
                                setForm({ ...form, deliveryPolicy: { ...form.deliveryPolicy, type: e.target.value as any } })
                            }
                            className="w-full p-2 border mt-1"
                        >
                            <option value="무료배송">무료배송</option>
                            <option value="유료배송">유료배송</option>
                        </select>
                    </div>

                    <div className="mb-4">
                        <label>배송비</label>
                        <input
                            type="number"
                            name="deliveryPolicy.cost"
                            value={form.deliveryPolicy.cost}
                            onChange={(e) =>
                                setForm({ ...form, deliveryPolicy: { ...form.deliveryPolicy, cost: Number(e.target.value) } })
                            }
                            className="w-full p-2 border mt-1"
                        />
                    </div>

                    <div className="mb-4">
                        <label>지역 제한</label>
                        <input
                            name="deliveryPolicy.regionLimit"
                            value={form.deliveryPolicy.regionLimit}
                            onChange={(e) =>
                                setForm({ ...form, deliveryPolicy: { ...form.deliveryPolicy, regionLimit: e.target.value } })
                            }
                            className="w-full p-2 border mt-1"
                        />
                    </div>

                    {error && <p className="text-red-500 mb-2">{error}</p>}

                    <button type="submit" className="w-full bg-blue-600 text-white py-2 mt-2">수정하기</button>
                </form>
            </div>
        </>
    );
}