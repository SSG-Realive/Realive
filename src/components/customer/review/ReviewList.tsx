    'use client';

    import { ReviewResponseDTO } from '@/types/customer/review/review';

    interface ReviewListProps {
        reviews: ReviewResponseDTO[];
    }

    export default function ReviewList({ reviews }: ReviewListProps) {
        if (!reviews || reviews.length === 0) {
            return <p className="text-gray-500">등록된 리뷰가 없습니다.</p>;
        }

        return (
            <div className="space-y-4">
                {reviews.map((review) => (
                    <div key={review.reviewId} className="border p-4 rounded shadow-sm">
                        <div className="flex justify-between items-center">
                            <p className="font-semibold text-sm">{review.productName}</p>
                            <span className="text-yellow-500 text-sm">⭐ {review.rating}</span>
                        </div>
                        <p className="text-sm mt-2 whitespace-pre-line">{review.content}</p>
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
