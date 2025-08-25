"use client";

import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogFooter,
  DialogTitle,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";

interface Props {
  open: boolean;
  message: string;
  onResolve: (result: boolean) => void; // true=확인, false=취소
}

export default function ConfirmDialog({ open, message, onResolve }: Props) {
  return (
    <Dialog open={open} onOpenChange={() => onResolve(false)}>
      <DialogContent showCloseButton={false} className="sm:max-w-sm">
        <DialogHeader>
          <DialogTitle className="sr-only">확인</DialogTitle>
        </DialogHeader>

        <p className="py-4 whitespace-pre-line">{message}</p>

        <DialogFooter className="gap-3 sm:justify-center">
          <Button variant="outline" onClick={() => onResolve(false)}>
            취소
          </Button>
          <Button onClick={() => onResolve(true)}>확인</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
