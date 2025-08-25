'use client';

import { createContext, useContext, ReactNode } from 'react';
import useDialog from '@/hooks/useDialog';
import GlobalDialog from '@/components/ui/GlobalDialog';

const DialogCtx = createContext<ReturnType<typeof useDialog> | null>(null);

export function DialogProvider({ children }: { children: ReactNode }) {
  const dialog = useDialog();          // 단 하나만 생성
  return (
    <DialogCtx.Provider value={dialog}>
      {children}
      {/* 어디서 show()를 호출하든 이 모달이 렌더링됨 */}
      <GlobalDialog
        open={dialog.open}
        message={dialog.message}
        onClose={dialog.handleClose}
      />
    </DialogCtx.Provider>
  );
}

export function useGlobalDialog() {
  const ctx = useContext(DialogCtx);
  if (!ctx) throw new Error('🚨 DialogProvider로 감싸지 않았습니다.');
  return ctx;
}
