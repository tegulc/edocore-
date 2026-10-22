import { useEffect, useState } from 'react';
import { Outlet, useLocation, Navigate } from 'react-router-dom';
import { Sidebar } from './Sidebar';
import { Navbar } from './Navbar';
import { Footer } from './Footer';
import { useAuth } from '@/hooks/useAuth';
import { useEtablissement } from '@/hooks/useEtablissement';
import { etablissementApi } from '@/lib/etablissementApi';

/**
 * Squelette d'ecran pour toutes les pages authentifiees :
 * Sidebar (fixe desktop / tiroir mobile) + Navbar + contenu + Footer.
 * S'adapte de mobile (< 640px) a tres large ecran (le contenu est borne
 * en largeur pour rester lisible sur les grands moniteurs).
 */
export function AppShell() {
  const [sidebarOuverte, setSidebarOuverte] = useState(false);
  const { utilisateur } = useAuth();
  const { etablissement, definir } = useEtablissement();
  const location = useLocation();

  useEffect(() => {
    if (!etablissement) {
      etablissementApi.moi().then(({ data }) => definir(data)).catch(() => {});
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  if (utilisateur?.doitChangerMotDePasse && location.pathname !== '/profil') {
    return <Navigate to="/profil" replace />;
  }

  return (
    <div className="flex min-h-screen bg-ink-50 dark:bg-surface-dark text-ink-900 dark:text-ink-50">
      <Sidebar
        ouverte={sidebarOuverte}
        onFermer={() => setSidebarOuverte(false)}
        rolesUtilisateur={utilisateur?.roles}
      />

      <div className="flex-1 flex flex-col min-w-0">
        <Navbar
          onOuvrirMenu={() => setSidebarOuverte(true)}
          utilisateur={utilisateur}
          nomEtablissement={etablissement?.nom}
        />

        <main className="flex-1 px-4 sm:px-6 py-6 max-w-[1600px] w-full mx-auto">
          <Outlet />
        </main>

        <Footer />
      </div>
    </div>
  );
}
