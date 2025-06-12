'use client';

import { useEffect, useState, ChangeEvent, FormEvent } from 'react';
import { useRouter, useParams } from 'next/navigation';
import Header from '@/components/Header';
import { getProductDetail, updateProduct, fetchCategories } from '@/service/productService';
import { SellerCategoryDTO } from '@/types/category/sellerCategory';
import { ProductDetail } from '@/types/product';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';

export default function ProductEditPage() {
    const checking = useSellerAuthGuard();
    const router = useRouter();
    const params = useParams();
    const productId = params?.id as string;

    const [form, setForm] = useState<ProductDetail | null>(null);
    const [categories, setCategories] = useState<SellerCategoryDTO[]>([]);
    const [parentCategoryId, setParentCategoryId] = useState<number | ''>('');
    const [imageThumbnail, setImageThumbnail] = useState<File | null>(null);
    const [videoThumbnail, setVideoThumbnail] = useState<File | null>(null);
    const [subImages, setSubImages] = useState<FileList | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState<boolean>(true);

    useEffect(() => {
        if (checking) return;
        if (!productId) return;

        const fetchData = async () => {
            try {
                const [productData, categoryData] = await Promise.all([
                    getProductDetail(Number(productId)),
                    fetchCategories()
                ]);

                setForm(productData);
                setCategories(categoryData);

                // parentCategoryId 초기 설정
                const selectedCategory = categoryData.find(cat => cat.id === productData.categoryId);
                if (selectedCategory?.parentId) {
                    setParentCategoryId(selectedCategory.parentId);
                } else {
                    setParentCategoryId(selectedCategory ? selectedCategory.id : '');
                }

                setError(null);
            } catch (err) {
                console.error('상품/카테고리 불러오기 실패', err);
                setError('상품 또는 카테고리 정보를 불러오지 못했습니다.');
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, [productId, checking]);

    const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
        if (!form) return;
        const { name, value } = e.target;
        setForm({ ...form, [name]: value });
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!form) return;

        // 카테고리 필수 체크
        if (!form.categoryId || form.categoryId === 0) {
            alert('카테고리를 선택해주세요.');
            return;
        }

        const formData = new FormData();
        formData.append('name', form.name);
        formData.append('description', form.description);
        formData.append('price', String(form.price));
        formData.append('stock', String(form.stock || 1));
        formData.append('width', String(form.width || 0));
        formData.append('depth', String(form.depth || 0));
        formData.append('height', String(form.height || 0));
        formData.append('status', form.status);
        formData.append('active', String(form.active));
        formData.append('categoryId', String(form.categoryId));

        // 이미지
        if (imageThumbnail) formData.append('imageThumbnail', imageThumbnail);
        if (videoThumbnail) formData.append('videoThumbnail', videoThumbnail);
        if (subImages) {
            Array.from(subImages).forEach((file) => formData.append('subImages', file));
        }

        // 배송 정책
        if (form.deliveryPolicy) {
            formData.append('deliveryPolicy.type', form.deliveryPolicy.type);
            formData.append('deliveryPolicy.cost', String(form.deliveryPolicy.cost));
            formData.append('deliveryPolicy.regionLimit', form.deliveryPolicy.regionLimit);
        }

        try {
            await updateProduct(Number(productId), formData);
            alert('상품이 수정되었습니다.');
            router.push('/seller/products');
        } catch (err) {
            console.error('수정 실패', err);
            setError('상품 수정 중 오류가 발생했습니다.');
        }
    };

    const handleParentCategoryChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const parentId = e.target.value ? Number(e.target.value) : '';
        setParentCategoryId(parentId);
        setForm(form ? { ...form, categoryId: 0 } : null); // subCategory 초기화
    };

    const handleSubCategoryChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const subCategoryId = e.target.value ? Number(e.target.value) : 0;
        setForm(form ? { ...form, categoryId: subCategoryId } : null);
    };

    if (checking) return <div className="p-8">인증 확인 중...</div>;
    if (loading) return <div className="p-4">로딩 중...</div>;
    if (error) return <div className="p-4 text-red-600">{error}</div>;
    if (!form) return <div className="p-4">상품 정보를 불러올 수 없습니다.</div>;

    const parentCategories = categories.filter(cat => cat.parentId === null);
    const subCategories = categories.filter(cat => cat.parentId === Number(parentCategoryId));

    return (
        <>
            <Header />
            <div style={{ maxWidth: 700, margin: '0 auto', padding: '2rem' }}>
                <h1 className="text-xl font-bold mb-4">상품 수정</h1>

                <form onSubmit={handleSubmit} encType="multipart/form-data">
                    {/* 카테고리 선택 */}
                    <div className="mb-4">
                        <label>카테고리 (1차)</label>
                        <select
                            value={parentCategoryId}
                            onChange={handleParentCategoryChange}
                            className="w-full p-2 border mt-1"
                            required
                        >
                            <option value="">-- 선택 --</option>
                            {parentCategories.map(cat => (
                                <option key={cat.id} value={cat.id}>{cat.name}</option>
                            ))}
                        </select>
                    </div>

                    <div className="mb-4">
                        <label>카테고리 (2차)</label>
                        <select
                            value={form.categoryId || ''}
                            onChange={handleSubCategoryChange}
                            className="w-full p-2 border mt-1"
                            required
                        >
                            <option value="">-- 선택 --</option>
                            {subCategories.map(cat => (
                                <option key={cat.id} value={cat.id}>{cat.name}</option>
                            ))}
                        </select>
                    </div>

                    {/* 상품명 */}
                    <div className="mb-4">
                        <label>상품명</label>
                        <input name="name" value={form.name} onChange={handleChange} required className="w-full p-2 border mt-1" />
                    </div>

                    {/* 가격 */}
                    <div className="mb-4">
                        <label>가격</label>
                        <input type="number" name="price" value={form.price} onChange={handleChange} required className="w-full p-2 border mt-1" />
                    </div>

                    {/* 상품 설명 */}
                    <div className="mb-4">
                        <label>상품 설명</label>
                        <textarea name="description" value={form.description} onChange={handleChange} className="w-full p-2 border mt-1" rows={5} />
                    </div>

                    {/* 재고 */}
                    <div className="mb-4">
                        <label>재고</label>
                        <input type="number" name="stock" value={form.stock} onChange={handleChange} className="w-full p-2 border mt-1" />
                    </div>

                    {/* 크기 */}
                    <div className="mb-4">
                        <label>가로 (Width)</label>
                        <input type="number" name="width" value={form.width} onChange={handleChange} className="w-full p-2 border mt-1" />
                    </div>

                    <div className="mb-4">
                        <label>세로 (Depth)</label>
                        <input type="number" name="depth" value={form.depth} onChange={handleChange} className="w-full p-2 border mt-1" />
                    </div>

                    <div className="mb-4">
                        <label>높이 (Height)</label>
                        <input type="number" name="height" value={form.height} onChange={handleChange} className="w-full p-2 border mt-1" />
                    </div>

                    {/* 상태 */}
                    <div className="mb-4">
                        <label>상품 상태</label>
                        <select
                            name="status"
                            value={form.status}
                            onChange={handleChange}
                            className="w-full p-2 border mt-1"
                            required
                        >
                            <option value="상">상</option>
                            <option value="중">중</option>
                            <option value="하">하</option>
                        </select>
                    </div>

                    {/* 활성화 여부 */}
                    <div className="mb-4">
                        <label>활성화 여부</label>
                        <select
                            name="active"
                            value={form.active ? 'true' : 'false'}
                            onChange={(e) =>
                                setForm({ ...form, active: e.target.value === 'true' })
                            }
                            className="w-full p-2 border mt-1"
                        >
                            <option value="true">활성</option>
                            <option value="false">비활성</option>
                        </select>
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
                    {form.deliveryPolicy && (
                        <>
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
                        </>
                    )}

                    {error && <p className="text-red-500 mb-2">{error}</p>}

                    <button type="submit" className="w-full bg-blue-600 text-white py-2 mt-2">수정하기</button>
                </form>
            </div>
        </>
    );
}
