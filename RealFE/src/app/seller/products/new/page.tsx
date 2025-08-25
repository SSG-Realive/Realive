    'use client';

    import { useEffect, useState, ChangeEvent, FormEvent } from 'react';
    import { useRouter } from 'next/navigation';
    import SellerHeader from '@/components/seller/SellerHeader';
    import { createProduct, fetchCategories } from '@/service/seller/productService';
    import { SellerCategoryDTO } from '@/types/seller/category/sellerCategory';
    import useSellerAuthGuard from '@/hooks/useSellerAuthGuard';
    import SellerLayout from '@/components/layouts/SellerLayout';
    import { useGlobalDialog } from '@/app/context/dialogContext';

    export default function ProductNewPage() {
        const checking = useSellerAuthGuard();
        const router = useRouter();

        const [categories, setCategories] = useState<SellerCategoryDTO[]>([]);
        const [parentCategoryId, setParentCategoryId] = useState<number | ''>('');
        const [imageThumbnail, setImageThumbnail] = useState<File | null>(null);
        const [videoThumbnail, setVideoThumbnail] = useState<File | null>(null);
        const [subImages, setSubImages] = useState<FileList | null>(null);
        const [error, setError] = useState<string | null>(null);
        const [loading, setLoading] = useState<boolean>(true);
        const {show} = useGlobalDialog();
        const [form, setForm] = useState({
            name: '',
            description: '',
            price: 0,
            stock: 0,
            width: 0,
            depth: 0,
            height: 0,
            status: '상',
            active: true,
            categoryId: 0,
            deliveryPolicy: {
                type: '무료배송',
                cost: 0,
                regionLimit: '',
            },
        });

        useEffect(() => {
            if (checking) return;

            const fetchData = async () => {
                try {
                    const categoryData = await fetchCategories();
                    setCategories(categoryData);
                    setError(null);
                } catch (err) {
                    console.error('카테고리 불러오기 실패', err);
                    setError('카테고리 정보를 불러오지 못했습니다.');
                } finally {
                    setLoading(false);
                }
            };

            fetchData();
        }, [checking]);

        const handleChange = (e: ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
            const { name, value } = e.target;
            setForm({ ...form, [name]: value });
        };

        const handleSubmit = async (e: FormEvent) => {
            e.preventDefault();
            setError(null);

            // 카테고리 필수 체크
            if (!form.categoryId || form.categoryId === 0) {
                show('카테고리를 선택해주세요.');
                return;
            }

            if (!imageThumbnail) {
                show('대표 이미지는 필수입니다.');
                return;
            }

            const formData = new FormData();
            formData.append('name', form.name);
            formData.append('description', form.description);
            formData.append('price', String(form.price));
            formData.append('stock', String(form.stock));
            formData.append('width', String(form.width));
            formData.append('depth', String(form.depth));
            formData.append('height', String(form.height));
            formData.append('status', form.status);
            formData.append('active', String(form.active));
            formData.append('categoryId', String(form.categoryId));

            // 이미지
            formData.append('imageThumbnail', imageThumbnail);
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
                await createProduct(formData);
                show('상품이 등록되었습니다.');
                router.push('/seller/products');
            } catch (err) {
                console.error('등록 실패', err);
                setError('상품 등록 중 오류가 발생했습니다.');
            }
        };

        const handleParentCategoryChange = (e: ChangeEvent<HTMLSelectElement>) => {
            const parentId = e.target.value ? Number(e.target.value) : '';
            setParentCategoryId(parentId);
            setForm({ ...form, categoryId: 0 }); // subCategory 초기화
        };

        const handleSubCategoryChange = (e: ChangeEvent<HTMLSelectElement>) => {
            const subCategoryId = e.target.value ? Number(e.target.value) : 0;
            setForm({ ...form, categoryId: subCategoryId });
        };

        if (checking) return <div className="p-8">인증 확인 중...</div>;
        if (loading) return <div className="p-4">로딩 중...</div>;
        if (error) return <div className="p-4 text-red-600">{error}</div>;

        const parentCategories = categories.filter(cat => cat.parentId === null);
        const subCategories = categories.filter(cat => cat.parentId === Number(parentCategoryId));

        return (
            <SellerLayout>
                <main className="min-h-screen w-full px-4 py-8">
                    <h1 className="text-2xl font-extrabold mb-8 text-[#374151] tracking-wide text-center">상품 등록</h1>
                    <div className="max-w-4xl mx-auto">
                        <form onSubmit={handleSubmit} encType="multipart/form-data" className="bg-[#f3f4f6] rounded-xl shadow border border-[#d1d5db] p-8">
                            {/* 카테고리 선택 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">카테고리 (1차)</label>
                                <select
                                    value={parentCategoryId}
                                    onChange={handleParentCategoryChange}
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                    required
                                >
                                    <option value="">-- 선택 --</option>
                                    {parentCategories.map(cat => (
                                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                                    ))}
                                </select>
                            </div>

                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">카테고리 (2차)</label>
                                <select
                                    value={form.categoryId || ''}
                                    onChange={handleSubCategoryChange}
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                    required
                                >
                                    <option value="">-- 선택 --</option>
                                    {subCategories.map(cat => (
                                        <option key={cat.id} value={cat.id}>{cat.name}</option>
                                    ))}
                                </select>
                            </div>

                            {/* 상품명 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">상품명</label>
                                <input
                                    name="name"
                                    value={form.name}
                                    onChange={handleChange}
                                    required
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                    placeholder="상품명을 입력하세요"
                                />
                            </div>

                            {/* 가격 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">가격</label>
                                <input
                                    type="number"
                                    name="price"
                                    value={form.price}
                                    onChange={handleChange}
                                    required
                                    step="100"
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                    placeholder="가격을 입력하세요"
                                />
                            </div>

                            {/* 상품 설명 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">상품 설명</label>
                                <textarea
                                    name="description"
                                    value={form.description}
                                    onChange={handleChange}
                                    required
                                    rows={5}
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                    placeholder="상품 설명을 입력하세요"
                                />
                            </div>

                            {/* 재고 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">재고</label>
                                <input
                                    type="number"
                                    name="stock"
                                    value={form.stock}
                                    onChange={handleChange}
                                    required
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                    placeholder="재고 수량을 입력하세요"
                                />
                            </div>

                            {/* 크기 */}
                            <div className="grid grid-cols-3 gap-6 mb-6">
                                <div>
                                    <label className="block text-sm font-medium text-[#374151] mb-2">가로 (Width)</label>
                                    <input
                                        type="number"
                                        name="width"
                                        value={form.width}
                                        onChange={handleChange}
                                        required
                                        step="5"
                                        className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                        placeholder="가로"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-[#374151] mb-2">세로 (Depth)</label>
                                    <input
                                        type="number"
                                        name="depth"
                                        value={form.depth}
                                        onChange={handleChange}
                                        required
                                        step="5"
                                        className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                        placeholder="세로"
                                    />
                                </div>

                                <div>
                                    <label className="block text-sm font-medium text-[#374151] mb-2">높이 (Height)</label>
                                    <input
                                        type="number"
                                        name="height"
                                        value={form.height}
                                        onChange={handleChange}
                                        required
                                        step="5"
                                        className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                        placeholder="높이"
                                    />
                                </div>
                            </div>

                            {/* 상태 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">상품 상태</label>
                                <select
                                    name="status"
                                    value={form.status}
                                    onChange={handleChange}
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                    required
                                >
                                    <option value="상">상</option>
                                    <option value="중">중</option>
                                    <option value="하">하</option>
                                </select>
                            </div>

                            {/* 활성화 여부 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">활성화 여부</label>
                                <select
                                    name="active"
                                    value={form.active ? 'true' : 'false'}
                                    onChange={(e) =>
                                        setForm({ ...form, active: e.target.value === 'true' })
                                    }
                                    required
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                >
                                    <option value="true">활성</option>
                                    <option value="false">비활성</option>
                                </select>
                            </div>

                            {/* 대표 이미지 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">대표 이미지</label>
                                <input
                                    type="file"
                                    accept="image/*"
                                    onChange={(e) => setImageThumbnail(e.target.files?.[0] || null)}
                                    required
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                />
                            </div>

                            {/* 대표 영상 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">대표 영상</label>
                                <input
                                    type="file"
                                    accept="video/*"
                                    onChange={(e) => setVideoThumbnail(e.target.files?.[0] || null)}
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                />
                            </div>

                            {/* 서브 이미지 */}
                            <div className="mb-6">
                                <label className="block text-sm font-medium text-[#374151] mb-2">서브 이미지</label>
                                <input
                                    type="file"
                                    accept="image/*"
                                    multiple
                                    onChange={(e) => setSubImages(e.target.files)}
                                    className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                />
                            </div>
                            {/* 배송 정책 */}
                            {form.deliveryPolicy && (
                                <div className="space-y-6 mb-6">
                                    <div>
                                        <label className="block text-sm font-medium text-[#374151] mb-2">배송 방식</label>
                                        <select
                                            name="deliveryPolicy.type"
                                            value={form.deliveryPolicy.type}
                                            onChange={(e) =>
                                                setForm({ ...form, deliveryPolicy: { ...form.deliveryPolicy, type: e.target.value as any } })
                                            }
                                            className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                        >
                                            <option value="무료배송">무료배송</option>
                                            <option value="유료배송">유료배송</option>
                                        </select>
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-[#374151] mb-2">배송비</label>
                                        <input
                                            type="number"
                                            name="deliveryPolicy.cost"
                                            value={form.deliveryPolicy.cost}
                                            onChange={(e) =>
                                                setForm({ ...form, deliveryPolicy: { ...form.deliveryPolicy, cost: Number(e.target.value) } })
                                            }
                                            className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                            placeholder="배송비를 입력하세요"
                                        />
                                    </div>

                                    <div>
                                        <label className="block text-sm font-medium text-[#374151] mb-2">지역 제한</label>
                                        <input
                                            name="deliveryPolicy.regionLimit"
                                            value={form.deliveryPolicy.regionLimit}
                                            onChange={(e) =>
                                                setForm({ ...form, deliveryPolicy: { ...form.deliveryPolicy, regionLimit: e.target.value } })
                                            }
                                            className="w-full px-3 py-2 border border-[#d1d5db] rounded-md shadow-sm focus:outline-none focus:ring-2 focus:ring-[#d1d5db] focus:border-[#d1d5db] bg-white text-[#374151]"
                                            placeholder="배송 가능 지역을 입력하세요"
                                        />
                                    </div>
                                </div>
                            )}

                            {error && (
                                <div className="bg-red-50 border border-red-200 rounded-md p-3 mb-6">
                                    <p className="text-red-600 text-sm">{error}</p>
                                </div>
                            )}

                            <button
                                type="submit"
                                className="w-full py-3 rounded-lg font-bold text-[#374151] bg-[#d1d5db] hover:bg-[#e5e7eb] transition border border-[#d1d5db]"
                            >
                                등록하기
                            </button>
                        </form>
                    </div>
                </main>
            </SellerLayout>
        );
    }
