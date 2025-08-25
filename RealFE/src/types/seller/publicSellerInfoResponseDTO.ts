

export interface publicSellerInfoResponseDTO {
    id: number;                // 판매자 ID (Java의 Long -> TypeScript의 number)
    name: string;              // 판매자의 공개 이름 또는 업체명 (Java의 String -> TypeScript의 string)
    profileImageUrl?: string;  // 판매자 프로필 이미지 URL (필수 아님을 나타내기 위해 '?' 추가)
    isApproved?: boolean;      // 승인 여부 (필수 아님을 나타내기 위해 '?' 추가)
    averageRating: number;     // 판매자의 평균 평점 (Java의 double -> TypeScript의 number)
    totalReviews: number;      // 총 리뷰 수 (Java의 Long -> TypeScript의 number)
    createdAt: string;         // 판매자 가입일 (Java의 LocalDateTime -> ISO 8601 문자열로 가정)
    contactNumber?: string;    // 판매자의 공개 연락처 (필수 아님을 나타내기 위해 '?' 추가)
    businessNumber?: string;   // 판매자의 사업자 등록 번호 (필수 아님을 나타내기 위해 '?' 추가)
}