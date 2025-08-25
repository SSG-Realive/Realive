
export interface SpringPage<T> {
    content: T[];           // 실제 데이터 목록
    totalElements: number;  // 전체 요소 수
    totalPages: number;     // 전체 페이지 수
    number: number;         // 현재 페이지 번호 (0-based)
    size: number;           // 페이지 크기
    first: boolean;
    last: boolean;
    empty: boolean;
    numberOfElements: number;
    pageable: {
        pageNumber: number;
        pageSize: number;
        sort: {
            empty: boolean;
            sorted: boolean;
            unsorted: boolean;
        };
        offset: number;
        paged: boolean;
        unpaged: boolean;
    };
    sort: {
        empty: boolean;
        sorted: boolean;
        unsorted: boolean;
    };
}