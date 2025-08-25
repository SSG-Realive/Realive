import { cn } from '@/lib/utils';
import React from 'react';

interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  message: string;
  type: 'success' | 'error';
  className?: string;
  buttonClassName?: string;
  titleClassName?: string;
}

const Modal: React.FC<ModalProps> = ({
  isOpen,
  onClose,
  title,
  message,
  type,
  className,
  buttonClassName,
  titleClassName,
}) => {
  if (!isOpen) return null;

  return (
    <div
      className={cn("fixed inset-0 bg-black/40 flex items-center justify-center z-50", className)}
    >
      <div className="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
        <div className={`text-2xl font-bold mb-4 ${
          type === 'success' ? 'text-green-600' : 'text-red-600'
        } ${titleClassName}`}>
          {title}
        </div>
        <p className="text-gray-700 mb-6">{message}</p>
        <button
          onClick={onClose}
          className={`w-full py-2 px-4 rounded ${
            type === 'success' 
              ? 'bg-green-500 hover:bg-green-600' 
              : 'bg-red-500 hover:bg-red-600'
          } text-white font-semibold transition-colors ${buttonClassName}`}
        >
          확인
        </button>
      </div>
    </div>

  );
};
export default Modal;