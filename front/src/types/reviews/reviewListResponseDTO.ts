import {ReviewResponseDTO} from "@/types/reviews/reviewResponseDTO";

export interface ReviewListResponseDTO {
    reviews: ReviewResponseDTO[];
    totalCount: number;
    page: number;
    size: number;
}