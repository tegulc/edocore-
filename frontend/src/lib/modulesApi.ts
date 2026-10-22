import { api } from './api';
import type {
  AbonnementCantineProfil, ArretProfil, BusProfil, ConsultationProfil, ConversationProfil,
  EmpruntProfil, IncidentProfil, ItineraireProfil, JournalAuditProfil, LivreProfil, MenuCantineProfil,
  MessageProfil, NotificationProfil, ReunionInviteProfil, ReunionProfil, SanctionProfil, UrgenceProfil,
  VaccinProfil,
} from '@/types/modules';
import type { EleveProfil } from '@/types/eleve';
import type { PageDto } from '@/types/common';
import axios from 'axios';
import { useAuth } from '@/hooks/useAuth';
import { versCamelCase, versSnakeCase } from './caseConversion';

/** Client HTTP vers le notifications-service (port 8081, proxy /notif). */
export const notifApi = axios.create({
  baseURL: '/notif/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

notifApi.interceptors.request.use((config) => {
  const token = useAuth.getState().accessToken;
  if (token) config.headers.Authorization = `Bearer ${token}`;
  if (config.data && !(config.data instanceof FormData)) {
    config.data = versSnakeCase(config.data);
  }
  return config;
});

notifApi.interceptors.response.use((reponse) => {
  if (reponse.data) reponse.data = versCamelCase(reponse.data);
  return reponse;
});

export const bibliothequeApi = {
  livres: () => api.get<LivreProfil[]>('/livres'),
  creerLivre: (donnees: { titre: string; auteur?: string; editeur?: string; categorie?: string; isbn?: string; nombreExemplaires: number }) =>
    api.post<LivreProfil>('/livres', donnees),
  emprunter: (donnees: { livreId: number; utilisateurId: number; dateRetourPrevue: string }) =>
    api.post<EmpruntProfil>('/emprunts', donnees),
  retourner: (empruntId: number) => api.patch<EmpruntProfil>(`/emprunts/${empruntId}/retourner`),
  empruntsUtilisateur: (utilisateurId: number) => api.get<EmpruntProfil[]>(`/utilisateurs/${utilisateurId}/emprunts`),
};

export const disciplineApi = {
  creerIncident: (donnees: { eleveId: number; dateIncident: string; description: string }) =>
    api.post<IncidentProfil>('/incidents-disciplinaires', donnees),
  incidentsEleve: (eleveId: number) => api.get<IncidentProfil[]>(`/eleves/${eleveId}/incidents-disciplinaires`),
  creerSanction: (donnees: { eleveId: number; incidentId?: number; typeSanctionCode: string; dureeJours?: number; motif?: string }) =>
    api.post<SanctionProfil>('/sanctions', donnees),
  sanctionsEleve: (eleveId: number) => api.get<SanctionProfil[]>(`/eleves/${eleveId}/sanctions`),
};

export const infirmerieApi = {
  creerConsultation: (donnees: { eleveId: number; motif: string; diagnostic?: string; traitement?: string }) =>
    api.post<ConsultationProfil>('/consultations-medicales', donnees),
  consultations: (eleveId: number) => api.get<ConsultationProfil[]>(`/eleves/${eleveId}/consultations-medicales`),
  creerUrgence: (donnees: { eleveId: number; description: string; actionPrise?: string; hopitalTransfert?: string }) =>
    api.post<UrgenceProfil>('/urgences-medicales', donnees),
  urgences: (eleveId: number) => api.get<UrgenceProfil[]>(`/eleves/${eleveId}/urgences-medicales`),
  creerVaccin: (donnees: { eleveId: number; nomVaccin: string; dateAdministration: string; rappelPrevu?: string }) =>
    api.post<VaccinProfil>('/vaccins', donnees),
  vaccins: (eleveId: number) => api.get<VaccinProfil[]>(`/eleves/${eleveId}/vaccins`),
};

export const transportApi = {
  bus: () => api.get<BusProfil[]>('/bus'),
  creerBus: (donnees: { immatriculation: string; capacite: number; chauffeurId?: number }) =>
    api.post<BusProfil>('/bus', donnees),
  creerItineraire: (donnees: { busId: number; libelle: string; heureDepartMatin?: string; heureRetourSoir?: string }) =>
    api.post<ItineraireProfil>('/itineraires', donnees),
  arrets: (itineraireId: number) => api.get<ArretProfil[]>(`/itineraires/${itineraireId}/arrets`),
  creerArret: (itineraireId: number, donnees: { libelle: string; latitude?: number; longitude?: number; ordre: number }) =>
    api.post<ArretProfil>(`/itineraires/${itineraireId}/arrets`, donnees),
  affecterEleve: (eleveId: number, donnees: { itineraireId: number; arretId?: number }) =>
    api.post(`/eleves/${eleveId}/transport`, donnees),
};

export const cantineApi = {
  menus: () => api.get<MenuCantineProfil[]>('/menus-cantine'),
  creerMenu: (donnees: { dateJour: string; libellePlat: string; informationsNutritionnelles?: string }) =>
    api.post<MenuCantineProfil>('/menus-cantine', donnees),
  abonner: (eleveId: number, donnees: { periodeId: number; montant: number }) =>
    api.post<AbonnementCantineProfil>(`/eleves/${eleveId}/abonnement-cantine`, donnees),
  pointer: (donnees: { eleveId: number; dateJour: string; statut: string }) =>
    api.post('/presences-cantine', donnees),
};

export const reunionApi = {
  lister: () => api.get<ReunionProfil[]>('/reunions'),
  creer: (donnees: { titre: string; description?: string; dateReunion: string; lieu?: string; lienVisioconference?: string; invitesIds: number[] }) =>
    api.post<ReunionProfil>('/reunions', donnees),
  invites: (id: number) => api.get<ReunionInviteProfil[]>(`/reunions/${id}/invites`),
  repondre: (id: number, statutReponse: string) => api.patch(`/reunions/${id}/reponse`, { statutReponse }),
  compteRendu: (id: number, contenu: string, fichierUrl?: string) =>
    api.put(`/reunions/${id}/compte-rendu`, { contenu, fichierUrl }),
};

export const notificationClientApi = {
  lister: () => notifApi.get<NotificationProfil[]>('/notifications'),
  marquerLue: (id: number) => notifApi.patch<NotificationProfil>(`/notifications/${id}/lue`),
};

export const chatApi = {
  conversations: () => notifApi.get<ConversationProfil[]>('/conversations'),
  creer: (donnees: { titre?: string; participantIds: number[] }) =>
    notifApi.post<ConversationProfil>('/conversations', donnees),
  messages: (id: number) => notifApi.get<MessageProfil[]>(`/conversations/${id}/messages`),
  envoyer: (id: number, contenu: string) =>
    notifApi.post<MessageProfil>(`/conversations/${id}/messages`, { contenu, typeContenu: 'texte' }),
};

export const auditApi = {
  lister: (page = 1, taille = 50, tableCible?: string) =>
    api.get<PageDto<JournalAuditProfil>>('/journal-audit', { params: { page, taille, tableCible } }),
};

export const portailApi = {
  mesEnfants: () => api.get<EleveProfil[]>('/parents/moi/enfants'),
  monDossierEleve: () => api.get<EleveProfil>('/eleves/moi'),
};

export const cahierApi = {
  lister: (classeMatiereId: number) => api.get<import('@/types/pedagogie').CahierDeTexteProfil[]>(`/classes-matiere/${classeMatiereId}/cahier-de-texte`),
  creer: (donnees: { classeMatiereId: number; dateCours: string; chapitre?: string; lecon?: string; contenu?: string; exercices?: string }) =>
    api.post<import('@/types/pedagogie').CahierDeTexteProfil>('/cahier-de-texte', donnees),
};

export const devoirApi = {
  lister: (classeMatiereId: number) => api.get<import('@/types/pedagogie').DevoirProfil[]>(`/classes-matiere/${classeMatiereId}/devoirs`),
  creer: (donnees: { classeMatiereId: number; titre: string; description?: string; dateLimite: string; fichierSujetUrl?: string }) =>
    api.post<import('@/types/pedagogie').DevoirProfil>('/devoirs', donnees),
  rendus: (devoirId: number) => api.get<import('@/types/pedagogie').DevoirRenduProfil[]>(`/devoirs/${devoirId}/rendus`),
  rendre: (donnees: { devoirId: number; fichierReponseUrl?: string }) =>
    api.post<import('@/types/pedagogie').DevoirRenduProfil>('/devoirs/rendus', donnees),
  corriger: (renduId: number, donnees: { note?: number; correctionCommentaire?: string }) =>
    api.patch<import('@/types/pedagogie').DevoirRenduProfil>(`/devoirs/rendus/${renduId}/corriger`, donnees),
  rendusEleve: (eleveId: number) => api.get<import('@/types/pedagogie').DevoirRenduProfil[]>(`/eleves/${eleveId}/devoirs-rendus`),
};

export const personnelApi = {
  lister: () => api.get<import('@/types/pedagogie').PersonnelProfil[]>('/personnel'),
  creer: (donnees: { nom: string; prenom: string; email: string; telephone?: string; matricule?: string; roleCode: string; fonction?: string; departement?: string; dateEmbauche?: string; typeContrat?: string; salaireBase?: number }) =>
    api.post<{ profil: import('@/types/pedagogie').PersonnelProfil; motDePasseTemporaire: string }>('/personnel', donnees),
  demanderConge: (donnees: { typeConge: string; dateDebut: string; dateFin: string; motif?: string }) =>
    api.post<import('@/types/pedagogie').DemandeCongeProfil>('/personnel/conges', donnees),
  mesConges: () => api.get<import('@/types/pedagogie').DemandeCongeProfil[]>('/personnel/conges/moi'),
  congesEnAttente: () => api.get<import('@/types/pedagogie').DemandeCongeProfil[]>('/personnel/conges/en-attente'),
  validerConge: (congeId: number, statut: 'approuve' | 'refuse') =>
    api.patch<import('@/types/pedagogie').DemandeCongeProfil>(`/personnel/conges/${congeId}`, { statut }),
};

export const documentApi = {
  lister: (params?: { proprietaireType?: string; proprietaireId?: number }) =>
    api.get<import('@/types/pedagogie').DocumentProfil[]>('/documents', { params }),
  creer: (donnees: { proprietaireType: string; proprietaireId: number; typeDocument: string; titre: string; fichierUrl: string }) =>
    api.post<import('@/types/pedagogie').DocumentProfil>('/documents', donnees),
  obtenir: (id: number) => api.get<import('@/types/pedagogie').DocumentProfil>(`/documents/${id}`),
};

export const rapportApi = {
  synthese: () => api.get<import('@/types/pedagogie').RapportSynthese>('/rapports/synthese'),
  effectifs: () => api.get<import('@/types/pedagogie').RapportEffectifClasse[]>('/rapports/effectifs-classes'),
  soldes: () => api.get<import('@/types/pedagogie').RapportSoldeEleve[]>('/rapports/soldes-eleves'),
  presences: () => api.get<import('@/types/pedagogie').RapportTauxPresence[]>('/rapports/taux-presences'),
  moyennes: (classeId: number, periodeId: number) =>
    api.get<import('@/types/pedagogie').RapportMoyenneClasse[]>('/rapports/moyennes-classe', { params: { classeId, periodeId } }),
};
