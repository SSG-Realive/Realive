'use client';

import { useEffect, useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { fetchWishlist, toggleWishlist } from '@/service/customer/wishlistService';
import { ProductListDTO } from '@/types/seller/product/product';
import Navbar from '@/components/customer/common/Navbar';

export default function WishlistPage() {
    const [products, setProducts] = useState<ProductListDTO[]>([]);
    const [loading, setLoading] = useState(true);
    const [isEditMode, setIsEditMode] = useState(false);
    const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());
    const router = useRouter();


    useEffect(() => {
        fetchWishlist()
            .then(setProducts)
            .catch(() => alert('찜 목록을 불러오는데 실패했습니다.'))
            .finally(() => setLoading(false));
    }, []);

    const toggleEditMode = () => {
        setIsEditMode((prev) => !prev);
        setSelectedIds(new Set());
    };

    const handleSelectProduct = (productId: number) => {
        setSelectedIds((prevSelectedIds) => {
            const newSelectedIds = new Set(prevSelectedIds);
            if (newSelectedIds.has(productId)) {
                newSelectedIds.delete(productId);
            } else {
                newSelectedIds.add(productId);
            }
            return newSelectedIds;
        });
    };
    
    const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.checked) {
            setSelectedIds(new Set(products.map((p) => p.id)));
        } else {
            setSelectedIds(new Set());
        }
    };

    // ✅ 실수 수정: preventDefault 대신 stopPropagation 사용
    const handleDeleteOne = async (e: React.MouseEvent, productId: number) => {
        // 클릭 이벤트가 Link 태그로 전파되는 것을 막습니다.
        e.stopPropagation(); 
        
        if (!confirm('이 상품을 찜 목록에서 삭제하시겠습니까?')) return;
        
        try {
            await toggleWishlist({ productId });
            setProducts((prev) => prev.filter((p) => p.id !== productId));
            alert('찜 목록에서 제거되었습니다.');
        } catch (err) {
            alert('삭제 중 오류가 발생했습니다.');
        }
    };
    
    const handleDeleteSelected = async () => {
        if (selectedIds.size === 0) {
            alert('삭제할 상품을 선택해주세요.');
            return;
        }
        if (!confirm(`선택된 ${selectedIds.size}개의 상품을 찜 목록에서 삭제하시겠습니까?`)) return;

        try {
            const deletePromises = Array.from(selectedIds).map(id => toggleWishlist({ productId: id }));
            await Promise.all(deletePromises);
            
            setProducts((prev) => prev.filter((p) => !selectedIds.has(p.id)));
            alert(`${selectedIds.size}개의 상품이 삭제되었습니다.`);
            
            toggleEditMode();
        } catch (err) {
            alert('선택 삭제 중 오류가 발생했습니다.');
        }
    };


    if (loading) return <div>로딩 중...</div>;
    
    return (
        <>
            <Navbar />
            <main className="max-w-xl lg:max-w-4xl mx-auto px-4 py-8">
                <div className="flex justify-between items-center mb-6">
                    <h1 className="text-xl font-bold">찜한 상품</h1>
                    {products.length > 0 && (
                        <button onClick={toggleEditMode} className="text-sm text-gray-600">
                            {isEditMode ? '편집 취소' : '상품 편집'}
                        </button>
                    )}
                </div>

                {isEditMode && products.length > 0 && (
                     <div className="flex items-center mb-4 pb-2 border-b">
                        <input
                            type="checkbox"
                            className="w-5 h-5"
                            onChange={handleSelectAll}
                            checked={selectedIds.size > 0 && selectedIds.size === products.length}
                        />
                        <label className="ml-2 text-sm">
                            전체선택 ({selectedIds.size}/{products.length})
                        </label>
                    </div>
                )}

                {products.length === 0 ? (
                    <div className="text-center py-20 text-gray-500">찜한 상품이 없습니다.</div>
                ) : (
                    <ul className="space-y-6">
                        {products.map((p) => (
                            <li key={p.id} className="flex items-center bg-white rounded-lg shadow-sm p-4 relative">
                                {isEditMode && (
                                    <input
                                        type="checkbox"
                                        className="w-5 h-5 mr-4 flex-shrink-0"
                                        checked={selectedIds.has(p.id)}
                                        onChange={() => handleSelectProduct(p.id)}
                                    />
                                )}
                                <Link href={`/main/products/${p.id}`} className="flex items-center flex-grow">
                                    <img
                                        src={p.imageThumbnailUrl}
                                        alt={p.name}
                                        className="w-24 h-24 object-cover rounded-md flex-shrink-0"
                                    />
                                    <div className="flex-grow ml-4">
                                        <p className="text-sm text-gray-500">{p.sellerName}</p>
                                        <h3 className="font-medium hover:underline leading-tight">{p.name}</h3>
                                        <p className="font-bold mt-1">{p.price.toLocaleString()}원</p>
                                    </div>
                                </Link>
                                <button onClick={(e) => handleDeleteOne(e, p.id)} className="absolute top-3 right-3 text-gray-400 hover:text-gray-600 text-2xl">
                                    &times;
                                </button>
                            </li>
                        ))}
                    </ul>
                )}
            </main>

            {isEditMode && (
                <footer className="fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg">
                    <div className="max-w-xl lg:max-w-4xl mx-auto p-4">
                        <button
                            onClick={handleDeleteSelected}
                            disabled={selectedIds.size === 0}
                            className="w-full py-3 bg-red-500 text-white font-bold rounded-md disabled:bg-gray-300"
                        >
                            선택 삭제 ({selectedIds.size})
                        </button>
                    </div>
                </footer>
            )}
        </>
    );
}