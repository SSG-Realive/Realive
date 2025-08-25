'use client';

import { ReviewResponseDTO } from '@/types/customer/review/review';
import {
    getTrafficLightEmoji,
    getTrafficLightText,
    getTrafficLightBgClass
} from '@/types/admin/review';

interface ReviewListProps {
    reviews: ReviewResponseDTO[];
}

export default function ReviewList({ reviews }: ReviewListProps) {
    if (!reviews || reviews.length === 0) {
        return <p className="text-sm text-gray-500">등록된 리뷰가 없습니다.</p>;
    }

    return (
        <div className="space-y-6">
            {reviews.map((review) => (
                <div key={review.reviewId} className="p-2">
                    <div className="flex justify-between items-center">
                        <p className="text-sm font-semibold text-black">{review.productName}</p>
                        <div
                            className={`flex items-center gap-1 px-3 py-1 rounded-full border ${getTrafficLightBgClass(
                                review.rating
                            )} bg-white`}
                        >
                            <span className="text-base">{getTrafficLightEmoji(review.rating)}</span>
                            <span className="text-xs text-gray-700">{getTrafficLightText(review.rating)}</span>
                        </div>
                    </div>

                    <p className="text-sm text-gray-700 mt-2 whitespace-pre-line">{review.content}</p>

                    {review.imageUrls.length > 0 && (
                        <div className="flex gap-2 mt-2">
                            {review.imageUrls.map((url, idx) => (
                                <img
                                    key={idx}
                                    src={url}
                                    alt="리뷰 이미지"
                                    className="w-20 h-20 object-cover rounded"
                                />
                            ))}
                        </div>
                    )}

                    <p className="text-xs text-gray-400 mt-2">{review.createdAt}</p>
                </div>
            ))}
        </div>
    );
}
