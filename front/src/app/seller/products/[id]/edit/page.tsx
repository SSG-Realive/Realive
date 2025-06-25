'use client';

import { useEffect, useState, ChangeEvent, FormEvent } from 'react';
import { useRouter, useParams } from 'next/navigation';
import SellerHeader from '@/components/seller/SellerHeader';
import { getProductDetail, updateProduct, fetchCategories } from '@/service/seller/productService';
import { SellerCategoryDTO } from '@/types/seller/category/sellerCategory';
import { ProductDetail } from '@/types/seller/product/product';
import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
import SellerLayout from '@/components/layouts/SellerLayout';

export default function ProductEditPage() {
    const checking = useSellerAuthGuard();
    const router = useRouter();
    const params = useParams();
    const productId = params?.id as string;

    const [form, setForm] = useState<ProductDetail | null>(null);
    const [categories, setCategories] = useState<SellerCategoryDTO[]>([]);
    const [parentCategoryIdState, setParentCategoryIdState] = useState<number | ''>(''); // ✅ UI 용 parentCategoryId
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

                // parentCategoryId 계산
                const selectedCategory = categoryData.find(cat => cat.id === productData.categoryId);
                const parentId = selectedCategory?.parentId ?? '';

                // form 세팅
                setForm({
                    ...productData,
                });

                // parentCategoryId state 에만 유지
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
                isActive: newStock === 0 ? false : form.isActive,  // 🚩 재고가 0이면 isActive false 강제 설정
            });
        } else {
            setForm({ ...form, [name]: value });
        }
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
        formData.append('active', String(form.isActive));
        formData.append('categoryId', String(form.categoryId)); // ✅ categoryId 만 서버 전송

        // 이미지
        if (imageThumbnail) formData.append('imageThumbnail', imageThumbnail);
        if (videoThumbnail) formData.append('videoThumbnail', videoThumbnail);
        if (subImages) {
            Array.from(subImages).forEach((file) => formData.append('subImages', file));
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
        setParentCategoryIdState(parentId); // ✅ select box state 유지
        setForm(form ? { ...form, categoryId: 0 } : null); // ✅ subCategory 초기화
    };

    const handleSubCategoryChange = (e: ChangeEvent<HTMLSelectElement>) => {
        const subCategoryId = e.target.value ? Number(e.target.value) : 0;
        setForm(form ? { ...form, categoryId: subCategoryId } : null);
    };

    if (checking) return <div className="p-4 sm:p-8">인증 확인 중...</div>;
    if (loading) return <div className="p-4">로딩 중...</div>;
    if (error) return <div className="p-4 text-red-600">{error}</div>;
    if (!form) return <div className="p-4">상품 정보를 불러올 수 없습니다.</div>;

    const parentCategories = categories.filter(cat => cat.parentId === null);
    const subCategories = categories.filter(cat => cat.parentId === Number(parentCategoryIdState));

    return (
        <>
            <div className="hidden">
            <SellerHeader />
            </div>
            <SellerLayout>
                <div className="max-w-4xl mx-auto p-4 sm:p-6">
                    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4 sm:p-6">
                        <h1 className="text-xl sm:text-2xl font-bold mb-6 text-gray-900">상품 수정</h1>

                        <form onSubmit={handleSubmit} encType="multipart/form-data" className="space-y-6">
                        {/* 카테고리 선택 */}
                            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        카테고리 (1차)
                                    </label>
                            <select
                                value={parentCategoryIdState}
                                onChange={handleParentCategoryChange}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                required
                            >
                                <option value="">-- 선택 --</option>
                                {parentCategories.map(cat => (
                                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                                ))}
                            </select>
                        </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        카테고리 (2차)
                                    </label>
                            <select
                                value={form.categoryId || ''}
                                onChange={handleSubCategoryChange}
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                required
                            >
                                <option value="">-- 선택 --</option>
                                {subCategories.map(cat => (
                                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                                ))}
                            </select>
                        </div>
                            </div>

                        {/* 상품명 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    상품명
                                </label>
                                <input 
                                    name="name" 
                                    value={form.name} 
                                    onChange={handleChange} 
                                    required 
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    placeholder="상품명을 입력하세요"
                                />
                        </div>

                        {/* 가격 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    가격
                                </label>
                                <input 
                                    type="number" 
                                    name="price" 
                                    value={form.price} 
                                    onChange={handleChange} 
                                    required 
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    placeholder="가격을 입력하세요"
                                />
                        </div>

                        {/* 상품 설명 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    상품 설명
                                </label>
                                <textarea 
                                    name="description" 
                                    value={form.description} 
                                    onChange={handleChange} 
                                    required 
                                    rows={5}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 resize-none"
                                    placeholder="상품 설명을 입력하세요"
                                />
                        </div>

                        {/* 재고 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    재고
                                </label>
                                <input 
                                    type="number" 
                                    name="stock" 
                                    value={form.stock} 
                                    onChange={handleChange} 
                                    required 
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                    placeholder="재고 수량을 입력하세요"
                                />
                        </div>

                        {/* 크기 */}
                            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        가로 (Width)
                                    </label>
                                    <input 
                                        type="number" 
                                        name="width" 
                                        value={form.width} 
                                        onChange={handleChange} 
                                        required 
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        placeholder="가로"
                                    />
                        </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        세로 (Depth)
                                    </label>
                                    <input 
                                        type="number" 
                                        name="depth" 
                                        value={form.depth} 
                                        onChange={handleChange} 
                                        required 
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        placeholder="세로"
                                    />
                        </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        높이 (Height)
                                    </label>
                                    <input 
                                        type="number" 
                                        name="height" 
                                        value={form.height} 
                                        onChange={handleChange} 
                                        required 
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                        placeholder="높이"
                                    />
                                </div>
                        </div>

                        {/* 상태 */}
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    상품 상태
                                </label>
                            <select
                                name="status"
                                value={form.status}
                                onChange={handleChange}
                                    className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                                required
                            >
                                <option value="상">상</option>
                                <option value="중">중</option>
                                <option value="하">하</option>
                            </select>
                        </div>

                        {/* 활성화 여부 */}
                            <div className="flex items-center">
                            <input
                                type="checkbox"
                                    id="active"
                                    checked={form.isActive}
                                disabled={form.stock === 0}  // 재고가 0이면 체크박스 비활성화
                                onChange={(e) => {
                                    if (form.stock === 0 && e.target.checked) {
                                        alert('재고가 0인 상태에서는 상품을 활성화할 수 없습니다.');
                                        return;  // 체크 방지
                                    }
                                        setForm({ ...form, isActive: e.target.checked });
                                }}
                                    className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                            />
                                <label htmlFor="active" className="ml-2 block text-sm text-gray-700">
                                    활성화 여부
                                </label>
                        </div>

                        {/* 썸네일 및 서브 이미지 */}
                            <div className="space-y-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        대표 이미지
                                    </label>
                                    {form?.imageThumbnailUrl && (
                                <div className="mb-2 text-sm text-gray-600">
                                    현재 등록된 파일명: {form.imageThumbnailUrl.split('/').pop()}
                                </div>
                            )}
                                    <input 
                                        type="file" 
                                        accept="image/*" 
                                        onChange={(e) => setImageThumbnail(e.target.files?.[0] || null)} 
                                        required={form?.imageThumbnailUrl ? false : true} 
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 file:mr-4 file:py-1 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        대표 영상
                                    </label>
                                    <input
                                        type="file" 
                                        accept="video/*" 
                                        onChange={(e) => setVideoThumbnail(e.target.files?.[0] || null)} 
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 file:mr-4 file:py-1 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-2">
                                        서브 이미지
                                    </label>
                                    <input
                                        type="file" 
                                        accept="image/*" 
                                        multiple 
                                        onChange={(e) => setSubImages(e.target.files)} 
                                        className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 file:mr-4 file:py-1 file:px-4 file:rounded-md file:border-0 file:text-sm file:font-medium file:bg-blue-50 file:text-blue-700 hover:file:bg-blue-100"
                                    />
                                </div>
                            </div>

                            {error && (
                                <div className="bg-red-50 border border-red-200 rounded-md p-3">
                                    <p className="text-red-600 text-sm">{error}</p>
                                </div>
                            )}

                            <button 
                                type="submit" 
                                className="w-full bg-blue-600 hover:bg-blue-700 text-white py-3 px-4 rounded-lg font-medium transition-colors"
                            >
                                수정하기
                            </button>
                    </form>
                    </div>
                </div>
            </SellerLayout>
        </>
    );
}
