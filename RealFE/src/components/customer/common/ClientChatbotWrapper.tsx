'use client';

import dynamic from 'next/dynamic';
import { Suspense } from 'react';

const ChatBotWidget = dynamic(() => import('@/components/aiChatbot/chatbot'), {
  ssr: false,
});

export default function ClientChatbotWrapper() {
  return (
    <Suspense fallback={null}>
      <ChatBotWidget />
    </Suspense>
  );
}
