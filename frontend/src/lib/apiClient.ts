import axios, { AxiosError, AxiosResponse } from 'axios';
import { toast } from 'sonner';
import i18n from '@/i18n';
import { useAuthStore } from '@/stores/authStore';
import { BusinessError, type ApiResponse } from '@/lib/errors';

/**
 * 공통 응답 포맷을 인터셉터 한 곳에서 해체한다.
 * 업무 규칙 거절은 HTTP 200 + status=FAIL 이므로, 벗기지 않고 쓰면 실패가 성공으로 처리된다.
 */
export const apiClient = axios.create({ baseURL: '/api' });

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token; // JWT
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

apiClient.interceptors.response.use(
  (res: AxiosResponse) => {
    const body = res.data as ApiResponse<unknown>;
    if (body.status === 'FAIL') {
      // HTTP 200 + FAIL = 업무 규칙 거절 → 화면이 code 로 분기 (여기서 토스트 띄우지 않음)
      throw new BusinessError(body.code, body.message, body.errors, 200);
    }
    // 컴포넌트는 data 만 본다
    return body.data as unknown as AxiosResponse;
  },
  (error: AxiosError) => {
    const status = error.response?.status;
    const body = error.response?.data as ApiResponse<unknown> | undefined;

    // 401 — 세션 만료. 스토어를 비우고 로그인으로 (라우터를 직접 import 하지 않는다)
    if (status === 401) {
      useAuthStore.getState().clear();
      window.location.assign('/login');
      return Promise.reject(new BusinessError('UNAUTHORIZED', '', undefined, 401));
    }

    // 403 — 권한 없음. 공통 안내만 (권한 판정은 백엔드 Security 소관)
    if (status === 403) {
      toast.error(i18n.t('error.forbidden'));
    }

    // 5xx·네트워크 — 공통 토스트. 화면마다 처리하지 않는다
    if (!status || status >= 500) {
      toast.error(i18n.t('error.server'));
    }

    // 공통 응답 포맷이면 같은 형태로 변환해 화면·폼이 code·errors 를 쓸 수 있게 한다
    if (body?.status === 'FAIL') {
      return Promise.reject(new BusinessError(body.code, body.message, body.errors, status));
    }
    return Promise.reject(error);
  },
);
