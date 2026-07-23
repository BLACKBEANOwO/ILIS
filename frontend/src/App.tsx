import { Routes, Route } from 'react-router-dom';
import { useTranslation } from 'react-i18next';

function Home() {
  const { t } = useTranslation();
  return (
    <main className="flex min-h-screen flex-col items-center justify-center gap-2">
      <h1 className="text-3xl font-bold">{t('app.title')}</h1>
      <p className="text-gray-500">{t('app.subtitle')}</p>
    </main>
  );
}

export default function App() {
  return (
    <Routes>
      <Route path="/" element={<Home />} />
    </Routes>
  );
}
