'use client';

import { useState, useCallback, useRef } from 'react';

export default function useDialog() {
  const [open, setOpen] = useState(false);
  const [message, setMessage] = useState('');

  // ✅ 1. Promise의 resolve 함수를 저장할 ref를 생성합니다.
  const resolvePromiseRef = useRef<(() => void) | null>(null);

  // ✅ 2. show 함수가 Promise를 반환하도록 수정합니다.
  const show = useCallback((message: string): Promise<void> => {
    setMessage(message);
    setOpen(true);
    // Promise를 생성하고, 그 resolve 함수를 ref에 저장해 둡니다.
    // 이 Promise는 handleClose가 호출되기 전까지는 완료되지 않습니다.
    return new Promise((resolve) => {
      resolvePromiseRef.current = resolve;
    });
  }, []);

  // ✅ 3. 다이얼로그를 닫을 때, 저장해둔 resolve 함수를 실행하는 핸들러
  const handleClose = useCallback(() => {
    // 저장된 resolve 함수가 있다면 실행하여 Promise를 완료시킵니다.
    if (resolvePromiseRef.current) {
      resolvePromiseRef.current();
      resolvePromiseRef.current = null; // 사용 후 초기화
    }
    setOpen(false);
  }, []);

  // 이제 open, message, show와 함께 handleClose를 반환합니다.
  return { open, message, show, handleClose };
}