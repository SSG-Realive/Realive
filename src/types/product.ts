// src/types/product.ts

export interface DeliveryPolicy {
    type: '무료배송' | '유료배송';
    cost: number;
    regionLimit: string;
}

export interface ProductDetail {
    id: number;
    name: string;
    description: string;
    price: number;
    stock: number;
    width: number;
    depth: number;
    height: number;
    status: string;
    isActive: boolean;
    categoryId: number;
    deliveryPolicy: DeliveryPolicy;
}

