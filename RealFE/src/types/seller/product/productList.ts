export interface ProductListItem {
    id: number;
    name: string;
    price: number;
    status: string;
    isActive: boolean;  // active -> isActive로 변경하여 다른 타입과 일관성 맞춤
    imageThumbnailUrl: string;
    categoryName: string;
    parentCategoryName: string | null;
    sellerName: string;
    stock: number;
}