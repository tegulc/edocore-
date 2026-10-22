import { api } from './api';
import type { PageReponse } from './utilisateursApi';
import type { EleveParentProfil, EleveProfil, InscriptionProfil, ParentProfil } from '@/types/eleve';

export interface CreationEleveEntree {
  nom: string;
  prenom: string;
  matricule: string;
  sexe: 'M' | 'F';
  dateNaissance: string;
  lieuNaissance?: string;
  nationalite?: string;
  groupeSanguin?: string;
  allergies?: string;
  classeId?: number;
}

export const eleveApi = {
  lister: (page = 1, taille = 20) => api.get<PageReponse<EleveProfil>>('/eleves', { params: { page, taille } }),
  obtenir: (id: number) => api.get<EleveProfil>(`/eleves/${id}`),
  creer: (donnees: CreationEleveEntree) => api.post<EleveProfil>('/eleves', donnees),
  changerStatut: (id: number, statut: string) => api.patch(`/eleves/${id}/statut`, { statut }),
  inscriptions: (id: number) => api.get<InscriptionProfil[]>(`/eleves/${id}/inscriptions`),
  inscrire: (id: number, classeId: number, typeInscription?: string) =>
    api.post<InscriptionProfil>(`/eleves/${id}/inscriptions`, { classeId, typeInscription }),
  parents: (id: number) => api.get<EleveParentProfil[]>(`/eleves/${id}/parents`),
  lierParent: (id: number, donnees: { parentId: number; lienParente: string; estContactPrincipal: boolean; autoriseRecupererEleve: boolean }) =>
    api.post<EleveParentProfil>(`/eleves/${id}/parents`, donnees),
};

export const parentApi = {
  lister: () => api.get<ParentProfil[]>('/parents'),
  creer: (donnees: { nom: string; prenom: string; email: string; telephone?: string; profession?: string; adresse?: string }) =>
    api.post<{ parent: ParentProfil; motDePasseTemporaire: string }>('/parents', donnees),
};
