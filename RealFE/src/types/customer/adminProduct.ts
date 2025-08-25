// types/customer/adminProduct.ts
export interface AdminProduct {
    productName: string | null;
    imageUrl?: string | null;
    imageThumbnailUrl?: string | null;
    imageUrls?: string[]; // 서버에서 빈 배열일 수 있으므로 optional로
}