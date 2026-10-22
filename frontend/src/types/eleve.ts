export interface EleveProfil {
  id: number;
  uuid: string;
  matricule: string;
  nom: string;
  prenom: string;
  sexe: 'M' | 'F';
  dateNaissance: string;
  lieuNaissance: string | null;
  nationalite: string | null;
  groupeSanguin: string | null;
  allergies: string | null;
  photoUrl: string | null;
  qrCode: string;
  statut: 'actif' | 'transfere' | 'exclu' | 'diplome' | 'abandonne';
  dateInscription: string;
}

export interface InscriptionProfil {
  id: number;
  eleveId: number;
  anneeScolaireId: number;
  classeId: number;
  classeLibelle: string;
  typeInscription: string;
  statut: string;
}

export interface ParentProfil {
  id: number;
  utilisateur: { id: number; nom: string; prenom: string; email: string; telephone?: string };
  profession: string | null;
  adresse: string | null;
}

export interface EleveParentProfil {
  id: number;
  eleveId: number;
  parentId: number;
  parentNom: string;
  parentPrenom: string;
  parentTelephone: string | null;
  lienParente: string;
  estContactPrincipal: boolean;
  autoriseRecupererEleve: boolean;
}
