export interface ProductListItem {
    id: number;
    name: string;
    price: number;
    status: string;
    isActive: boolean;
    imageThumbnailUrl: string;
    categoryName: string;
    parentCategoryName: string | null;
    sellerName: string;
}