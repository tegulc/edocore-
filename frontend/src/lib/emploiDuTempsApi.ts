import { api } from './api';
import type { BilanPresence, CreneauHoraireProfil, PresenceProfil, SeanceProfil } from '@/types/emploiDuTemps';

export const emploiDuTempsApi = {
  creneaux: () => api.get<CreneauHoraireProfil[]>('/creneaux-horaires'),
  creerCreneau: (donnees: { libelle: string; heureDebut: string; heureFin: string; ordre: number }) =>
    api.post<CreneauHoraireProfil>('/creneaux-horaires', donnees),
  parClasse: (classeId: number) => api.get<SeanceProfil[]>(`/emploi-du-temps/classes/${classeId}`),
  creerSeance: (donnees: { classeId: number; matiereId: number; enseignantId: number; salleId?: number; creneauId: number; jourSemaine: number }) =>
    api.post<SeanceProfil>('/emploi-du-temps', donnees),
};

export const presenceApi = {
  enregistrer: (donnees: { eleveId: number; classeMatiereId?: number; dateJour: string; typePresence: string; methodePointage?: string }) =>
    api.post<PresenceProfil>('/presences', donnees),
  justifier: (id: number, motif: string) => api.patch<PresenceProfil>(`/presences/${id}/justifier`, { motif }),
  historique: (eleveId: number, debut: string, fin: string) =>
    api.get<PresenceProfil[]>(`/presences/eleves/${eleveId}`, { params: { debut, fin } }),
  bilan: (eleveId: number, debut: string, fin: string) =>
    api.get<BilanPresence>(`/presences/eleves/${eleveId}/bilan`, { params: { debut, fin } }),
};
