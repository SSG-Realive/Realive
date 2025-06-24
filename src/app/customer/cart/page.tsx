'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { CartItem } from '@/types/customer/cart/cart';
import {
    fetchCartList,
    updateCartItemQuantity,
    deleteCartItem,
} from '@/service/customer/cartService';
import { useCartStore } from '@/store/customer/useCartStore';
import CartItemCard from '@/components/customer/cart/CartItemCard';
import Navbar from '@/components/customer/common/Navbar';

export default function CartPage() {
    const [cartItems, setCartItems] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [isEditMode, setIsEditMode] = useState(false);
    const [selectedItemIds, setSelectedItemIds] = useState<Set<number>>(new Set());
    const router = useRouter();

    const setItemsForCheckout = useCartStore((state) => state.setItemsForCheckout);

    useEffect(() => {
        fetchCartList()
            .then(items => {
                setCartItems(items);
                setSelectedItemIds(new Set(items.map(item => item.cartItemId)));
            })
            .catch(() => alert('장바구니 불러오기 실패'))
            .finally(() => setLoading(false));
    }, []);

    const totalPrice = cartItems
        .filter(item => selectedItemIds.has(item.cartItemId))
        .reduce((sum, item) => sum + item.productPrice * item.quantity, 0);

    const handleQuantityChange = async (cartItemId: number, newQty: number) => {
        if (newQty < 1) return;

        try {
            await updateCartItemQuantity({ cartItemId, quantity: newQty }); // service 수정 반영
            setCartItems((prevItems) =>
                prevItems.map((item) =>
                    item.cartItemId === cartItemId ? { ...item, quantity: newQty } : item
                )
            );
        } catch (error) {
            alert('수량 변경에 실패했습니다.');
        }
    };

    const handleDelete = async (cartItemId: number) => {
        if (!confirm('이 상품을 장바구니에서 삭제하시겠습니까?')) return;

        try {
            await deleteCartItem({ cartItemId }); // service 수정 반영
            setCartItems((prevItems) => prevItems.filter((item) => item.cartItemId !== cartItemId));
            setSelectedItemIds(prev => {
                const newSet = new Set(prev);
                newSet.delete(cartItemId);
                return newSet;
            });
            alert('상품이 장바구니에서 제거되었습니다.');
        } catch (error) {
            alert('상품 삭제에 실패했습니다.');
        }
    };

    const handleToggleSelect = (cartItemId: number) => {
        setSelectedItemIds(prev => {
            const newSet = new Set(prev);
            if (newSet.has(cartItemId)) {
                newSet.delete(cartItemId);
            } else {
                newSet.add(cartItemId);
            }
            return newSet;
        });
    };

    const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.checked) {
            setSelectedItemIds(new Set(cartItems.map(item => item.cartItemId)));
        } else {
            setSelectedItemIds(new Set());
        }
    };

    const handleDeleteSelected = async () => {
        if (selectedItemIds.size === 0) {
            alert('삭제할 상품을 선택해주세요.');
            return;
        }
        if (!confirm(`선택된 ${selectedItemIds.size}개의 상품을 장바구니에서 삭제하시겠습니까?`)) return;

        try {
            const deletePromises = Array.from(selectedItemIds).map(id => deleteCartItem({ cartItemId: id })); // service 수정 반영
            await Promise.all(deletePromises);

            setCartItems((prev) => prev.filter((item) => !selectedItemIds.has(item.cartItemId)));
            setSelectedItemIds(new Set());
            alert(`${selectedItemIds.size}개의 상품이 삭제되었습니다.`);
            setIsEditMode(false);
        } catch (err) {
            alert('선택 삭제 중 오류가 발생했습니다.');
        }
    };

    // ✨ 장바구니 비우기 핸들러 추가
    const handleClearCart = async () => {
        if (!confirm('장바구니의 모든 상품을 삭제하시겠습니까?')) return;

        try {
            // 모든 cartItem.cartItemId를 가져와서 일괄 삭제
            const allCartItemIds = cartItems.map(item => item.cartItemId);
            const deletePromises = allCartItemIds.map(id => deleteCartItem({ cartItemId: id }));
            await Promise.all(deletePromises);

            setCartItems([]); // 장바구니 비우기
            setSelectedItemIds(new Set()); // 선택 초기화
            alert('장바구니의 모든 상품이 삭제되었습니다.');
            setIsEditMode(false); // 편집 모드 해제
        } catch (err) {
            alert('장바구니 비우기 중 오류가 발생했습니다.');
        }
    };

    const handleCheckout = () => {
        const itemsToCheckout = cartItems.filter(item => selectedItemIds.has(item.cartItemId));

        if (itemsToCheckout.length === 0) {
            alert("결제할 상품을 선택해주세요.");
            return;
        }
        setItemsForCheckout(itemsToCheckout);
        router.push('/customer/orders/new');
    };

    if (loading) return <div className="p-10">로딩 중...</div>;

    return (
        <>
            <Navbar />
            <main className="max-w-4xl mx-auto p-6">
                <div className="flex justify-between items-center mb-6">
                    <h1 className="text-2xl font-bold">🛒 장바구니</h1>
                    {cartItems.length > 0 && (
                        <div className="flex gap-2">
                            {/* ✨ 장바구니 비우기 버튼 추가 (편집 모드와 무관하게 항상 보일 수 있음) */}
                            <button onClick={handleClearCart} className="text-sm text-red-600 px-3 py-1 border border-red-600 rounded hover:bg-red-50">
                                장바구니 비우기
                            </button>
                            <button onClick={() => setIsEditMode(prev => !prev)} className="text-sm text-gray-600 px-3 py-1 border rounded hover:bg-gray-100">
                                {isEditMode ? '편집 취소' : '상품 편집'}
                            </button>
                        </div>
                    )}
                </div>

                {cartItems.length === 0 ? (
                    <p className="text-gray-500 text-center py-20">장바구니가 비어있습니다.</p>
                ) : (
                    <>
                        <div className="mb-4 border-b pb-4">
                            <label className="flex items-center space-x-3 cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="h-5 w-5"
                                    onChange={handleSelectAll}
                                    checked={selectedItemIds.size > 0 && selectedItemIds.size === cartItems.length}
                                />
                                <span>전체선택 ({selectedItemIds.size}/{cartItems.length})</span>
                            </label>
                        </div>

                        <ul className="space-y-6"> {/* ✨ 각 리스트 아이템 사이 간격 증가 */}
                            {cartItems.map((item) => (
                                <CartItemCard
                                    key={item.cartItemId}
                                    item={item}
                                    onQuantityChange={handleQuantityChange}
                                    onDelete={handleDelete}
                                    isSelected={selectedItemIds.has(item.cartItemId)}
                                    onToggleSelect={handleToggleSelect}
                                />
                            ))}
                        </ul>
                    </>
                )}
            </main>

            {(cartItems.length > 0) && (
                <footer className="fixed bottom-0 left-0 right-0 bg-white border-t shadow-lg">
                    <div className="max-w-4xl mx-auto p-4 flex justify-between items-center">
                        <div className="text-lg font-bold">
                            총 금액: {totalPrice.toLocaleString()}원
                        </div>
                        <div className="flex gap-2">
                            {isEditMode && (
                                <button
                                    onClick={handleDeleteSelected}
                                    disabled={selectedItemIds.size === 0}
                                    className="py-3 px-4 bg-red-500 text-white font-bold rounded-md disabled:bg-gray-300"
                                >
                                    선택 상품 삭제 ({selectedItemIds.size})
                                </button>
                            )}
                            <button
                                onClick={handleCheckout}
                                disabled={selectedItemIds.size === 0}
                                className="py-3 px-6 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-300"
                            >
                                선택 상품 결제하기
                            </button>
                        </div>
                    </div>
                </footer>
            )}
        </>
    );
}