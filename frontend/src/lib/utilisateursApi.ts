import { api } from './api';
import type { UtilisateurProfil, RoleCode } from '@/types/auth';

/** Conforme au Volume 6 (Documentation API) §2.2 : { donnees, pagination: { page (base 1), taille, total, pagesTotales } }. */
export interface PageReponse<T> {
  donnees: T[];
  pagination: {
    page: number;
    taille: number;
    total: number;
    pagesTotales: number;
  };
}

export interface CreationUtilisateurEntree {
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  /** Premier code = role principal (cahier des charges Module 1 - multi-role). */
  roleCodes: RoleCode[];
  matricule?: string;
}

export const utilisateursApi = {
  /** page en base 1 (conforme au contrat documente), pas base 0. */
  lister: (page = 1, taille = 20) =>
    api.get<PageReponse<UtilisateurProfil>>('/utilisateurs', { params: { page, taille } }),

  creer: (donnees: CreationUtilisateurEntree) =>
    api.post<{ utilisateur: UtilisateurProfil; motDePasseTemporaire: string }>('/utilisateurs', donnees),

  changerStatut: (id: number, statut: string) =>
    api.patch(`/utilisateurs/${id}/statut`, { statut }),
};
