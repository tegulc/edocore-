import { create } from 'zustand';

type Theme = 'light' | 'dark';

interface ThemeState {
  theme: Theme;
  basculer: () => void;
  definir: (theme: Theme) => void;
}

const CLE_STOCKAGE = 'educore-theme';

function appliquerAuDocument(theme: Theme) {
  document.documentElement.classList.toggle('dark', theme === 'dark');
  localStorage.setItem(CLE_STOCKAGE, theme);
}

function themeInitial(): Theme {
  const enregistre = localStorage.getItem(CLE_STOCKAGE) as Theme | null;
  if (enregistre) return enregistre;
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

/**
 * Store global du theme clair/sombre. Le script inline dans index.html
 * evite deja le flash au chargement ; ce store synchronise ensuite l'etat
 * React et persiste le choix de l'utilisateur.
 */
export const useTheme = create<ThemeState>((set, get) => ({
  theme: themeInitial(),
  basculer: () => {
    const nouveau = get().theme === 'dark' ? 'light' : 'dark';
    appliquerAuDocument(nouveau);
    set({ theme: nouveau });
  },
  definir: (theme) => {
    appliquerAuDocument(theme);
    set({ theme });
  },
}));
