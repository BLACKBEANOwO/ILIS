import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import en from '@/i18n/locales/en.json';
import id from '@/i18n/locales/id.json';
import ko from '@/i18n/locales/ko.json';

// 전 화면 다국어. 하드코딩 문자열 대신 i18n 키를 쓴다.
void i18n.use(initReactI18next).init({
  resources: {
    en: { translation: en },
    id: { translation: id },
    ko: { translation: ko },
  },
  lng: 'id',
  fallbackLng: 'en',
  interpolation: { escapeValue: false },
});

export default i18n;
