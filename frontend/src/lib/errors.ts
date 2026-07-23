/** 백엔드 공통 응답 포맷의 필드 단위 오류 상세. */
export interface ErrorDetail {
  field: string;
  rejectedValue: unknown;
  message: string;
}

/** 백엔드 공통 응답 포맷 `{ status, code, message, data, errors }`. */
export interface ApiResponse<T> {
  status: 'SUCCESS' | 'FAIL';
  code: string;
  message: string;
  data: T;
  errors?: ErrorDetail[];
}

/** 목록 응답. 인터셉터가 포맷을 벗기므로 훅이 받는 것은 이 형태다. */
export interface PageResponse<T> {
  content: T[];
  page: number; // 0-based
  size: number;
  totalElements: number;
  totalPages: number;
}

/**
 * 업무 규칙 거절(HTTP 200 + status=FAIL) 및 서버/인증 오류를 화면이 code로 분기할 수 있게 감싼 에러.
 */
export class BusinessError extends Error {
  constructor(
    public readonly code: string, // NOT_REVIEWED, DUPLICATE_LAW_NO ...
    message: string,
    public readonly errors?: ErrorDetail[],
    public readonly httpStatus?: number,
  ) {
    super(message);
    this.name = 'BusinessError';
  }
}
