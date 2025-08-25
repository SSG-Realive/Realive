'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { fetchWishlist, toggleWishlist } from '@/service/customer/wishlistService';
import { ProductListDTO } from '@/types/seller/product/product';
import useDialog from '@/hooks/useDialog';
import GlobalDialog from '@/components/ui/GlobalDialog';
import useConfirm from '@/hooks/useConfirm';
import MyPageLayout from '@/components/layouts/MyPageLayout'; //

export default function WishlistPage() {
    const [products, setProducts] = useState<ProductListDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [isEditMode, setIsEditMode] = useState(false);
    const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
    const [selectedParentCategory, setSelectedParentCategory] = useState<string | null>(null);
    const router = useRouter();
    const { open, message, handleClose, show } = useDialog();
    const { confirm, dialog } = useConfirm();

    useEffect(() => {
        fetchWishlist()
            .then(setProducts)
            .catch(() => show('찜 목록을 불러오는데 실패했습니다.'))
            .finally(() => setLoading(false));
    }, []);

    const toggleEditMode = () => {
        setIsEditMode((prev) => !prev);
        setSelectedIds(new Set());
    };

    const handleSelectProduct = (productId: number) => {
        setSelectedIds((prev) => {
            const next = new Set(prev);
            next.has(productId) ? next.delete(productId) : next.add(productId);
            return next;
        });
    };

    const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedIds(e.target.checked ? new Set(filteredProducts.map((p) => p.id)) : new Set());
    };

    const handleDeleteOne = async (e: React.MouseEvent, productId: number) => {
        e.stopPropagation();
        if (!(await confirm('이 상품을 찜 목록에서 삭제하시겠습니까?'))) return;
        try {
            await toggleWishlist({ productId });
            setProducts((prev) => prev.filter((p) => p.id !== productId));
            show('찜 목록에서 제거되었습니다.');
        } catch {
            show('삭제 중 오류가 발생했습니다.');
        }
    };

    const handleDeleteSelected = async () => {
        if (selectedIds.size === 0) return show('삭제할 상품을 선택해주세요.');
        if (!(await confirm(`${selectedIds.size}개의 상품을 삭제하시겠습니까?`))) return;
        try {
            await Promise.all(Array.from(selectedIds).map((id) => toggleWishlist({ productId: id })));
            setProducts((prev) => prev.filter((p) => !selectedIds.has(p.id)));
            show(`${selectedIds.size}개의 상품이 삭제되었습니다.`);
            toggleEditMode();
        } catch {
            show('선택 삭제 중 오류가 발생했습니다.');
        }
    };

    const parentCategories = Array.from(
        new Set(products.map((p) => p.parentCategoryName).filter(Boolean))
    ) as string[];

    const filteredProducts = selectedParentCategory
        ? products.filter((p) => p.parentCategoryName === selectedParentCategory)
        : products;

    if (loading) {
        return (
            <MyPageLayout>
                <div className="text-center py-20">로딩 중...</div>
            </MyPageLayout>
        );
    }

    return (
        <MyPageLayout>
            {dialog}
            <GlobalDialog open={open} message={message} onClose={handleClose} />

            <div className="min-h-screen py-0 sm:py-2 lg:py-4">
                <main className="max-w-xl lg:max-w-4xl mx-auto px-4 space-y-6">
                    <section className="rounded-lg p-4">
                        {products.length > 0 && (
                            <>
                                {/* 카테고리 필터 */}
                                <div className="overflow-x-auto no-scrollbar mt-4 px-4">
                                    <div className="inline-flex space-x-6 text-sm text-gray-500">
                                        {/* 전체 버튼 */}
                                        <div
                                            onClick={() => setSelectedParentCategory(null)}
                                            className="cursor-pointer flex-shrink-0 px-2 py-1 relative"
                                        >
      <span className={`relative z-10 ${selectedParentCategory === null ? 'text-slate-600 font-light' : ''}`}>
        전체
      </span>
                                            {selectedParentCategory === null && (
                                                <hr className="absolute bottom-0 left-1/2 w-10 border-t border-slate-600 transform -translate-x-1/2" />
                                            )}
                                        </div>

                                        {/* 나머지 카테고리 */}
                                        {parentCategories.map((name) => (
                                            <div
                                                key={name}
                                                onClick={() => setSelectedParentCategory(name)}
                                                className="cursor-pointer flex-shrink-0 px-2 py-1 relative"
                                            >
        <span className={`relative z-10 ${selectedParentCategory === name ? 'text-slate-600 font-light' : ''}`}>
          {name}
        </span>
                                                {selectedParentCategory === name && (
                                                    <hr className="absolute bottom-0 left-1/2 w-10 border-t border-slate-600 transform -translate-x-1/2" />
                                                )}
                                            </div>
                                        ))}
                                    </div>
                                </div>

                                {/* 개수 + 편집 */}
                                <div className="flex justify-between items-center mt-4 px-4">
                                    <span className="text-sm text-gray-500">
                                        <span className="mr-1">
                                            {selectedParentCategory === null ? '전체' : selectedParentCategory}
                                        </span>
                                        <span className="text-rose-500">{filteredProducts.length}</span>개
                                    </span>
                                    <button
                                        onClick={toggleEditMode}
                                        className="text-sm text-gray-600 border border-gray-300 px-3 py-1 rounded-md hover:bg-gray-50"
                                    >
                                        {isEditMode ? '편집 취소' : '편집'}
                                    </button>
                                </div>
                            </>
                        )}
                    </section>

                    {/* 상품 리스트 */}
                    <section className="rounded-lg p-4">
                        {isEditMode && filteredProducts.length > 0 && (
                            <div className="flex items-center mb-4 pb-2 border-b">
                                <input
                                    type="checkbox"
                                    className="w-5 h-5"
                                    onChange={handleSelectAll}
                                    checked={selectedIds.size > 0 && selectedIds.size === filteredProducts.length}
                                />
                                <label className="ml-2 text-sm">
                                    전체선택 ({selectedIds.size}/{filteredProducts.length})
                                </label>
                            </div>
                        )}

                        {filteredProducts.length === 0 ? (
                            <div className="text-center py-20 text-gray-500">찜한 상품이 없습니다.</div>
                        ) : (
                            <ul className="space-y-6">
                                {filteredProducts.map((p) => (
                                    <li
                                        key={p.id}
                                        className="flex items-center rounded-lg p-4 relative border border-gray-200"
                                    >
                                        {isEditMode && (
                                            <input
                                                type="checkbox"
                                                className="w-5 h-5 mr-4 flex-shrink-0"
                                                checked={selectedIds.has(p.id)}
                                                onChange={() => handleSelectProduct(p.id)}
                                            />
                                        )}
                                        <Link
                                            href={`/main/products/${p.id}`}
                                            className="flex items-center flex-grow"
                                        >
                                            <img
                                                src={p.imageThumbnailUrl}
                                                alt={p.name}
                                                className="w-24 h-24 object-cover rounded-md flex-shrink-0"
                                            />
                                            <div className="flex-grow ml-4">
                                                <p className="text-sm text-gray-500">{p.sellerName}</p>
                                                <h3 className="font-light hover:underline leading-tight">{p.name}</h3>
                                                <p className="font-light mt-1">{p.price.toLocaleString()}원</p>
                                            </div>
                                        </Link>
                                        <button
                                            onClick={(e) => handleDeleteOne(e, p.id)}
                                            className="absolute top-3 right-3 text-gray-400 hover:text-gray-600 text-2xl"
                                        >
                                            &times;
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        )}
                    </section>
                </main>
            </div>

            {/* 선택 삭제 버튼 */}
            {isEditMode && (
                <footer className="fixed bottom-0 left-0 right-0 border-t shadow bg-white/90 backdrop-blur-md">
                    <div className="max-w-xl lg:max-w-4xl mx-auto p-4">
                        <button
                            onClick={handleDeleteSelected}
                            disabled={selectedIds.size === 0}
                            className="w-full py-3 bg-black text-white font-light rounded-md disabled:bg-gray-300"
                        >
                            선택 삭제 ({selectedIds.size})
                        </button>
                    </div>
                </footer>
            )}
        </MyPageLayout>
    );
}
