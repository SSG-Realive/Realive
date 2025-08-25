// src/components/common/ImageWithFallback.tsx
"use client";

import React from 'react';

interface ImageWithFallbackProps {
    src: string | undefined | null; // 이미지 URL
    alt: string;                    // 대체 텍스트
    className?: string;             // Tailwind CSS 클래스 등
    fallbackSrc?: string;           // 에러 발생 시 보여줄 대체 이미지 URL (기본값 설정 가능)
    width?: number;                 // 이미지 너비 (Next.js Image 컴포넌트와 유사)
    height?: number;                // 이미지 높이
    [key: string]: any;             // 기타 HTML img 속성 (object-cover 등)
}

/**
 * 이미지 로딩 실패 시 대체 이미지를 표시하는 클라이언트 컴포넌트
 */
export default function ImageWithFallback({
                                              src,
                                              alt,
                                              className,
                                              fallbackSrc = '/images/placeholder.jpg', // 기본 대체 이미지 경로
                                              ...props
                                          }: ImageWithFallbackProps) {
    const handleError = (e: React.SyntheticEvent<HTMLImageElement, Event>) => {
        // 이미 fallbackSrc로 변경되었다면 더 이상 에러 처리하지 않음 (무한 루프 방지)
        if (e.currentTarget.src === fallbackSrc) {
            return;
        }
        e.currentTarget.onerror = null; // 중복 호출 방지
        e.currentTarget.src = fallbackSrc; // 에러 시 대체 이미지로 변경
    };

    return (
        <img
            src={src || fallbackSrc} // src가 null/undefined일 경우 바로 fallbackSrc 사용
            alt={alt}
            className={className}
            onError={handleError} // ⭐ 여기서 onError 이벤트 핸들러 사용 ⭐
            {...props}
        />
    );
}