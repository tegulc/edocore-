export interface StructureFraisProfil { id: number; niveauId: number; libelle: string; montant: number; obligatoire: boolean }

export interface TrancheProfil {
  id: number;
  echeancierId: number;
  numeroTranche: number;
  montantDu: number;
  montantPaye: number;
  dateEcheance: string;
  statut: 'impaye' | 'partiel' | 'paye' | 'en_retard';
}

export interface EcheancierProfil {
  id: number;
  eleveId: number;
  structureFraisId: number;
  montantTotal: number;
  nombreTranches: number;
  tranches: TrancheProfil[];
}

export interface PaiementProfil {
  id: number;
  uuid: string;
  eleveId: number;
  trancheId: number | null;
  moyenPaiement: string;
  montant: number;
  referenceTransaction: string | null;
  statut: 'en_attente' | 'confirme' | 'echoue' | 'rembourse';
  datePaiement: string;
}

export interface CompteComptableProfil { id: number; code: string; libelle: string; typeCompte: string }

export interface EcritureComptableProfil {
  id: number;
  compteId: number;
  compteCode: string;
  dateEcriture: string;
  libelle: string;
  debit: number;
  credit: number;
  referenceSource: string | null;
}

export interface BalanceComptable { compteCode: string; compteLibelle: string; totalDebit: number; totalCredit: number; solde: number }
