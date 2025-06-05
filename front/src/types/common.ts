export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    page: number;
    size: number;
    hasNext: boolean;
}