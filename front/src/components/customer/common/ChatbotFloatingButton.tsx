// src/components/ChatbotFloatingButton.tsx
'use client';

export default function ChatbotFloatingButton() {
    return (
        <button
            className="fixed bottom-6 right-6 bg-green-600 text-white px-4 py-2 rounded-full shadow-lg text-sm"
            onClick={() => {
                // 기능 없음, UI용
            }}
        >
            💬 AI 챗봇
        </button>
    );
}