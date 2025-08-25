// types/customer/auctions.ts (기존 파일에 추가 또는 수정)

// ✅ 1. Spring의 Page 객체 전체를 정확하게 표현하는 제네릭 타입
//    (기존 PaginatedAuctionResponse를 대체하거나 업그레이드)
export interface SpringPage<T> {
  content: T[];
  pageable: object; // 보통 이 안의 상세 정보는 잘 쓰지 않으므로 object로 두어도 무방
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // 현재 페이지 번호 (0부터 시작)
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}

// ✅ 2. 백엔드의 ApiResponse 클래스와 일치하는 제네릭 타입
//    (기존 ApiResponse를 이 형태로 수정하는 것을 추천)
export interface BackendApiResponse<T> {
    success: boolean;
    message: string;
    data: T | null;
    error?: { // 에러가 있을 경우에만 포함될 수 있음
      code: string;
      message: string;
    } | null;
}