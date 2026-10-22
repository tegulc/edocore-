export interface CahierDeTexteProfil {
  id: number;
  classeMatiereId: number;
  dateCours: string;
  chapitre: string | null;
  lecon: string | null;
  contenu: string | null;
  exercices: string | null;
  createdAt: string;
}

export interface DevoirProfil {
  id: number;
  classeMatiereId: number;
  titre: string;
  description: string | null;
  datePublication: string;
  dateLimite: string;
  fichierSujetUrl: string | null;
  createdAt: string;
}

export interface DevoirRenduProfil {
  id: number;
  devoirId: number;
  eleveId: number;
  fichierReponseUrl: string | null;
  dateSoumission: string;
  note: number | null;
  correctionCommentaire: string | null;
  statut: string;
}

export interface PersonnelProfil {
  id: number;
  utilisateurId: number;
  matricule: string | null;
  nom: string | null;
  prenom: string | null;
  email: string | null;
  fonction: string | null;
  departement: string | null;
  dateEmbauche: string | null;
  typeContrat: string | null;
  salaireBase: number | null;
  statut: string;
}

export interface DemandeCongeProfil {
  id: number;
  utilisateurId: number;
  typeConge: string;
  dateDebut: string;
  dateFin: string;
  motif: string | null;
  statut: string;
  createdAt: string;
}

export interface DocumentProfil {
  id: number;
  proprietaireType: string;
  proprietaireId: number;
  typeDocument: string;
  titre: string;
  fichierUrl: string;
  archive: boolean | null;
  dateAjout: string;
}

export interface RapportEffectifClasse {
  classeId: number;
  libelle: string;
  effectif: number;
  capacite: number;
}

export interface RapportSoldeEleve {
  eleveId: number;
  nom: string;
  prenom: string;
  totalDu: number;
  totalPaye: number;
  soldeRestant: number;
}

export interface RapportTauxPresence {
  eleveId: number;
  nom: string;
  prenom: string;
  joursPresents: number;
  joursAbsents: number;
  tauxPresence: number;
}

export interface RapportMoyenneClasse {
  eleveId: number;
  nom: string;
  prenom: string;
  moyenne: number | null;
  rang: number | null;
}

export interface RapportSynthese {
  elevesActifs: number;
  tauxPresenceMoyen: number;
  recettesMois: number;
  incidentsDisciplinaires: number;
  congesEnAttente: number;
}
