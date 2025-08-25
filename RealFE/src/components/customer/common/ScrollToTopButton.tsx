'use client';

import { useEffect, useState } from 'react';
import { FaChevronUp } from 'react-icons/fa';

export default function ScrollToTopButton() {
    const [isVisible, setIsVisible] = useState(false);

    useEffect(() => {
        const toggleVisibility = () => {
            setIsVisible(window.scrollY > 300);
        };

        window.addEventListener('scroll', toggleVisibility);
        return () => window.removeEventListener('scroll', toggleVisibility);
    }, []);

    const scrollToTop = () => {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    };

    return isVisible ? (
        <button
            onClick={scrollToTop}
            className="fixed bottom-6 left-1/2 transform -translate-x-1/2 z-50 w-8 h-8 flex items-center justify-center rounded-full bg-black text-white shadow-md hover:bg-gray-800 transition"
            aria-label="Scroll to top"
        >
            <FaChevronUp size={12} />
        </button>
    ) : null;
}
