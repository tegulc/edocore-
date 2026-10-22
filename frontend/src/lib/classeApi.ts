import { api } from './api';
import type { ClasseMatiereProfil, ClasseProfil, EnseignantProfil, MatiereProfil, NiveauProfil, SalleProfil, SerieProfil } from '@/types/classe';

export const structureApi = {
  niveaux: () => api.get<NiveauProfil[]>('/niveaux'),
  creerNiveau: (donnees: { code: string; libelle: string; ordre: number; cycle?: string }) => api.post<NiveauProfil>('/niveaux', donnees),
  series: () => api.get<SerieProfil[]>('/series'),
  creerSerie: (donnees: { code: string; libelle: string }) => api.post<SerieProfil>('/series', donnees),
  salles: () => api.get<SalleProfil[]>('/salles'),
  creerSalle: (donnees: { nom: string; capacite: number; typeSalle?: string }) => api.post<SalleProfil>('/salles', donnees),
  matieres: () => api.get<MatiereProfil[]>('/matieres'),
  creerMatiere: (donnees: { code: string; libelle: string; couleur?: string }) => api.post<MatiereProfil>('/matieres', donnees),
};

export const classeApi = {
  lister: () => api.get<ClasseProfil[]>('/classes'),
  creer: (donnees: { niveauId: number; serieId?: number; libelle: string; salleId?: number; professeurPrincipalId?: number; capacite?: number }) =>
    api.post<ClasseProfil>('/classes', donnees),
  matieres: (classeId: number) => api.get<ClasseMatiereProfil[]>(`/classes/${classeId}/matieres`),
  affecterMatiere: (classeId: number, donnees: { matiereId: number; enseignantId?: number; coefficient: number; volumeHoraireHebdo: number }) =>
    api.post<ClasseMatiereProfil>(`/classes/${classeId}/matieres`, donnees),
};

export const enseignantApi = {
  lister: () => api.get<EnseignantProfil[]>('/enseignants'),
  creer: (donnees: {
    nom: string; prenom: string; email: string; telephone?: string; matricule?: string; specialite?: string;
    diplomePrincipal?: string; dateEmbauche?: string; typeContrat?: string;
  }) => api.post<{ enseignant: EnseignantProfil; motDePasseTemporaire: string }>('/enseignants', donnees),
};
