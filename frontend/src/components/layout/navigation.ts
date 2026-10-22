import {
  LayoutDashboard, Users, GraduationCap, BookOpen, CalendarClock,
  ClipboardCheck, Wallet, Library, MessageSquare, Bell, ShieldCheck, Building2,
  FileText, Award, Layers, Calculator, HeartPulse, Bus, UtensilsCrossed,
  Video, ShieldAlert, UserCircle, BookMarked, ClipboardList, FolderOpen, BarChart3, Briefcase,
} from 'lucide-react';
import type { RoleCode } from '@/types/auth';
import type { LucideIcon } from 'lucide-react';

export interface ElementNav {
  libelle: string;
  chemin: string;
  icone: LucideIcon;
  /** Roles autorises a voir cet element. Vide = visible par tous les roles connectes. */
  roles?: RoleCode[];
}

/**
 * Source unique de verite pour la barre laterale. Chaque module ajoute son
 * entree ici au fur et a mesure de sa livraison (Phase 1, 2, 3...).
 */
export const NAVIGATION: ElementNav[] = [
  { libelle: 'Tableau de bord', chemin: '/', icone: LayoutDashboard, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'COMPTABLE', 'ECONOME', 'SECRETAIRE', 'PROFESSEUR', 'SURVEILLANT_GENERAL'] },
  { libelle: 'Mon espace', chemin: '/mon-espace', icone: UserCircle, roles: ['PARENT', 'ELEVE'] },
  { libelle: 'Etablissement', chemin: '/etablissement', icone: Building2, roles: ['ADMIN', 'DIRECTEUR'] },
  { libelle: 'Utilisateurs', chemin: '/utilisateurs', icone: Users, roles: ['ADMIN', 'DIRECTEUR'] },
  { libelle: 'Eleves', chemin: '/eleves', icone: GraduationCap, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'SECRETAIRE', 'PROFESSEUR', 'SURVEILLANT_GENERAL'] },
  { libelle: 'Parents', chemin: '/parents', icone: Users, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'SECRETAIRE'] },
  { libelle: 'Enseignants', chemin: '/enseignants', icone: Users, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR'] },
  { libelle: 'Classes & matieres', chemin: '/classes', icone: BookOpen, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'SECRETAIRE', 'PROFESSEUR'] },
  { libelle: 'Referentiels', chemin: '/referentiels', icone: Layers, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR'] },
  { libelle: 'Emploi du temps', chemin: '/emploi-du-temps', icone: CalendarClock },
  { libelle: 'Presences', chemin: '/presences', icone: ClipboardCheck, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'SURVEILLANT_GENERAL', 'PROFESSEUR'] },
  { libelle: 'Notes', chemin: '/notes', icone: FileText, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR'] },
  { libelle: 'Bulletins', chemin: '/bulletins', icone: Award, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR'] },
  { libelle: 'Cahier de texte', chemin: '/cahier-de-texte', icone: BookMarked, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR', 'PARENT', 'ELEVE'] },
  { libelle: 'Devoirs', chemin: '/devoirs', icone: ClipboardList, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR', 'ELEVE'] },
  { libelle: 'Paiements', chemin: '/paiements', icone: Wallet, roles: ['ADMIN', 'DIRECTEUR', 'COMPTABLE', 'ECONOME', 'SECRETAIRE'] },
  { libelle: 'Comptabilite', chemin: '/comptabilite', icone: Calculator, roles: ['ADMIN', 'DIRECTEUR', 'COMPTABLE', 'ECONOME'] },
  { libelle: 'Bibliotheque', chemin: '/bibliotheque', icone: Library, roles: ['ADMIN', 'DIRECTEUR', 'BIBLIOTHECAIRE', 'PROFESSEUR', 'ELEVE'] },
  { libelle: 'Discipline', chemin: '/discipline', icone: ShieldAlert, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'SURVEILLANT_GENERAL'] },
  { libelle: 'Infirmerie', chemin: '/infirmerie', icone: HeartPulse, roles: ['ADMIN', 'DIRECTEUR', 'INFIRMIER'] },
  { libelle: 'Transport', chemin: '/transport', icone: Bus, roles: ['ADMIN', 'DIRECTEUR', 'ECONOME', 'CHAUFFEUR'] },
  { libelle: 'Cantine', chemin: '/cantine', icone: UtensilsCrossed, roles: ['ADMIN', 'DIRECTEUR', 'ECONOME'] },
  { libelle: 'Personnel', chemin: '/personnel', icone: Briefcase, roles: ['ADMIN', 'DIRECTEUR'] },
  { libelle: 'Documents', chemin: '/documents', icone: FolderOpen, roles: ['ADMIN', 'DIRECTEUR', 'SECRETAIRE', 'CENSEUR'] },
  { libelle: 'Rapports', chemin: '/rapports', icone: BarChart3, roles: ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'COMPTABLE', 'ECONOME'] },
  { libelle: 'Reunions', chemin: '/reunions', icone: Video },
  { libelle: 'Messagerie', chemin: '/messagerie', icone: MessageSquare },
  { libelle: 'Notifications', chemin: '/notifications', icone: Bell },
  { libelle: 'Securite & audit', chemin: '/securite', icone: ShieldCheck, roles: ['ADMIN', 'DIRECTEUR'] },
];
