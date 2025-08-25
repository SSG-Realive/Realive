// components/ui/GlobalDialog.tsx
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
  onClose: () => void;
}

export default function GlobalDialog({ open, message, onClose }: Props) {
  return (
    <Dialog open={open} onOpenChange={onClose}>
      <DialogContent showCloseButton={false} className="sm:max-w-sm">
        <DialogHeader>
          <DialogTitle className="sr-only">알림</DialogTitle>
        </DialogHeader>

        <p className="py-4 whitespace-pre-line">{message}</p>

        <DialogFooter>
          <Button onClick={onClose} className="w-full">확인</Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}
