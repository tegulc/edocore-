export interface CreneauHoraireProfil { id: number; libelle: string; heureDebut: string; heureFin: string; ordre: number }

export interface SeanceProfil {
  id: number;
  classeId: number;
  matiereId: number;
  matiereLibelle: string;
  enseignantId: number;
  enseignantNom: string;
  salleId: number | null;
  creneauId: number;
  creneauLibelle: string | null;
  jourSemaine: number; // 1=lundi
}

export interface PresenceProfil {
  id: number;
  eleveId: number;
  classeMatiereId: number | null;
  dateJour: string;
  typePresence: 'present' | 'absent' | 'retard' | 'exclu_cours';
  justifie: boolean;
  motifJustification: string | null;
}

export interface BilanPresence { absencesNonJustifiees: number; retards: number }

export const JOURS_SEMAINE = ['', 'Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi', 'Samedi', 'Dimanche'];
