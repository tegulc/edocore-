import axios from 'axios';
import { useAuth } from '@/hooks/useAuth';
import { versCamelCase, versSnakeCase } from './caseConversion';

/**
 * Client HTTP unique de l'application. Toute requete vers l'API EDUCORE
 * doit passer par ici :
 *  - le jeton JWT est attache automatiquement
 *  - le corps sortant est converti camelCase -> snake_case, la reponse
 *    entrante snake_case -> camelCase (contrat du Volume 6, Documentation API)
 *  - un access token expire (401) declenche un rafraichissement automatique
 *    avant de rejouer la requete initiale une seule fois
 */
export const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

const ROUTES_PUBLIQUES = ['/auth/login', '/auth/2fa/verifier', '/auth/mot-de-passe-oublie', '/etablissements/inscription'];

function estRoutePublique(url?: string): boolean {
  return ROUTES_PUBLIQUES.some((route) => url?.includes(route));
}

api.interceptors.request.use((config) => {
  const token = useAuth.getState().accessToken;
  if (token && !estRoutePublique(config.url)) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  if (config.data && !(config.data instanceof FormData)) {
    config.data = versSnakeCase(config.data);
  }
  return config;
});

api.interceptors.response.use((reponse) => {
  if (reponse.data) {
    reponse.data = versCamelCase(reponse.data);
  }
  return reponse;
});

// Evite les rafraichissements concurrents si plusieurs requetes echouent en meme temps.
let rafraichissementEnCours: Promise<string | null> | null = null;

async function tenterRafraichissement(): Promise<string | null> {
  const { refreshToken, connecter, utilisateur, deconnecter } = useAuth.getState();
  if (!refreshToken) return null;

  try {
    const reponse = await axios.post('/api/v1/auth/refresh', versSnakeCase({ jetonRafraichissement: refreshToken }));
    const donnees = versCamelCase(reponse.data) as { jeton: string; jetonRafraichissement: string; utilisateur?: unknown };
    connecter((donnees.utilisateur ?? utilisateur) as never, donnees.jeton, donnees.jetonRafraichissement);
    return donnees.jeton;
  } catch {
    deconnecter();
    return null;
  }
}

api.interceptors.response.use(
  (reponse) => reponse,
  async (erreur) => {
    const requeteOriginale = erreur.config;

    if (erreur.response?.status === 401 && !requeteOriginale?._dejaTentee && !requeteOriginale?.url?.includes('/auth/')) {
      requeteOriginale._dejaTentee = true;

      if (!rafraichissementEnCours) {
        rafraichissementEnCours = tenterRafraichissement().finally(() => {
          rafraichissementEnCours = null;
        });
      }

      const nouveauToken = await rafraichissementEnCours;
      if (nouveauToken) {
        requeteOriginale.headers.Authorization = `Bearer ${nouveauToken}`;
        return api(requeteOriginale);
      }
    }

    // Normalise le corps d'erreur en camelCase egalement, pour que extraireErreurApi
    // (lib/authApi.ts) reste simple a lire malgre l'enveloppe { erreur: {...} } du Volume 6.
    if (erreur.response?.data) {
      erreur.response.data = versCamelCase(erreur.response.data);
    }

    return Promise.reject(erreur);
  }
);
