import { Outlet } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

/**
 * 인증 화면 레이아웃 — 로그인 등 인증 전 화면의 뼈대(가운데 정렬 카드).
 * 실제 로그인 화면은 로그인 구현 시점에 이 레이아웃 하위에 추가한다(골격).
 */
export function AuthLayout() {
  const { t } = useTranslation();
  return (
    <div className="flex min-h-screen items-center justify-center bg-muted/40 p-4">
      <div className="w-full max-w-sm rounded-lg border bg-card p-6 text-card-foreground shadow-sm">
        <h1 className="mb-4 text-center text-xl font-semibold">{t('app.title')}</h1>
        <Outlet />
      </div>
    </div>
  );
}
