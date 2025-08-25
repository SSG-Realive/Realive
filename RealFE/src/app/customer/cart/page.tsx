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
import { useAuthStore } from '@/store/customer/authStore';
import CartItemCard from '@/components/customer/cart/CartItemCard';
import useDialog from '@/hooks/useDialog';
import GlobalDialog from '@/components/ui/GlobalDialog';
import useConfirm from '@/hooks/useConfirm';
import { FiTrash2 } from 'react-icons/fi';

export default function CartPage() {
    const [cartItems, setCartItems] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [isEditMode, setIsEditMode] = useState(false);
    const [selectedItemIds, setSelectedItemIds] = useState<Set<number>>(new Set());
    const router = useRouter();
    const { open, message, handleClose, show } = useDialog();
    const { confirm, dialog } = useConfirm();
    

    const { hydrated, isAuthenticated } = useAuthStore();
    const setItemsForCheckout = useCartStore((state) => state.setItemsForCheckout);

    useEffect(() => {
        if (!hydrated) return;
        if (!isAuthenticated()) {
            show('로그인이 필요합니다.');
            router.push('/login');
            return;
        }

        fetchCartList()
            .then((items) => {
                setCartItems(items);
                setSelectedItemIds(new Set(items.map((item) => item.cartItemId)));
            })
            .catch(() => show('장바구니 불러오기 실패'))
            .finally(() => setLoading(false));
    }, [hydrated, isAuthenticated, router]);

    const totalPrice = cartItems
        .filter((item) => selectedItemIds.has(item.cartItemId))
        .reduce((sum, item) => sum + item.productPrice * item.quantity, 0);

    const handleQuantityChange = async (cartItemId: number, newQty: number) => {
        if (newQty < 1) return;
        try {
            await updateCartItemQuantity({ cartItemId, quantity: newQty });
            setCartItems((prev) =>
                prev.map((item) =>
                    item.cartItemId === cartItemId ? { ...item, quantity: newQty } : item
                )
            );
        } catch {
            show('수량 변경에 실패했습니다.');
        }
    };

    const handleDelete = async (cartItemId: number) => {
        if (!(await confirm('이 상품을 장바구니에서 삭제하시겠습니까?'))) return;
        try {
            await deleteCartItem({ cartItemId });
            setCartItems((prev) => prev.filter((item) => item.cartItemId !== cartItemId));
            setSelectedItemIds((prev) => {
                const newSet = new Set(prev);
                newSet.delete(cartItemId);
                return newSet;
            });
            show('상품이 장바구니에서 제거되었습니다.');
        } catch {
            show('상품 삭제에 실패했습니다.');
        }
    };

    const handleToggleSelect = (cartItemId: number) => {
        setSelectedItemIds((prev) => {
            const newSet = new Set(prev);
            if (newSet.has(cartItemId)) newSet.delete(cartItemId);
            else newSet.add(cartItemId);
            return newSet;
        });
    };

    const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.checked) {
            setSelectedItemIds(new Set(cartItems.map((item) => item.cartItemId)));
        } else {
            setSelectedItemIds(new Set());
        }
    };

    const handleDeleteSelected = async () => {
        if (selectedItemIds.size === 0) return show('삭제할 상품을 선택해주세요.');
        if (!(await confirm(`선택된 ${selectedItemIds.size}개의 상품을 삭제하시겠습니까?`))) return;

        try {
            await Promise.all(
                Array.from(selectedItemIds).map((id) => deleteCartItem({ cartItemId: id }))
            );
            setCartItems((prev) => prev.filter((item) => !selectedItemIds.has(item.cartItemId)));
            setSelectedItemIds(new Set());
            show(`${selectedItemIds.size}개의 상품이 삭제되었습니다.`);
            setIsEditMode(false);
        } catch {
            show('선택 삭제 중 오류가 발생했습니다.');
        }
    };

    const handleClearCart = async () => {
        if (!(await confirm('장바구니의 모든 상품을 삭제하시겠습니까?'))) return;

        try {
            await Promise.all(cartItems.map((item) => deleteCartItem({ cartItemId: item.cartItemId })));
            setCartItems([]);
            setSelectedItemIds(new Set());
            show('장바구니의 모든 상품이 삭제되었습니다.');
            setIsEditMode(false);
        } catch {
            show('장바구니 비우기 중 오류가 발생했습니다.');
        }
    };

    const handleCheckout = () => {
        const itemsToCheckout = cartItems.filter((item) => selectedItemIds.has(item.cartItemId));
        if (itemsToCheckout.length === 0) return show('결제할 상품을 선택해주세요.');
        setItemsForCheckout(itemsToCheckout);
        router.push('/customer/mypage/orders/new');
    };

    if (!hydrated) {
        return <div className="flex justify-center items-center h-screen">인증 정보를 확인하는 중...</div>;
    }

    if (loading) return <div className="p-10">장바구니를 불러오는 중...</div>;

    return (
        <>
            {dialog}
            <GlobalDialog open={open} message={message} onClose={handleClose} />

            <main className="relative max-w-4xl mx-auto p-6 pb-40">
                {/* 선택/편집/삭제 상단 바 */}
                {cartItems.length > 0 && (
                    <div className="flex items-center justify-between mb-4 pt-10 pb-4">
                        <label className="flex items-center space-x-3 cursor-pointer">
                            <input
                                type="checkbox"
                                className="h-5 w-5 accent-black"
                                onChange={handleSelectAll}
                                checked={
                                    selectedItemIds.size > 0 &&
                                    selectedItemIds.size === cartItems.length
                                }
                            />
                            <span className="text-sm text-gray-800">
              전체선택 ({selectedItemIds.size}/{cartItems.length})
            </span>
                        </label>

                        <div className="flex flex-wrap items-center justify-end gap-1 sm:gap-2 max-w-full">
                            {isEditMode && (
                                <button
                                    onClick={handleDeleteSelected}
                                    disabled={selectedItemIds.size === 0}
                                    className="py-1 px-2 flex items-center bg-red-500 text-white text-xs rounded-md disabled:bg-gray-300"
                                    title="선택 삭제"
                                >
                                    <FiTrash2 className="text-sm" />
                                </button>
                            )}
                            <button
                                onClick={handleClearCart}
                                className="text-xs text-red-600 border border-red-300 px-2 py-1 hover:bg-red-50 rounded-md whitespace-nowrap"
                            >
                                전체 삭제
                            </button>
                            <button
                                onClick={() => setIsEditMode((prev) => !prev)}
                                className="text-xs text-gray-600 border border-gray-300 px-2 py-1 hover:bg-gray-100 rounded-md whitespace-nowrap"
                            >
                                편집
                            </button>
                        </div>
                    </div>
                )}

                {/* 장바구니 항목 */}
                {cartItems.length === 0 ? (
                    <p className="text-gray-500 text-center py-20">장바구니가 비어있습니다.</p>
                ) : (
                    <ul className="space-y-6">
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
                )}
            </main>

            {/* 결제 버튼 하단 고정 */}
            {cartItems.length > 0 && (
                <div className="fixed bottom-0 left-0 right-0 bg-white z-10">
                    <div className="max-w-4xl mx-auto px-4 py-4">
                        <button
                            onClick={handleCheckout}
                            disabled={selectedItemIds.size === 0}
                            className="w-full py-3 px-8 bg-black text-white text-sm rounded hover:bg-gray-800 disabled:bg-gray-300"
                        >
                            {totalPrice.toLocaleString()}원 ({selectedItemIds.size}) 결제
                        </button>
                    </div>
                </div>
            )}
        </>
    );
}


