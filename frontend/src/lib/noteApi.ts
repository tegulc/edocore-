import { api } from './api';
import type { BulletinProfil, ConseilClasse, EvaluationProfil, MoyenneGeneraleProfil, NoteProfil } from '@/types/note';

export const evaluationApi = {
  lister: (classeMatiereId: number, periodeId: number) =>
    api.get<EvaluationProfil[]>('/evaluations', { params: { classeMatiereId, periodeId } }),
  creer: (donnees: { classeMatiereId: number; typeEvaluationCode: string; periodeId: number; titre: string; dateEvaluation: string; bareme?: number }) =>
    api.post<EvaluationProfil>('/evaluations', donnees),
  valider: (id: number) => api.patch(`/evaluations/${id}/valider`),
  notes: (evaluationId: number) => api.get<NoteProfil[]>(`/evaluations/${evaluationId}/notes`),
  saisirNotes: (evaluationId: number, notes: { eleveId: number; valeur?: number; absent: boolean; observation?: string }[]) =>
    api.post<NoteProfil[]>(`/evaluations/${evaluationId}/notes`, { notes }),
};

export const bulletinApi = {
  recalculerMoyennes: (classeId: number, periodeId: number) =>
    api.post<MoyenneGeneraleProfil[]>(`/classes/${classeId}/moyennes/recalculer`, null, { params: { periodeId } }),
  genererBulletins: (classeId: number, periodeId: number) =>
    api.post<BulletinProfil[]>(`/classes/${classeId}/bulletins/generer`, null, { params: { periodeId } }),
  valider: (id: number) => api.patch<BulletinProfil>(`/bulletins/${id}/valider`),
  publier: (id: number) => api.patch<BulletinProfil>(`/bulletins/${id}/publier`),
  obtenirPourEleve: (eleveId: number, periodeId: number) => api.get<BulletinProfil>(`/eleves/${eleveId}/bulletins/${periodeId}`),
  telechargerPdf: (id: number) => api.get(`/bulletins/${id}/pdf`, { responseType: 'blob' }),
  enregistrerConseil: (id: number, conseil: ConseilClasse) => api.post<BulletinProfil>(`/bulletins/${id}/conseil`, conseil),
};
