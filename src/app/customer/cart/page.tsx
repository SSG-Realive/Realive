'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { CartItem } from '@/types/customer/cart/cart';
import {
    fetchCartList,
    updateCartItemQuantity,
    deleteCartItem,
} from '@/service/customer/cartService';
import { useCartStore } from '@/store/customer/useCartStore'; // ✨ 1. Cart 스토어 임포트
import CartItemCard from '@/components/customer/cart/CartItemCard';
import Navbar from '@/components/customer/common/Navbar';

export default function CartPage() {
    const [cartItems, setCartItems] = useState<CartItem[]>([]);
    const [loading, setLoading] = useState(true);
    const router = useRouter();

    // ✨ 2. 선택된 아이템의 ID를 관리하는 상태 추가
    const [selectedItemIds, setSelectedItemIds] = useState<Set<number>>(new Set());

    // ✨ 3. useCartStore에서 필요한 액션을 가져옴
    const setItemsForCheckout = useCartStore((state) => state.setItemsForCheckout);

    // 데이터 로딩 로직은 기존과 동일
    useEffect(() => {
        fetchCartList()
            .then(items => {
                setCartItems(items);
                // 기본적으로 모든 상품을 선택 상태로 시작
                setSelectedItemIds(new Set(items.map(item => item.cartItemId)));
            })
            .catch(() => alert('장바구니 불러오기 실패'))
            .finally(() => setLoading(false));
    }, []);
    
    // ✨ 4. 선택된 상품들의 총 가격 계산
    const totalPrice = cartItems
        .filter(item => selectedItemIds.has(item.cartItemId))
        .reduce((sum, item) => sum + item.productPrice * item.quantity, 0);

    // 수량 변경, 삭제 핸들러는 기존과 동일
    const handleQuantityChange = async (cartItemId: number, newQty: number) => {
        // ... 기존 로직 ...
    };

    const handleDelete = async (cartItemId: number) => {
        // ... 기존 로직 ...
        // 삭제 시, 선택된 아이템 목록에서도 제거
        setSelectedItemIds(prev => {
            const newSet = new Set(prev);
            newSet.delete(cartItemId);
            return newSet;
        });
    };
    
    // ✨ 5. 선택/해제를 위한 핸들러 함수들 추가
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

    // ✨ 6. 결제하기 버튼 로직 수정
    const handleCheckout = () => {
        const itemsToCheckout = cartItems.filter(item => selectedItemIds.has(item.cartItemId));

        if (itemsToCheckout.length === 0) {
            alert("결제할 상품을 선택해주세요.");
            return;
        }
        // 선택된 상품들을 전역 스토어에 저장
        setItemsForCheckout(itemsToCheckout);
        // 결제 페이지로 이동
        router.push('/customer/orders/new'); // customer 경로는 실제 프로젝트에 맞게 확인
    };

    if (loading) return <div className="p-10">로딩 중...</div>;

    return (
        <>
            <Navbar />
            <main className="max-w-4xl mx-auto p-6">
                <h1 className="text-2xl font-bold mb-6">🛒 장바구니</h1>

                {cartItems.length === 0 ? (
                    <p className="text-gray-500">장바구니가 비어있습니다.</p>
                ) : (
                    <>
                        {/* ✨ 전체 선택 체크박스 추가 */}
                        <div className="mb-4 border-b pb-4">
                            <label className="flex items-center space-x-3 cursor-pointer">
                                <input
                                    type="checkbox"
                                    className="h-5 w-5"
                                    onChange={handleSelectAll}
                                    checked={selectedItemIds.size === cartItems.length && cartItems.length > 0}
                                />
                                <span>전체선택 ({selectedItemIds.size}/{cartItems.length})</span>
                            </label>
                        </div>

                        <ul className="space-y-6">
                            {cartItems.map((item) => (
                                <CartItemCard
                                    key={item.cartItemId}
                                    item={item}
                                    onQuantityChange={handleQuantityChange}
                                    onDelete={handleDelete}
                                    // ✨ isSelected와 onToggleSelect props 전달
                                    isSelected={selectedItemIds.has(item.cartItemId)}
                                    onToggleSelect={handleToggleSelect}
                                />
                            ))}
                        </ul>

                        <div className="mt-8 text-right">
                            <p className="text-xl font-bold">
                                선택 상품 총 금액: {totalPrice.toLocaleString()}원
                            </p>
                            <button
                                onClick={handleCheckout} // ✨ 수정된 핸들러 연결
                                className="mt-4 px-6 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
                            >
                                선택 상품 결제하기
                            </button>
                        </div>
                    </>
                )}
            </main>
        </>
    );
}