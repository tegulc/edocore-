import type { ReactNode } from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '@/hooks/useAuth';
import type { RoleCode } from '@/types/auth';

interface RouteProtegeeProps {
  children: ReactNode;
  rolesAutorises?: RoleCode[];
}

/**
 * Protege une route : redirige vers /connexion si aucun utilisateur n'est
 * authentifie. Le controle de role est une commodite UI ; l'autorisation
 * reelle reste toujours revalidee par le backend (principe P3).
 */
export function RouteProtegee({ children, rolesAutorises }: RouteProtegeeProps) {
  const { utilisateur, hydrate } = useAuth();

  if (!hydrate) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-ink-50 dark:bg-surface-dark">
        <div className="h-8 w-8 rounded-full border-2 border-brand-500 border-t-transparent animate-spin" />
      </div>
    );
  }

  if (!utilisateur) {
    return <Navigate to="/connexion" replace />;
  }

  if (rolesAutorises && !utilisateur.roles.some((r) => rolesAutorises.includes(r))) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
}
