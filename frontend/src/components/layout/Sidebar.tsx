import { NavLink } from 'react-router-dom';
import { X } from 'lucide-react';
import clsx from 'clsx';
import { NAVIGATION } from './navigation';
import type { RoleCode } from '@/types/auth';

interface SidebarProps {
  ouverte: boolean;
  onFermer: () => void;
  rolesUtilisateur?: RoleCode[];
}

/**
 * Barre laterale responsive :
 * - Desktop (lg+) : fixe, toujours visible
 * - Mobile/tablette : panneau glissant en overlay, ferme au clic sur un lien
 * Filtre les entrees selon TOUS les roles de l'utilisateur connecte (support
 * multi-role, cahier des charges Module 1) : un element est visible des lors
 * qu'au moins un des roles de l'utilisateur y est autorise.
 */
export function Sidebar({ ouverte, onFermer, rolesUtilisateur = [] }: SidebarProps) {
  const elements = NAVIGATION.filter((el) => !el.roles || el.roles.some((r) => rolesUtilisateur.includes(r)));

  return (
    <>
      {/* Overlay mobile */}
      {ouverte && (
        <div
          className="fixed inset-0 z-30 bg-ink-900/50 backdrop-blur-sm lg:hidden"
          onClick={onFermer}
          aria-hidden="true"
        />
      )}

      <aside
        className={clsx(
          'fixed z-40 inset-y-0 left-0 w-72 shrink-0 border-r border-ink-100 dark:border-white/10',
          'bg-white dark:bg-surface-darkAlt transition-transform duration-200 lg:translate-x-0 lg:static lg:z-auto',
          ouverte ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        <div className="flex items-center justify-between h-16 px-5 border-b border-ink-100 dark:border-white/10">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-brand-500 to-accent-500" />
            <span className="font-display font-bold text-lg tracking-tight">EDUCORE</span>
          </div>
          <button
            onClick={onFermer}
            className="lg:hidden p-1.5 rounded-md text-ink-400 hover:bg-ink-50 dark:hover:bg-white/5"
            aria-label="Fermer le menu"
          >
            <X size={20} />
          </button>
        </div>

        <nav className="p-3 space-y-1 overflow-y-auto h-[calc(100%-4rem)]">
          {elements.map(({ libelle, chemin, icone: Icone }) => (
            <NavLink
              key={chemin}
              to={chemin}
              end={chemin === '/'}
              onClick={onFermer}
              className={({ isActive }) =>
                clsx(
                  'relative flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-theme',
                  isActive
                    ? 'bg-brand-50 text-brand-700 dark:bg-brand-500/15 dark:text-brand-300'
                    : 'text-ink-700 dark:text-ink-100/80 hover:bg-ink-50 dark:hover:bg-white/5'
                )
              }
            >
              {({ isActive }) => (
                <>
                  {isActive && (
                    <span className="absolute left-0 top-1.5 bottom-1.5 w-0.5 rounded-full bg-gradient-to-b from-brand-500 to-accent-500" />
                  )}
                  <Icone size={18} strokeWidth={2} />
                  {libelle}
                </>
              )}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
