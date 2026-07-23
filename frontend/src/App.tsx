import { Routes, Route } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { AppLayout } from '@/components/layouts/AppLayout';

function Home() {
  const { t } = useTranslation();
  return (
    <section className="space-y-1">
      <h2 className="text-2xl font-bold">{t('app.title')}</h2>
      <p className="text-muted-foreground">{t('app.subtitle')}</p>
    </section>
  );
}

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<Home />} />
      </Route>
    </Routes>
  );
}
