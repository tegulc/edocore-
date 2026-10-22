import { api } from './api';
import type {
  BalanceComptable, CompteComptableProfil, EcheancierProfil, EcritureComptableProfil,
  PaiementProfil, StructureFraisProfil,
} from '@/types/paiement';

export const structureFraisApi = {
  creer: (donnees: { niveauId: number; libelle: string; montant: number; obligatoire: boolean }) =>
    api.post<StructureFraisProfil>('/structures-frais', donnees),
};

export const echeancierApi = {
  lister: (eleveId: number) => api.get<EcheancierProfil[]>(`/eleves/${eleveId}/echeanciers`),
  generer: (eleveId: number, donnees: { structureFraisId: number; nombreTranches: number; premiereEcheance: string }) =>
    api.post<EcheancierProfil>(`/eleves/${eleveId}/echeanciers`, donnees),
};

export const paiementApi = {
  /** Genere automatiquement une Idempotency-Key : un nouvel appel = une nouvelle intention de paiement. */
  initier: (donnees: { eleveId: number; trancheId?: number; montant: number; moyenPaiement: string; numeroTelephone?: string }) =>
    api.post<{ paiementId: number; statut: string; instructions: string }>('/paiements/initier', donnees, {
      headers: { 'Idempotency-Key': crypto.randomUUID() },
    }),

  encaisser: (donnees: { eleveId: number; trancheId?: number; montant: number; moyenPaiement: string }) =>
    api.post<PaiementProfil>('/paiements/encaisser', donnees),

  historique: (eleveId: number) => api.get<PaiementProfil[]>(`/paiements/eleves/${eleveId}`),
};

export const comptabiliteApi = {
  comptes: () => api.get<CompteComptableProfil[]>('/comptes-comptables'),
  creerCompte: (donnees: { code: string; libelle: string; typeCompte: string }) =>
    api.post<CompteComptableProfil>('/comptes-comptables', donnees),
  ecritures: (debut: string, fin: string) => api.get<EcritureComptableProfil[]>('/ecritures-comptables', { params: { debut, fin } }),
  enregistrerEcriture: (donnees: { compteId: number; dateEcriture: string; libelle: string; debit?: number; credit?: number }) =>
    api.post<EcritureComptableProfil>('/ecritures-comptables', donnees),
  balance: (debut: string, fin: string) => api.get<BalanceComptable[]>('/ecritures-comptables/balance', { params: { debut, fin } }),
};
