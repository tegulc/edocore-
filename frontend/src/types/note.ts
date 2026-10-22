export interface EvaluationProfil {
  id: number;
  classeMatiereId: number;
  typeEvaluationCode: string;
  periodeId: number;
  titre: string;
  dateEvaluation: string;
  bareme: number;
  statut: 'planifiee' | 'saisie' | 'validee' | 'publiee';
}

export interface NoteProfil {
  id: number;
  evaluationId: number;
  eleveId: number;
  eleveNom: string;
  valeur: number | null;
  absent: boolean;
  observation: string | null;
  valide: boolean;
}

export interface MoyenneGeneraleProfil {
  eleveId: number;
  eleveNom: string;
  moyenneGenerale: number | null;
  rangClasse: number | null;
  decision: string | null;
}

export interface MoyenneMatiere {
  classeMatiereId: number;
  matiereLibelle: string;
  moyenne: number | null;
  rangMatiere: number | null;
  enseignantNom: string;
  coefficient: number;
  volumeHoraireHebdo: number;
  maxClasse: number | null;
  minClasse: number | null;
  moyenneClasse: number | null;
  appreciation: string;
}

export interface BulletinProfil {
  id: number;
  eleveId: number;
  eleveNom: string;
  classeId: number;
  periodeId: number;
  moyenneGenerale: number | null;
  rang: number | null;
  appreciationGenerale: string | null;
  decisionConseil: string | null;
  nombreAbsences: number;
  statut: 'brouillon' | 'valide' | 'publie';
  moyennesParMatiere: MoyenneMatiere[];
  moyenneClasse: number | null;
  maxClasse: number | null;
  minClasse: number | null;
  effectif: number;
  dateConseil: string | null;
  recompenses: string | null;
  avertissementTravail: boolean | null;
  avertissementConduite: boolean | null;
  blame: boolean | null;
  exclusion: boolean | null;
  observationConseil: string | null;
  decisionFinale: string | null;
  classeOrientation: string | null;
  valideParId: number | null;
  dateValidation: string | null;
}

export interface ConseilClasse {
  dateConseil: string;
  recompenses: string;
  avertissementTravail: boolean;
  avertissementConduite: boolean;
  blame: boolean;
  exclusion: boolean;
  observationConseil: string;
  decisionFinale: string;
  classeOrientation: string;
}
