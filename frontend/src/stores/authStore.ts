import { create } from 'zustand';

/** JWT 등 인증 상태(클라이언트 상태 → Zustand). 서버 상태는 TanStack Query로 분리한다. */
interface AuthState {
  token: string | null;
  setToken: (token: string) => void;
  clear: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  token: null,
  setToken: (token) => set({ token }),
  clear: () => set({ token: null }),
}));
