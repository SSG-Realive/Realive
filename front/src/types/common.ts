export interface PageResponse<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    currentPage: number;
    size: number;
}