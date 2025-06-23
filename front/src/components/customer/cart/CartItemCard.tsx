'use client';

import { CartItem } from '@/types/customer/cart/cart';
// import Image from 'next/image'; // 현재 next/image를 사용하지 않으므로 주석 처리하거나 제거할 수 있습니다.

// ✨ 1. Props 인터페이스에 isSelected와 onToggleSelect 추가
interface Props {
    item: CartItem;
    onQuantityChange: (cartItemId: number, newQty: number) => void;
    onDelete: (cartItemId: number) => void;
    isSelected: boolean;
    onToggleSelect: (cartItemId: number) => void;
}

// ✨ 2. 컴포넌트가 새로운 props를 받도록 수정
export default function CartItemCard({ item, onQuantityChange, onDelete, isSelected, onToggleSelect }: Props) {
    return (
        <li className="flex items-center justify-between border-b pb-4">
            <div className="flex items-center space-x-4">

                {/* ✨ 3. 체크박스 UI 추가 */}
                <input
                    type="checkbox"
                    className="h-5 w-5 flex-shrink-0" // 스타일은 필요에 맞게 조절
                    checked={isSelected}
                    onChange={() => onToggleSelect(item.cartItemId)}
                />

                <img
                    src={item.imageThumbnailUrl}
                    alt={item.productName}
                    className="w-24 h-24 object-cover rounded"
                />
                <div>
                    <p className="font-semibold">{item.productName}</p>
                    <p className="text-green-600 font-bold">
                        {(item.productPrice * item.quantity).toLocaleString()}원
                    </p>
                    <div className="flex items-center mt-2 space-x-2">
                        <button
                            onClick={() => onQuantityChange(item.cartItemId, item.quantity - 1)}
                            className="px-2 border rounded"
                        >
                            -
                        </button>
                        <span>{item.quantity}</span>
                        <button
                            onClick={() => onQuantityChange(item.cartItemId, item.quantity + 1)}
                            className="px-2 border rounded"
                        >
                            +
                        </button>
                    </div>
                </div>
            </div>
            <button
                onClick={() => onDelete(item.cartItemId)}
                className="text-red-500 hover:underline"
            >
                삭제
            </button>
        </li>
    );
}