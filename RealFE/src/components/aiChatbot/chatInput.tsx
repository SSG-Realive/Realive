'use client';

import { useEffect, useState } from 'react';
import { useAuthStore } from '@/store/customer/authStore';

export default function ChatInput() {
    const { isAuthenticated, hydrated } = useAuthStore();
    const [inputValue, setInputValue] = useState('');

    const loggedIn = isAuthenticated();

    useEffect(() => {
        // 비로그인 유저일 때 초기화
        if (hydrated && !loggedIn) {
            setInputValue('');
        }
    }, [hydrated, loggedIn]);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (!loggedIn) return;
        setInputValue(e.target.value);
    };

    const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
        if (!loggedIn) {
            e.preventDefault(); // 입력 차단
        }

        if (e.key === 'Enter' && inputValue.trim()) {
            // 로그인된 경우에만 제출 처리
            if (loggedIn) {
                // 메시지 전송 로직 (필요 시 props로 분리)
                console.log('메시지 전송:', inputValue);
                setInputValue('');
            }
        }
    };

    if (!hydrated) return null; // hydration 대기

    return (
        <div className="w-full p-2">
            <input
                type="text"
                value={inputValue}
                onChange={handleChange}
                onKeyDown={handleKeyDown}
                placeholder={
                    loggedIn ? '메시지를 입력하세요...' : '로그인 후 이용해주세요'
                }
                readOnly={!loggedIn}
                className={`w-full px-4 py-2 border rounded-xl transition focus:outline-none ${
                    loggedIn
                        ? 'bg-white text-black border-gray-300 focus:ring-2 focus:ring-blue-500'
                        : 'bg-gray-100 text-gray-400 border-gray-200 cursor-not-allowed'
                }`}
                style={
                    !loggedIn
                        ? { pointerEvents: 'none', userSelect: 'none' }
                        : undefined
                }
            />
        </div>
    );
}
