import { useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { AppShell } from '@/components/layout/AppShell';
import { RouteProtegee } from '@/components/layout/RouteProtegee';
import PageConnexion from '@/pages/auth/PageConnexion';
import PageInscription from '@/pages/auth/PageInscription';
import PageTableauDeBord from '@/pages/dashboard/PageTableauDeBord';
import PageProfil from '@/pages/dashboard/PageProfil';
import PageUtilisateurs from '@/pages/dashboard/PageUtilisateurs';
import PageEtablissement from '@/pages/dashboard/PageEtablissement';
import PageClasses from '@/pages/dashboard/PageClasses';
import PageEnseignants from '@/pages/dashboard/PageEnseignants';
import PageEleves from '@/pages/dashboard/PageEleves';
import PageParents from '@/pages/dashboard/PageParents';
import PageEmploiDuTemps from '@/pages/dashboard/PageEmploiDuTemps';
import PagePresences from '@/pages/dashboard/PagePresences';
import PageNotes from '@/pages/dashboard/PageNotes';
import PageBulletins from '@/pages/dashboard/PageBulletins';
import PageReferentiels from '@/pages/dashboard/PageReferentiels';
import PagePaiements from '@/pages/dashboard/PagePaiements';
import PageComptabilite from '@/pages/dashboard/PageComptabilite';
import PageBibliotheque from '@/pages/dashboard/PageBibliotheque';
import PageDiscipline from '@/pages/dashboard/PageDiscipline';
import PageInfirmerie from '@/pages/dashboard/PageInfirmerie';
import PageTransport from '@/pages/dashboard/PageTransport';
import PageCantine from '@/pages/dashboard/PageCantine';
import PageReunions from '@/pages/dashboard/PageReunions';
import PageMessagerie from '@/pages/dashboard/PageMessagerie';
import PageNotifications from '@/pages/dashboard/PageNotifications';
import PageSecurite from '@/pages/dashboard/PageSecurite';
import PageMonEspace from '@/pages/dashboard/PageMonEspace';
import PageCahierDeTexte from '@/pages/dashboard/PageCahierDeTexte';
import PageDevoirs from '@/pages/dashboard/PageDevoirs';
import PagePersonnel from '@/pages/dashboard/PagePersonnel';
import PageDocuments from '@/pages/dashboard/PageDocuments';
import PageRapports from '@/pages/dashboard/PageRapports';
import { useAuth } from '@/hooks/useAuth';
import { authApi } from '@/lib/authApi';
import { possedeUnRole } from '@/types/auth';

/**
 * Arborescence de routage. Chaque module ajoutera ses propres routes
 * enfants sous <AppShell/> au fil des phases.
 */
export default function App() {
  const { refreshToken, connecter, deconnecter, terminerHydratation, utilisateur } = useAuth();

  useEffect(() => {
    async function hydrater() {
      if (!refreshToken) {
        terminerHydratation();
        return;
      }
      try {
        const { data } = await authApi.refresh(refreshToken);
        if (data.jeton && data.jetonRafraichissement && data.utilisateur) {
          connecter(data.utilisateur, data.jeton, data.jetonRafraichissement);
        } else {
          deconnecter();
        }
      } catch {
        deconnecter();
      } finally {
        terminerHydratation();
      }
    }
    hydrater();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  /** Redirige parent/eleve vers leur portail au lieu du dashboard admin. */
  function AccueilParRole() {
    if (possedeUnRole(utilisateur, ['PARENT', 'ELEVE'])) {
      return <Navigate to="/mon-espace" replace />;
    }
    return <PageTableauDeBord />;
  }

  return (
    <Routes>
      <Route path="/connexion" element={<PageConnexion />} />
      <Route path="/inscription" element={<PageInscription />} />

      <Route element={<RouteProtegee><AppShell /></RouteProtegee>}>
        <Route path="/" element={<AccueilParRole />} />
        <Route path="/profil" element={<PageProfil />} />
        <Route path="/mon-espace" element={<PageMonEspace />} />

        <Route path="/etablissement" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR']}><PageEtablissement /></RouteProtegee>} />
        <Route path="/utilisateurs" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR']}><PageUtilisateurs /></RouteProtegee>} />
        <Route path="/classes" element={<PageClasses />} />
        <Route path="/enseignants" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'CENSEUR']}><PageEnseignants /></RouteProtegee>} />
        <Route path="/eleves" element={<PageEleves />} />
        <Route path="/parents" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'CENSEUR', 'SECRETAIRE']}><PageParents /></RouteProtegee>} />
        <Route path="/referentiels" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'CENSEUR']}><PageReferentiels /></RouteProtegee>} />
        <Route path="/emploi-du-temps" element={<PageEmploiDuTemps />} />
        <Route path="/presences" element={<PagePresences />} />
        <Route path="/notes" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR']}><PageNotes /></RouteProtegee>} />
        <Route path="/bulletins" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'CENSEUR']}><PageBulletins /></RouteProtegee>} />
        <Route path="/cahier-de-texte" element={<PageCahierDeTexte />} />
        <Route path="/devoirs" element={<PageDevoirs />} />
        <Route path="/paiements" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'COMPTABLE', 'ECONOME', 'SECRETAIRE']}><PagePaiements /></RouteProtegee>} />
        <Route path="/comptabilite" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'COMPTABLE', 'ECONOME']}><PageComptabilite /></RouteProtegee>} />
        <Route path="/bibliotheque" element={<PageBibliotheque />} />
        <Route path="/discipline" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'CENSEUR', 'SURVEILLANT_GENERAL']}><PageDiscipline /></RouteProtegee>} />
        <Route path="/infirmerie" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'INFIRMIER']}><PageInfirmerie /></RouteProtegee>} />
        <Route path="/transport" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'ECONOME', 'CHAUFFEUR']}><PageTransport /></RouteProtegee>} />
        <Route path="/cantine" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'ECONOME']}><PageCantine /></RouteProtegee>} />
        <Route path="/personnel" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR']}><PagePersonnel /></RouteProtegee>} />
        <Route path="/documents" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'SECRETAIRE', 'CENSEUR']}><PageDocuments /></RouteProtegee>} />
        <Route path="/rapports" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR', 'CENSEUR', 'COMPTABLE', 'ECONOME']}><PageRapports /></RouteProtegee>} />
        <Route path="/reunions" element={<PageReunions />} />
        <Route path="/messagerie" element={<PageMessagerie />} />
        <Route path="/notifications" element={<PageNotifications />} />
        <Route path="/securite" element={<RouteProtegee rolesAutorises={['ADMIN', 'DIRECTEUR']}><PageSecurite /></RouteProtegee>} />
      </Route>
    </Routes>
  );
}
