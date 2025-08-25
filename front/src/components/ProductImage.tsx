'use client';

import { useState } from 'react';

interface ProductImageProps {
    src?: string | null;
    alt: string;
    className?: string;
}

export default function ProductImage({
                                         src,
                                         alt,
                                         className = '',
                                     }: ProductImageProps) {
    const fallback1 = '/images/placeholder.jpg';      // 1차 fallback
    const fallback2 = '/images/placeholder.png';          // 2차 fallback

    const [currentSrc, setCurrentSrc] = useState(src || fallback1);
    const [errorCount, setErrorCount] = useState(0);

    const handleError = () => {
        if (errorCount === 0) {
            setCurrentSrc(fallback1); // 1차 실패 → fallback1
            setErrorCount(1);
        } else if (errorCount === 1) {
            setCurrentSrc(fallback2); // 2차 실패 → fallback2
            setErrorCount(2);
        }
    };

    return (
        <img
            src={currentSrc}
            alt={alt}
            onError={handleError}
            className={`object-cover rounded-lg ${className}`}
        />
    );
}
