'use client';

import { BsChatDotsFill } from 'react-icons/bs';

export default function ChatbotFloatingButton() {
    return (
        <button
            className="fixed bottom-6 right-0 translate-x-[-1.5rem] z-[9999] w-12 h-12 flex items-center justify-center
                       rounded-full bg-gray-200 text-gray-700 shadow-md hover:shadow-lg
                       hover:bg-gray-300 transition-all duration-200"
            onClick={() => {
                // TODO: 챗봇 팝업 또는 이동
            }}
        >
            <BsChatDotsFill className="text-xl" />
        </button>
    );
}
