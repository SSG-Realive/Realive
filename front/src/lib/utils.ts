import { type ClassValue, clsx } from "clsx"
import { twMerge } from "tailwind-merge"
import { stringify } from 'qs';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

export const paramsSerializer = (params: any) => {
  return stringify(params, { arrayFormat: 'repeat' });
};
