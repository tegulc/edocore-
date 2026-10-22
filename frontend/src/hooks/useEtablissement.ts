import { create } from 'zustand';
import type { EtablissementProfil } from '@/types/etablissement';

interface EtablissementState {
  etablissement: EtablissementProfil | null;
  definir: (etablissement: EtablissementProfil) => void;
}

/**
 * Etat global de l'etablissement courant. Remplace le nom d'etablissement
 * code en dur dans la Navbar (Phase 0) par la vraie donnee, chargee une fois
 * apres connexion (voir AppShell).
 */
export const useEtablissement = create<EtablissementState>((set) => ({
  etablissement: null,
  definir: (etablissement) => set({ etablissement }),
}));
