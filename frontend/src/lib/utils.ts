import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

/** Tailwind 클래스 병합 유틸. 조건부 클래스는 문자열 연결 대신 이 함수로 합친다. */
export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}
