import { create } from 'zustand';

/** 로그인 사용자 정보(세션 방식이므로 토큰이 아니라 사용자 식별·역할을 담는다). */
export interface AuthUser {
  id: string;
  username: string;
  roles: string[];
}

/**
 * 인증 상태(클라이언트 상태 → Zustand). 서버 상태는 TanStack Query로 분리한다.
 * 세션 방식이라 토큰을 보관하지 않는다 — 세션 쿠키는 브라우저가 관리한다.
 */
interface AuthState {
  user: AuthUser | null;
  setUser: (user: AuthUser | null) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  setUser: (user) => set({ user }),
  clear: () => set({ user: null }),
}));
