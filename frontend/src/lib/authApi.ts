import { api } from './api';
import type { UtilisateurProfil } from '@/types/auth';

/**
 * Forme exacte du corps de reponse de /auth/login, /auth/2fa/verifier et
 * /auth/refresh (conforme au Volume 6, Documentation API §4.1), une fois
 * convertie en camelCase par l'intercepteur (voir lib/api.ts).
 *
 * Login ne renvoie JAMAIS d'erreur pour le 2FA : quand deuxFaRequis est vrai,
 * jeton/jetonRafraichissement/utilisateur sont absents et jetonDeuxFa doit
 * etre represente tel quel a authApi.verifierDeuxFa().
 */
export interface ReponseConnexion {
  jeton: string | null;
  jetonRafraichissement: string | null;
  expireDans: number | null;
  deuxFaRequis: boolean;
  jetonDeuxFa: string | null;
  utilisateur: UtilisateurProfil | null;
}

export interface ErreurApi {
  code: string;
  message: string;
  champ?: string;
}

/** Extrait un ErreurApi lisible depuis une reponse axios en erreur (enveloppe { erreur: {...} } du Volume 6). */
export function extraireErreurApi(erreur: unknown): ErreurApi {
  const e = erreur as { response?: { data?: { erreur?: { code?: string; message?: string; champ?: string } } } };
  const contenu = e.response?.data?.erreur;
  return {
    code: contenu?.code ?? 'ERREUR_INCONNUE',
    message: contenu?.message ?? "Une erreur inattendue est survenue.",
    champ: contenu?.champ,
  };
}

export const authApi = {
  /** Etape 1 : email + mot de passe uniquement. Consulter deuxFaRequis dans la reponse avant de considerer la connexion terminee. */
  login: (email: string, motDePasse: string) =>
    api.post<ReponseConnexion>('/auth/login', { email, motDePasse }),

  /** Etape 2, uniquement si login() a renvoye deuxFaRequis=true. */
  verifierDeuxFa: (jetonDeuxFa: string, code: string) =>
    api.post<ReponseConnexion>('/auth/2fa/verifier', { jetonDeuxFa, code }),

  refresh: (jetonRafraichissement: string) =>
    api.post<ReponseConnexion>('/auth/refresh', { jetonRafraichissement }),

  logout: (jetonRafraichissement: string) =>
    api.post('/auth/logout', { jetonRafraichissement }),

  moi: () => api.get<UtilisateurProfil>('/auth/me'),

  changerMotDePasse: (ancienMotDePasse: string, nouveauMotDePasse: string) =>
    api.post<ReponseConnexion>('/auth/changer-mot-de-passe', { ancienMotDePasse, nouveauMotDePasse }),

  motDePasseOublie: (email: string) => api.post('/auth/mot-de-passe-oublie', { email }),

  /** Active le 2FA sur le compte (distinct de verifierDeuxFa, utilise a la connexion). */
  initierDeuxFa: () => api.post<{ secretBase32: string; uriProvisionnement: string }>('/auth/2fa/initier'),

  confirmerDeuxFa: (code: string) => api.post('/auth/2fa/confirmer', { code }),
};
