import { useCallback, useRef } from 'react';

export function useThrottle<T extends (...args: unknown[]) => void>(
  callback: T,
  delay: number
): (...args: Parameters<T>) => void {
  const lastCall = useRef<number>(0);
  const timeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  return useCallback((...args: Parameters<T>) => {
    const now = Date.now();

    if (now - lastCall.current >= delay) {
      lastCall.current = now;
      callback(...args);
    } else {
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      timeoutRef.current = setTimeout(() => {
        lastCall.current = Date.now();
        callback(...args);
      }, delay - (now - lastCall.current));
    }
  }, [callback, delay]);
}
