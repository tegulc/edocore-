export interface EtablissementProfil {
  id: number;
  nom: string;
  sigle: string | null;
  logoUrl: string | null;
  pays: string;
  ville: string | null;
  adresse: string | null;
  telephone: string | null;
  email: string | null;
  typeEtablissement: string | null;
  statut: 'ACTIF' | 'SUSPENDU' | 'RESILIE';
  planAbonnement: string;
  devise: string;
  langueDefaut: string;
  fuseauHoraire: string;
}

export interface AnneeScolaireProfil {
  id: number;
  libelle: string;
  dateDebut: string;
  dateFin: string;
  estActive: boolean;
}

export interface PeriodeProfil {
  id: number;
  anneeScolaireId: number;
  typePeriode: 'trimestre' | 'sequence' | 'semestre';
  numero: number;
  libelle: string;
  dateDebut: string;
  dateFin: string;
  estCloturee: boolean;
}

export interface ModuleActifProfil {
  moduleCode: string;
  actif: boolean;
}

export interface JourFerieProfil {
  id: number;
  libelle: string;
  dateJour: string;
  recurrentAnnuel: boolean;
}
