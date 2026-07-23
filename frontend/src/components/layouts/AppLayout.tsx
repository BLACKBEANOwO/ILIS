import { Outlet } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

/**
 * 앱 공통 레이아웃 — 인증 후 화면의 뼈대(헤더 + 본문 아웃렛).
 * 사이드바·사용자 메뉴 등은 화면 요구가 정해지면 확장한다(골격).
 */
export function AppLayout() {
  const { t } = useTranslation();
  return (
    <div className="min-h-screen bg-background text-foreground">
      <header className="border-b">
        <div className="container flex h-14 items-center">
          <span className="text-lg font-semibold">{t('app.title')}</span>
        </div>
      </header>
      <main className="container py-6">
        <Outlet />
      </main>
    </div>
  );
}
