/** Correspond exactement a UtilisateurDto.Profil du backend (com.educore.modules.auth.dto). */
export interface UtilisateurProfil {
  id: number;
  etablissementId: number;
  /** Role par defaut (contexte JWT/UI). */
  rolePrincipal: RoleCode;
  /** Ensemble complet des roles attribues (cahier des charges Module 1 - support multi-role). */
  roles: RoleCode[];
  matricule?: string;
  nom: string;
  prenom: string;
  email: string;
  telephone?: string;
  photoUrl?: string;
  languePreferee: 'fr' | 'en';
  deuxFaActive: boolean;
  doitChangerMotDePasse: boolean;
}

export type RoleCode =
  | 'ADMIN' | 'DIRECTEUR' | 'CENSEUR' | 'SURVEILLANT_GENERAL' | 'ECONOME'
  | 'COMPTABLE' | 'SECRETAIRE' | 'PROFESSEUR' | 'ELEVE' | 'PARENT'
  | 'BIBLIOTHECAIRE' | 'INFIRMIER' | 'CHAUFFEUR' | 'PERSONNEL_ADMIN' | 'SECURITE';

/** Vrai si l'utilisateur possede au moins un des roles fournis (verification cote UI uniquement). */
export function possedeUnRole(utilisateur: UtilisateurProfil | null, roles: RoleCode[]): boolean {
  if (!utilisateur) return false;
  return utilisateur.roles.some((r) => roles.includes(r));
}
