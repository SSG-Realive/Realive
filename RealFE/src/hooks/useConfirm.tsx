import { useState, useCallback } from "react";
import ConfirmDialog from "@/components/ui/ConfirmDialog";

export default function useConfirm() {
  const [state, setState] = useState<{
    open: boolean;
    message: string;
    resolve: (v: boolean) => void;
  } | null>(null);

  /** confirm("문구") 처럼 사용 → Promise<boolean> */
  const confirm = useCallback(
    (message: string): Promise<boolean> =>
      new Promise((resolve) => {
        setState({ open: true, message, resolve });
      }),
    []
  );

  // --- ❗ JSX는 괄호로 감싸고, 없을 때는 null 을 반환 ---
  const dialog = state ? (
    <ConfirmDialog
      open={state.open}
      message={state.message}
      onResolve={(v) => {
        state.resolve(v);   // state 가 null 아님이 보장
        setState(null);     // 모달 닫기
      }}
    />
  ) : null;

  return { confirm, dialog };
}
