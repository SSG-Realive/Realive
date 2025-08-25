'use client';

import { useEffect, useState, ChangeEvent, FormEvent } from 'react';
import { useRouter, useParams } from 'next/navigation';
import { getProductDetail, updateProduct, fetchCategories } from '@/service/seller/productService';
import { SellerCategoryDTO } from '@/types/seller/category/sellerCategory';
import { ProductDetail } from '@/types/seller/product/product';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import SellerLayout from '@/components/layouts/SellerLayout';
import { Package, Tag, DollarSign, Layers, Ruler, Image, Video, Save, AlertCircle } from 'lucide-react';
import { useGlobalDialog } from '@/app/context/dialogContext';

export default function ProductEditPage() {
    const checking = useSellerAuthGuard();
    const router = useRouter();
    const params = useParams();
    const { show } = useGlobalDialog();
    const productId = params?.id as string;

    const [form, setForm] = useState<ProductDetail | null>(null);
    const [categories, setCategories] = useState<SellerCategoryDTO[]>([]);
    const [parentCategoryIdState, setParentCategoryIdState] = useState<number | ''>('');
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

                const selectedCategory = categoryData.find(cat => cat.id === productData.categoryId);
                const parentId = selectedCategory?.parentId ?? '';

                setForm({
                    ...productData,
                });

                setParentCategoryIdState(parentId);
                setCategories(categoryData);
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
        if (name === 'stock') {
            const newStock = Number(value);
            setForm({
                ...form,
                stock: newStock,
            });
            
            if (newStock === 0 && form.isActive) {
                console.log('경고: 재고가 0이지만 상품이 활성화되어 있습니다.');
            }
        } else {
            setForm({ ...form, [name]: value });
        }
    };

    const handleSubmit = async (e: FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!form) return;

        if (!form.categoryId || form.categoryId === 0) {
            await show('카테고리를 선택해주세요.');
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
        formData.append('active', String(form.isActive));
        formData.append('categoryId', String(form.categoryId));

        if (imageThumbnail) {
            formData.append('imageThumbnail', imageThumbnail);
        }
        if (videoThumbnail) {
            formData.append('videoThumbnail', videoThumbnail);
        }
        if (subImages) {
            Array.from(subImages).forEach((file) => {
                formData.append('subImages', file);
            });
        }

        try {
            await updateProduct(Number(productId), formData);
            await show('상품이 수정되었습니다.');
            router.push('/seller/products');
        } catch (err: any) {
            let errorMessage = '상품 수정 중 오류가 발생했습니다.';
            
            if (err.response?.status === 400) {
                errorMessage = '입력 데이터가 올바르지 않습니다. 필수 항목을 확인해주세요.';
            } else if (err.response?.status === 401) {
                errorMessage = '로그인이 필요합니다.';
            } else if (err.response?.status === 403) {
                errorMessage = '해당 상품을 수정할 권한이 없습니다.';
            } else if (err.response?.status === 404) {
                errorMessage = '상품을 찾을 수 없습니다.';
            } else if (err.response?.status >= 500) {
                errorMessage = '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.';
            } else if (err.response?.data?.message) {
                errorMessage = err.response.data.message;
            }
            
            setError(errorMessage);
            await show(`수정 실패: ${errorMessage}`);
        }
    };

    const handleParentCategoryChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const parentId = e.target.value ? Number(e.target.value) : '';
        setParentCategoryIdState(parentId);
        setForm(form ? { ...form, categoryId: 0 } : null);
    };

    const handleSubCategoryChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const subCategoryId = e.target.value ? Number(e.target.value) : 0;
        setForm(form ? { ...form, categoryId: subCategoryId } : null);
    };

    if (checking || loading) {
        return (
            <SellerLayout>
                <div className="min-h-screen bg-white p-6">
                    <div className="flex items-center justify-center py-16">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#374151]"></div>
                        <span className="ml-3 text-[#374151] text-lg">
                            {checking ? '인증 확인 중...' : '상품 정보를 불러오는 중...'}
                        </span>
            </div>
        </div>
            </SellerLayout>
    );
    }
    
    if (error) {
        return (
            <SellerLayout>
                <div className="min-h-screen bg-white p-6">
                    <div className="text-center py-16">
                        <AlertCircle className="w-16 h-16 text-red-500 mx-auto mb-4" />
                        <p className="text-red-600 font-medium">{error}</p>
                        <button
                            onClick={() => router.push('/seller/products')}
                            className="mt-4 bg-[#d1d5db] text-[#374151] px-4 py-2 rounded-lg hover:bg-[#e5e7eb] transition-colors"
                        >
                            상품 목록으로 돌아가기
                        </button>
            </div>
        </div>
            </SellerLayout>
        );
    }

    if (!form) {
    return (
            <SellerLayout>
                <div className="min-h-screen bg-white p-6">
                    <div className="text-center py-16">
                        <Package className="w-16 h-16 text-[#6b7280] mx-auto mb-4" />
                        <p className="text-[#6b7280] font-medium">상품 정보를 불러올 수 없습니다.</p>
                        <button
                            onClick={() => router.push('/seller/products')}
                            className="mt-4 bg-[#d1d5db] text-[#374151] px-4 py-2 rounded-lg hover:bg-[#e5e7eb] transition-colors"
                        >
                            상품 목록으로 돌아가기
                        </button>
                    </div>
                </div>
            </SellerLayout>
        );
    }

    const parentCategories = categories.filter(cat => !cat.parentId);
    const subCategories = categories.filter(cat => cat.parentId === parentCategoryIdState);

    return (
        <SellerLayout>
            <div className="min-h-screen bg-white p-6">
                {/* 헤더 */}
                <div className="flex flex-col md:flex-row md:items-center md:justify-between mb-8">
                                <div>
                        <h1 className="text-2xl font-extrabold text-[#374151] tracking-wide mb-2">상품 수정</h1>
                        <p className="text-sm text-[#6b7280]">상품의 정보를 수정하고 업데이트할 수 있습니다.</p>
                    </div>
                    <button
                        onClick={() => router.push('/seller/products')}
                        className="mt-4 md:mt-0 bg-[#d1d5db] text-[#374151] px-4 py-2 rounded-lg hover:bg-[#e5e7eb] transition-colors font-medium shadow-sm border border-[#d1d5db]"
                    >
                        상품 목록으로
                    </button>
                                </div>

                <form onSubmit={handleSubmit} className="space-y-8">
                    {/* 기본 정보 섹션 */}
                    <div className="bg-[#f3f4f6] rounded-xl shadow border-2 border-[#d1d5db] p-6">
                        <h2 className="text-lg font-bold text-[#374151] mb-6 flex items-center gap-2">
                            <Package className="w-5 h-5 text-[#6b7280]" />
                            기본 정보
                        </h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                <div>
                                <label className="block text-sm font-medium text-[#374151] mb-2">상품명</label>
                                <input 
                                    type="text"
                                    name="name" 
                                    value={form.name} 
                                    onChange={handleChange} 
                                    className="w-full px-4 py-3 border border-[#d1d5db] rounded-xl focus:ring-2 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151]"
                                    required 
                                />
                            </div>
                            <div>
                                <label className="block text-sm font-medium text-[#374151] mb-2">상품 상태</label>
                                <select
                                    name="status"
                                    value={form.status}
                                    onChange={handleChange}
                                    className="w-full px-4 py-3 border border-[#d1d5db] rounded-xl focus:ring-2 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151]"
                                    required
                                >
                                    <option value="">선택해주세요</option>
                                    <option value="상">상급</option>
                                    <option value="중">중급</option>
                                    <option value="하">하급</option>
                                </select>
                            </div>
                            <div className="md:col-span-2">
                                <label className="block text-sm font-medium text-[#374151] mb-2">상품 설명</label>
                                <textarea 
                                    name="description" 
                                    value={form.description} 
                                    onChange={handleChange} 
                                    rows={4}
                                    className="w-full px-4 py-3 border border-[#d1d5db] rounded-xl focus:ring-2 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151]"
                                    required
                                />
                            </div>
                            </div>
                        </div>

                    {/* 가격 및 재고, 카테고리, 크기 정보 - 컴팩트 그리드 */}
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
                        {/* 가격 및 재고 */}
                        <div className="bg-[#f3f4f6] rounded-lg shadow border border-[#d1d5db] p-3">
                            <h2 className="text-sm font-bold text-[#374151] mb-3 flex items-center gap-1">
                                <DollarSign className="w-3 h-3 text-[#6b7280]" />
                                가격 및 재고
                            </h2>
                            <div className="space-y-3">
                                <div>
                                    <label className="block text-xs font-medium text-[#374151] mb-1">판매 가격</label>
                                    <input 
                                        type="number" 
                                        name="price" 
                                        value={form.price} 
                                        onChange={handleChange} 
                                        min="0"
                                        className="w-full px-2 py-1.5 border border-[#d1d5db] rounded focus:ring-1 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] text-sm"
                                        required 
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium text-[#374151] mb-1">재고 수량</label>
                                    <input 
                                        type="number" 
                                        name="stock" 
                                        value={form.stock} 
                                        onChange={handleChange} 
                                        min="0"
                                        className="w-full px-2 py-1.5 border border-[#d1d5db] rounded focus:ring-1 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] text-sm"
                                        required 
                                    />
                                </div>
                            </div>
                        </div>

                        {/* 카테고리 */}
                        <div className="bg-[#f3f4f6] rounded-lg shadow border border-[#d1d5db] p-3">
                            <h2 className="text-sm font-bold text-[#374151] mb-3 flex items-center gap-1">
                                <Tag className="w-3 h-3 text-[#6b7280]" />
                                카테고리
                            </h2>
                            <div className="space-y-3">
                                <div>
                                    <label className="block text-xs font-medium text-[#374151] mb-1">상위 카테고리</label>
                                    <select
                                        value={parentCategoryIdState}
                                        onChange={handleParentCategoryChange}
                                        className="w-full px-2 py-1.5 border border-[#d1d5db] rounded focus:ring-1 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] text-sm"
                                    >
                                        <option value="">선택</option>
                                        {parentCategories.map(cat => (
                                            <option key={cat.id} value={cat.id}>{cat.name}</option>
                                        ))}
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-xs font-medium text-[#374151] mb-1">하위 카테고리</label>
                                    <select
                                        value={form.categoryId || ''}
                                        onChange={handleSubCategoryChange}
                                        className="w-full px-2 py-1.5 border border-[#d1d5db] rounded focus:ring-1 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] text-sm"
                                        required
                                    >
                                        <option value="">선택</option>
                                        {subCategories.map(cat => (
                                            <option key={cat.id} value={cat.id}>{cat.name}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                        </div>

                        {/* 크기 정보 */}
                        <div className="bg-[#f3f4f6] rounded-lg shadow border border-[#d1d5db] p-3">
                            <h2 className="text-sm font-bold text-[#374151] mb-3 flex items-center gap-1">
                                <Ruler className="w-3 h-3 text-[#6b7280]" />
                                크기 정보
                            </h2>
                            <div className="space-y-3">
                                <div>
                                    <label className="block text-xs font-medium text-[#374151] mb-1">너비 (cm)</label>
                                    <input 
                                        type="number" 
                                        name="width" 
                                        value={form.width || ''}
                                        onChange={handleChange} 
                                        min="0"
                                        step="0.1"
                                        className="w-full px-2 py-1.5 border border-[#d1d5db] rounded focus:ring-1 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] text-sm"
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium text-[#374151] mb-1">깊이 (cm)</label>
                                    <input 
                                        type="number" 
                                        name="depth" 
                                        value={form.depth || ''}
                                        onChange={handleChange} 
                                        min="0"
                                        step="0.1"
                                        className="w-full px-2 py-1.5 border border-[#d1d5db] rounded focus:ring-1 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] text-sm"
                                    />
                                </div>
                                <div>
                                    <label className="block text-xs font-medium text-[#374151] mb-1">높이 (cm)</label>
                                    <input 
                                        type="number" 
                                        name="height" 
                                        value={form.height || ''}
                                        onChange={handleChange} 
                                        min="0"
                                        step="0.1"
                                        className="w-full px-2 py-1.5 border border-[#d1d5db] rounded focus:ring-1 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] text-sm"
                                    />
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* 판매 상태 섹션 */}
                    <div className="bg-[#f3f4f6] rounded-lg shadow border border-[#d1d5db] p-4">
                        <h2 className="text-sm font-bold text-[#374151] mb-3 flex items-center gap-1">
                            <Layers className="w-3 h-3 text-[#6b7280]" />
                            판매 상태
                        </h2>
                        <div className="flex items-center gap-3 p-3 bg-white rounded border border-[#d1d5db]">
                            <div className="relative">
                                <input
                                    type="checkbox"
                                    id="isActive"
                                    checked={form.isActive}
                                    onChange={(e) => setForm({ ...form, isActive: e.target.checked })}
                                    className="w-5 h-5 text-[#6b7280] bg-white border-2 border-[#d1d5db] rounded focus:ring-2 focus:ring-[#6b7280] checked:bg-[#6b7280] checked:border-[#6b7280]"
                                />
                            </div>
                                <div>
                                <label htmlFor="isActive" className="text-sm font-medium text-[#374151] cursor-pointer">
                                    판매 활성화
                                    </label>
                                <p className="text-xs text-[#6b7280] mt-1">
                                    체크하면 고객이 상품을 구매할 수 있습니다.
                                </p>
                            </div>
                        </div>
                                        </div>

                    {/* 미디어 업로드 섹션 */}
                    <div className="bg-[#f3f4f6] rounded-xl shadow border-2 border-[#d1d5db] p-6">
                        <h2 className="text-lg font-bold text-[#374151] mb-6 flex items-center gap-2">
                            <Image className="w-5 h-5 text-[#6b7280]" />
                            미디어 업로드
                        </h2>
                        <div className="space-y-6">
                            <div>
                                <label className="block text-sm font-medium text-[#374151] mb-2">대표 이미지</label>
                                    <input 
                                        type="file" 
                                        accept="image/*" 
                                        onChange={(e) => setImageThumbnail(e.target.files?.[0] || null)} 
                                    className="w-full px-4 py-3 border border-[#d1d5db] rounded-xl focus:ring-2 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-[#6b7280] file:text-white hover:file:bg-[#374151]"
                                    />
                                </div>
                                <div>
                                <label className="block text-sm font-medium text-[#374151] mb-2">대표 영상</label>
                                    <input
                                        type="file" 
                                        accept="video/*" 
                                        onChange={(e) => setVideoThumbnail(e.target.files?.[0] || null)} 
                                    className="w-full px-4 py-3 border border-[#d1d5db] rounded-xl focus:ring-2 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-[#6b7280] file:text-white hover:file:bg-[#374151]"
                                    />
                                </div>
                                <div>
                                <label className="block text-sm font-medium text-[#374151] mb-2">추가 이미지</label>
                                    <input
                                        type="file" 
                                        accept="image/*" 
                                        multiple 
                                        onChange={(e) => setSubImages(e.target.files)} 
                                    className="w-full px-4 py-3 border border-[#d1d5db] rounded-xl focus:ring-2 focus:ring-[#6b7280] focus:border-transparent bg-white text-[#374151] file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-semibold file:bg-[#6b7280] file:text-white hover:file:bg-[#374151]"
                                    />
                            </div>
                                </div>
                            </div>

                        {/* 제출 버튼 */}
                    <div className="flex justify-center">
                            <button 
                                type="submit" 
                            className="inline-flex items-center gap-2 bg-[#6b7280] text-white px-8 py-4 rounded-xl font-semibold hover:bg-[#374151] transition-colors shadow-lg"
                            >
                            <Save className="w-5 h-5" />
                            상품 수정 완료
                            </button>
                        </div>
                    </form>
                </div>
            </SellerLayout>
    );
}
