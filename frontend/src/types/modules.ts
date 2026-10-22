export interface LivreProfil {
  id: number;
  isbn: string | null;
  titre: string;
  auteur: string | null;
  editeur: string | null;
  categorie: string | null;
  nombreExemplaires: number;
  exemplairesDisponibles: number;
  qrCode: string | null;
}

export interface EmpruntProfil {
  id: number;
  livreId: number;
  utilisateurId: number;
  dateEmprunt: string;
  dateRetourPrevue: string;
  dateRetourEffective: string | null;
  statut: string;
}

export interface IncidentProfil {
  id: number;
  eleveId: number;
  dateIncident: string;
  description: string;
}

export interface SanctionProfil {
  id: number;
  eleveId: number;
  incidentId: number | null;
  typeSanctionCode: string;
  typeSanctionLibelle: string;
  dateSanction: string;
  dureeJours: number | null;
  motif: string | null;
}

export interface ConsultationProfil {
  id: number;
  eleveId: number;
  dateConsultation: string;
  motif: string;
  diagnostic: string | null;
  traitement: string | null;
}

export interface UrgenceProfil {
  id: number;
  eleveId: number;
  dateUrgence: string;
  description: string;
  actionPrise: string | null;
  hopitalTransfert: string | null;
  parentNotifie: boolean;
}

export interface VaccinProfil {
  id: number;
  eleveId: number;
  nomVaccin: string;
  dateAdministration: string;
  rappelPrevu: string | null;
}

export interface BusProfil {
  id: number;
  immatriculation: string;
  capacite: number;
  chauffeurId: number | null;
  statut: string;
}

export interface ItineraireProfil {
  id: number;
  busId: number;
  libelle: string;
  heureDepartMatin: string | null;
  heureRetourSoir: string | null;
}

export interface ArretProfil {
  id: number;
  itineraireId: number;
  libelle: string;
  latitude: number | null;
  longitude: number | null;
  ordre: number;
}

export interface MenuCantineProfil {
  id: number;
  dateJour: string;
  libellePlat: string;
  informationsNutritionnelles: string | null;
}

export interface AbonnementCantineProfil {
  id: number;
  eleveId: number;
  periodeId: number;
  montant: number;
  statutPaiement: string;
}

export interface ReunionProfil {
  id: number;
  titre: string;
  description: string | null;
  dateReunion: string;
  lieu: string | null;
  lienVisioconference: string | null;
  organisateurId: number;
}

export interface ReunionInviteProfil {
  utilisateurId: number;
  statutReponse: string | null;
  presentEffectif: boolean | null;
}

export interface NotificationProfil {
  id: number;
  destinataireId: number;
  categorie: string;
  titre: string;
  contenu: string | null;
  canal: string;
  statutEnvoi: string;
  lue: boolean;
}

export interface ConversationProfil {
  id: number;
  typeConversation: string;
  titre: string | null;
  participantIds: number[];
}

export interface MessageProfil {
  id: number;
  conversationId: number;
  expediteurId: number;
  contenu: string;
  typeContenu: string | null;
  fichierUrl: string | null;
  lu: boolean;
  envoyeLe: string;
}

export interface JournalAuditProfil {
  id: number;
  utilisateurId: number | null;
  action: string;
  tableCible: string | null;
  enregistrementId: number | null;
  adresseIp: string | null;
  horodatage: string;
}
