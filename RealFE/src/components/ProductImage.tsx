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
    const fallback1 = '/images/placeholder.jpg';
    const fallback2 = '/images/placeholder.png';

    const [currentSrc, setCurrentSrc] = useState(src || fallback1);
    const [errorCount, setErrorCount] = useState(0);

    const handleError = () => {
        if (errorCount === 0) {
            setCurrentSrc(fallback1);
            setErrorCount(1);
        } else if (errorCount === 1) {
            setCurrentSrc(fallback2);
            setErrorCount(2);
        }
    };

    return (
        <div className={`w-full h-full overflow-hidden flex items-center justify-center bg-white ${className}`}>
            <img
                src={currentSrc}
                alt={alt}
                onError={handleError}
                loading="lazy"
                className="w-full h-full object-cover"
                decoding="async"
                sizes="(max-width: 768px) 50vw, 25vw" // 모바일에서는 50%, PC에서는 25%
            />
        </div>
    );
}
