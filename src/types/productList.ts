export interface ProductListItem {
    id: number;
    name: string;
    price: number;
    status: string;
    active: boolean;
    imageThumbnailUrl: string;
    categoryName: string;
    parentCategoryName: string | null;
    sellerName: string;
}