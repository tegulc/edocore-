export interface NiveauProfil { id: number; code: string; libelle: string; ordre: number; cycle: string | null }
export interface SerieProfil { id: number; code: string; libelle: string }
export interface SalleProfil { id: number; nom: string; capacite: number; typeSalle: string }
export interface MatiereProfil { id: number; code: string; libelle: string; couleur: string }

export interface ClasseProfil {
  id: number;
  anneeScolaireId: number;
  niveauId: number;
  niveauCode: string | null;
  serieId: number | null;
  serieCode: string | null;
  libelle: string;
  salleId: number | null;
  professeurPrincipalId: number | null;
  capacite: number;
  effectif: number;
}

export interface ClasseMatiereProfil {
  id: number;
  classeId: number;
  matiereId: number;
  matiereLibelle: string;
  enseignantId: number | null;
  coefficient: number;
  volumeHoraireHebdo: number;
}

export interface EnseignantProfil {
  id: number;
  utilisateur: { id: number; nom: string; prenom: string; email: string };
  matricule: string | null;
  specialite: string | null;
  diplomePrincipal: string | null;
  dateEmbauche: string | null;
  typeContrat: string | null;
  statut: string;
}
