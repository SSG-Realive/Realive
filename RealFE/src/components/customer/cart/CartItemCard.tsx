'use client';

import { CartItem } from '@/types/customer/cart/cart';
import Link from 'next/link';

interface Props {
    item: CartItem;
    onQuantityChange: (cartItemId: number, newQty: number) => void;
    onDelete: (cartItemId: number) => void;
    isSelected: boolean;
    onToggleSelect: (cartItemId: number) => void;
}

export default function CartItemCard({
                                         item,
                                         onQuantityChange,
                                         onDelete,
                                         isSelected,
                                         onToggleSelect,
                                     }: Props) {
    return (
        <li className="flex items-center bg-white rounded-lg shadow-sm p-4 relative">
            <input
                type="checkbox"
                className="w-5 h-5 mr-4 flex-shrink-0 accent-black"
                checked={isSelected}
                onChange={() => onToggleSelect(item.cartItemId)}
            />

            <Link href={`/main/products/${item.productId}`} className="flex items-center flex-grow">
                <img
                    src={item.imageThumbnailUrl}
                    alt={item.productName}
                    className="w-24 h-24 object-cover rounded-md flex-shrink-0"
                />
                <div className="flex-grow ml-4">
                    {/* 상품명 */}
                    <h3 className="text-base font-light text-gray-800 hover:underline leading-tight">
                        {item.productName}
                    </h3>

                    {/* 가격 */}
                    <p className="text-sm text-gray-600 mt-1">
                        {item.productPrice.toLocaleString()}
                        <span className="ml-1">원</span>
                    </p>

                    {/* 수량 조절 */}
                    <div className="flex items-center mt-2 gap-2">
                        <button
                            onClick={(e) => {
                                e.preventDefault();
                                onQuantityChange(item.cartItemId, item.quantity - 1);
                            }}
                            className="w-6 h-6 text-sm flex items-center justify-center border rounded hover:bg-gray-100"
                        >
                            -
                        </button>
                        <span className="w-6 text-center text-sm text-gray-500">{item.quantity}</span>
                        <button
                            onClick={(e) => {
                                e.preventDefault();
                                onQuantityChange(item.cartItemId, item.quantity + 1);
                            }}
                            className="w-6 h-6 text-sm flex items-center justify-center border rounded hover:bg-gray-100"
                        >
                            +
                        </button>
                    </div>
                </div>
            </Link>

            <button
                onClick={(e) => {
                    e.stopPropagation();
                    onDelete(item.cartItemId);
                }}
                className="absolute top-3 right-3 text-gray-400 hover:text-gray-600 text-2xl"
            >
                &times;
            </button>
        </li>
    );
}
