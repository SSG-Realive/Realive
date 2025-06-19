// src/types/seller/product/product.ts

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
    width?: number;
    depth?: number;
    height?: number;
    status: string;
    isActive: boolean;
    imageThumbnailUrl: string;
    videoThumbnailUrl?: string;
    categoryName?: string;
    categoryId?: number;
    parentCategoryId?: number;
    seller?: string;
}

export interface ProductListDTO {
    id: number;
    name: string;
    price: number;
    status: string;
    isActive: boolean;
    imageThumbnailUrl: string;
    parentCategoryName: string | null;
    categoryName: string;
    sellerName: string;
    sellerId: number;
    stock: number;
    isWished: boolean;
}
