import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Menu, Sun, Moon, Bell, ChevronDown } from 'lucide-react';
import { useTheme } from '@/hooks/useTheme';
import { useAuth } from '@/hooks/useAuth';
import { authApi } from '@/lib/authApi';
import type { UtilisateurProfil } from '@/types/auth';

interface NavbarProps {
  onOuvrirMenu: () => void;
  utilisateur?: UtilisateurProfil | null;
  nomEtablissement?: string;
}

/**
 * Barre superieure : bouton hamburger (mobile), nom de l'etablissement,
 * bascule mode sombre, notifications, menu utilisateur. Reste fixee en haut
 * et reste utilisable sur ecran etroit (le libelle etablissement se masque
 * en dessous de sm pour laisser la place).
 */
export function Navbar({ onOuvrirMenu, utilisateur, nomEtablissement }: NavbarProps) {
  const { theme, basculer } = useTheme();
  const { refreshToken, deconnecter } = useAuth();
  const navigate = useNavigate();
  const [menuOuvert, setMenuOuvert] = useState(false);

  async function gererDeconnexion() {
    if (refreshToken) {
      try { await authApi.logout(refreshToken); } catch { /* on deconnecte localement quoi qu'il arrive */ }
    }
    deconnecter();
    navigate('/connexion', { replace: true });
  }

  return (
    <header className="sticky top-0 z-20 h-16 flex items-center justify-between gap-4 px-4 sm:px-6 border-b border-ink-100 dark:border-white/10 bg-white/80 dark:bg-surface-dark/80 backdrop-blur-md">
      <div className="flex items-center gap-3 min-w-0">
        <button
          onClick={onOuvrirMenu}
          className="lg:hidden p-2 -ml-2 rounded-md text-ink-700 dark:text-ink-100 hover:bg-ink-50 dark:hover:bg-white/5"
          aria-label="Ouvrir le menu"
        >
          <Menu size={22} />
        </button>
        <div className="hidden sm:flex items-center gap-2 min-w-0">
          <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse shrink-0" aria-hidden="true" />
          <span className="text-sm font-medium text-ink-700 dark:text-ink-100/90 truncate">
            {nomEtablissement ?? 'Etablissement non defini'}
          </span>
        </div>
      </div>

      <div className="flex items-center gap-1.5 sm:gap-2">
        <button
          onClick={basculer}
          className="p-2 rounded-md text-ink-700 dark:text-ink-100 hover:bg-ink-50 dark:hover:bg-white/5 transition-theme"
          aria-label={theme === 'dark' ? 'Passer au mode clair' : 'Passer au mode sombre'}
        >
          {theme === 'dark' ? <Sun size={19} /> : <Moon size={19} />}
        </button>

        <button
          className="relative p-2 rounded-md text-ink-700 dark:text-ink-100 hover:bg-ink-50 dark:hover:bg-white/5"
          aria-label="Notifications"
        >
          <Bell size={19} />
          <span className="absolute top-1.5 right-1.5 h-2 w-2 rounded-full bg-accent-500" />
        </button>

        <div className="relative">
          <button
            onClick={() => setMenuOuvert((v) => !v)}
            className="flex items-center gap-2 pl-2 pr-1 py-1 rounded-md hover:bg-ink-50 dark:hover:bg-white/5"
          >
            <div className="h-8 w-8 rounded-full bg-gradient-to-br from-brand-500 to-accent-500 flex items-center justify-center text-white text-xs font-semibold">
              {utilisateur ? `${utilisateur.prenom[0]}${utilisateur.nom[0]}` : '?'}
            </div>
            <span className="hidden sm:block text-sm font-medium max-w-[8rem] truncate">
              {utilisateur ? `${utilisateur.prenom} ${utilisateur.nom}` : 'Invite'}
            </span>
            <ChevronDown size={16} className="hidden sm:block text-ink-400" />
          </button>

          {menuOuvert && (
            <div className="absolute right-0 mt-2 w-48 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt shadow-card dark:shadow-card-dark py-1 text-sm">
              <button onClick={() => { setMenuOuvert(false); navigate('/profil'); }} className="w-full text-left px-3 py-2 hover:bg-ink-50 dark:hover:bg-white/5">Mon profil</button>
              <hr className="my-1 border-ink-100 dark:border-white/10" />
              <button onClick={gererDeconnexion} className="w-full text-left px-3 py-2 text-red-600 hover:bg-red-50 dark:hover:bg-red-500/10">
                Se déconnecter
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
}
