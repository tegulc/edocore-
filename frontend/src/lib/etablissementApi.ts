import { api } from './api';
import type { AnneeScolaireProfil, EtablissementProfil, JourFerieProfil, ModuleActifProfil, PeriodeProfil } from '@/types/etablissement';
import type { UtilisateurProfil } from '@/types/auth';

export interface InscriptionEtablissementEntree {
  nom: string;
  sigle?: string;
  pays: string;
  ville?: string;
  typeEtablissement?: string;
  telephone?: string;
  email?: string;
  administrateur: {
    nom: string;
    prenom: string;
    email: string;
    telephone?: string;
  };
}

export interface InscriptionReponse {
  etablissement: EtablissementProfil;
  administrateur: UtilisateurProfil;
  motDePasseTemporaire: string;
}

export const etablissementApi = {
  inscrire: (donnees: InscriptionEtablissementEntree) =>
    api.post<InscriptionReponse>('/etablissements/inscription', donnees),

  moi: () => api.get<EtablissementProfil>('/etablissements/moi'),

  modifier: (donnees: Partial<EtablissementProfil>) => api.put<EtablissementProfil>('/etablissements/moi', donnees),

  modules: () => api.get<ModuleActifProfil[]>('/etablissements/moi/modules'),

  definirModule: (code: string, actif: boolean) =>
    api.patch<ModuleActifProfil>(`/etablissements/moi/modules/${code}`, { actif }),

  joursFeries: () => api.get<JourFerieProfil[]>('/etablissements/moi/jours-feries'),
};

export const anneeScolaireApi = {
  lister: () => api.get<AnneeScolaireProfil[]>('/annees-scolaires'),

  active: () => api.get<AnneeScolaireProfil>('/annees-scolaires/active'),

  creer: (donnees: { libelle: string; dateDebut: string; dateFin: string }) =>
    api.post<AnneeScolaireProfil>('/annees-scolaires', donnees),

  activer: (id: number) => api.patch<AnneeScolaireProfil>(`/annees-scolaires/${id}/activer`),

  periodes: (anneeId: number) => api.get<PeriodeProfil[]>(`/annees-scolaires/${anneeId}/periodes`),

  creerPeriode: (anneeId: number, donnees: { typePeriode: string; numero: number; libelle: string; dateDebut: string; dateFin: string }) =>
    api.post<PeriodeProfil>(`/annees-scolaires/${anneeId}/periodes`, donnees),

  cloturerPeriode: (anneeId: number, periodeId: number) =>
    api.patch<PeriodeProfil>(`/annees-scolaires/${anneeId}/periodes/${periodeId}/cloturer`),
};
