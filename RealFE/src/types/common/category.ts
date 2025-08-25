// src/types/common/category.ts
export interface Category {
    id: number;
    name: string;
    parentId: number | null;
}