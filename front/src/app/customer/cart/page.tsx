'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { CartItem } from '@/types/customer/cart/cart';
import {
    fetchCartList,
    updateCartItemQuantity,
    deleteCartItem,
} from '@/service/customer/cartService';
import CartItemCard from '@/components/customer/CartItemCard';
import Navbar from '@/components/customer/Navbar'; // ✅ 추가

export default function CartPage() {
    const [cartItems, setCartItems] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    const totalPrice = cartItems.reduce(
        (sum, item) => sum + item.productPrice * item.quantity,
        0
    );

    useEffect(() => {
        fetchCartList()
            .then(setCartItems)
            .catch(() => alert('장바구니 불러오기 실패'))
            .finally(() => setLoading(false));
    }, []);

    const handleQuantityChange = async (cartItemId: number, newQty: number) => {
        if (newQty < 1) return;
        try {
            await updateCartItemQuantity(cartItemId, newQty);
            setCartItems((prev) =>
                prev.map((item) =>
                    item.cartItemId === cartItemId ? { ...item, quantity: newQty } : item
                )
            );
        } catch {
            alert('수량 변경 실패');
        }
    };

    const handleDelete = async (cartItemId: number) => {
        try {
            await deleteCartItem(cartItemId);
            setCartItems((prev) =>
                prev.filter((item) => item.cartItemId !== cartItemId)
            );
        } catch {
            alert('삭제 실패');
        }
    };

    if (loading) return <div className="p-10">로딩 중...</div>;

    return (
        <>
            <Navbar /> {/* ✅ 네비게이션 바 추가 */}

            <div className="max-w-4xl mx-auto p-6">
                <h1 className="text-2xl font-bold mb-6">🛒 장바구니</h1>

                {cartItems.length === 0 ? (
                    <p className="text-gray-500">장바구니가 비어있습니다.</p>
                ) : (
                    <>
                        <ul className="space-y-6">
                            {cartItems.map((item) => (
                                <CartItemCard
                                    key={item.cartItemId}
                                    item={item}
                                    onQuantityChange={handleQuantityChange}
                                    onDelete={handleDelete}
                                />
                            ))}
                        </ul>

                        <div className="mt-8 text-right">
                            <p className="text-xl font-bold">
                                총 결제 금액: {totalPrice.toLocaleString()}원
                            </p>
                            <button
                                onClick={() => router.push('/orders/new')}
                                className="mt-4 px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                            >
                                결제하기
                            </button>
                        </div>
                    </>
                )}
            </div>
        </>
    );
}
