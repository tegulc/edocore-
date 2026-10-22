import { useEffect, useState } from 'react';
import { Building2, CalendarRange, ToggleLeft, Loader2, Plus, Lock } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { etablissementApi, anneeScolaireApi } from '@/lib/etablissementApi';
import { useEtablissement } from '@/hooks/useEtablissement';
import type { AnneeScolaireProfil, ModuleActifProfil, PeriodeProfil } from '@/types/etablissement';

const LIBELLES_MODULES: Record<string, string> = {
  ELEVES: 'Elèves', PARENTS: 'Parents', ENSEIGNANTS: 'Enseignants', CLASSES: 'Classes & matières',
  EMPLOI_DU_TEMPS: 'Emploi du temps', PRESENCE: 'Présences', NOTES: 'Notes', BULLETINS: 'Bulletins',
  CAHIER_TEXTE: 'Cahier de texte', DEVOIRS: 'Devoirs',
  DISCIPLINE: 'Discipline', PAIEMENTS: 'Paiements', COMPTABILITE: 'Comptabilité', BIBLIOTHEQUE: 'Bibliothèque',
  INFIRMERIE: 'Infirmerie', TRANSPORT: 'Transport', CANTINE: 'Cantine', PERSONNEL: 'Personnel',
  DOCUMENTS: 'Documents', CHAT: 'Messagerie', REUNIONS: 'Réunions', NOTIFICATIONS: 'Notifications',
  IA: 'Intelligence artificielle', DASHBOARD: 'Tableaux de bord avancés', RAPPORTS: 'Rapports',
};

export default function PageEtablissement() {
  const { etablissement, definir } = useEtablissement();
  const [annees, setAnnees] = useState<AnneeScolaireProfil[]>([]);
  const [periodesParAnnee, setPeriodesParAnnee] = useState<Record<number, PeriodeProfil[]>>({});
  const [modules, setModules] = useState<ModuleActifProfil[]>([]);
  const [chargement, setChargement] = useState(true);
  const [formulaireAnneeOuvert, setFormulaireAnneeOuvert] = useState(false);
  const [nouvelleAnnee, setNouvelleAnnee] = useState({ libelle: '', dateDebut: '', dateFin: '' });

  async function toutCharger() {
    setChargement(true);
    try {
      const [profil, listeAnnees, listeModules] = await Promise.all([
        etablissementApi.moi(),
        anneeScolaireApi.lister(),
        etablissementApi.modules(),
      ]);
      definir(profil.data);
      setAnnees(listeAnnees.data);
      setModules(listeModules.data);

      const entrees = await Promise.all(listeAnnees.data.map((a) => anneeScolaireApi.periodes(a.id).then((r) => [a.id, r.data] as const)));
      setPeriodesParAnnee(Object.fromEntries(entrees));
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => { toutCharger(); }, []); // eslint-disable-line react-hooks/exhaustive-deps

  async function activerAnnee(id: number) {
    await anneeScolaireApi.activer(id);
    await toutCharger();
  }

  async function creerAnnee() {
    if (!nouvelleAnnee.libelle || !nouvelleAnnee.dateDebut || !nouvelleAnnee.dateFin) return;
    await anneeScolaireApi.creer(nouvelleAnnee);
    setNouvelleAnnee({ libelle: '', dateDebut: '', dateFin: '' });
    setFormulaireAnneeOuvert(false);
    await toutCharger();
  }

  async function basculerModule(code: string, actif: boolean) {
    setModules((m) => m.map((mod) => (mod.moduleCode === code ? { ...mod, actif } : mod)));
    await etablissementApi.definirModule(code, actif);
  }

  if (chargement && !etablissement) {
    return <div className="flex justify-center py-16"><Loader2 className="animate-spin text-brand-500" size={24} /></div>;
  }

  return (
    <div className="space-y-6 max-w-4xl">
      <div>
        <h1 className="font-display text-2xl font-bold">Établissement</h1>
        <p className="text-sm text-ink-400 mt-1">Informations générales, années scolaires et modules activés.</p>
      </div>

      {/* Profil de l'établissement */}
      <Card>
        <div className="flex items-center gap-2 mb-4">
          <Building2 size={18} className="text-brand-500" />
          <h2 className="font-display font-semibold">Informations générales</h2>
        </div>
        {etablissement && (
          <div className="grid sm:grid-cols-2 gap-4 text-sm">
            <div><span className="text-ink-400">Nom</span><p className="font-medium">{etablissement.nom}</p></div>
            <div><span className="text-ink-400">Pays</span><p className="font-medium">{etablissement.pays}</p></div>
            <div><span className="text-ink-400">Ville</span><p className="font-medium">{etablissement.ville ?? '—'}</p></div>
            <div><span className="text-ink-400">Type</span><p className="font-medium">{etablissement.typeEtablissement ?? '—'}</p></div>
            <div><span className="text-ink-400">Plan</span><p className="font-medium capitalize">{etablissement.planAbonnement}</p></div>
            <div><span className="text-ink-400">Devise</span><p className="font-medium">{etablissement.devise}</p></div>
          </div>
        )}
      </Card>

      {/* Années scolaires */}
      <Card>
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <CalendarRange size={18} className="text-brand-500" />
            <h2 className="font-display font-semibold">Années scolaires</h2>
          </div>
          <Bouton variante="discret" onClick={() => setFormulaireAnneeOuvert((v) => !v)}>
            <Plus size={16} /> Nouvelle année
          </Bouton>
        </div>

        {formulaireAnneeOuvert && (
          <div className="grid sm:grid-cols-4 gap-3 mb-4 p-3 rounded-lg bg-ink-50 dark:bg-white/5">
            <input placeholder="Libellé (2026-2027)" value={nouvelleAnnee.libelle}
              onChange={(e) => setNouvelleAnnee({ ...nouvelleAnnee, libelle: e.target.value })}
              className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3 py-2 text-sm" />
            <input type="date" value={nouvelleAnnee.dateDebut}
              onChange={(e) => setNouvelleAnnee({ ...nouvelleAnnee, dateDebut: e.target.value })}
              className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3 py-2 text-sm" />
            <input type="date" value={nouvelleAnnee.dateFin}
              onChange={(e) => setNouvelleAnnee({ ...nouvelleAnnee, dateFin: e.target.value })}
              className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3 py-2 text-sm" />
            <Bouton onClick={creerAnnee}>Créer</Bouton>
          </div>
        )}

        <div className="space-y-3">
          {annees.map((annee) => (
            <div key={annee.id} className="border border-ink-100 dark:border-white/10 rounded-lg p-3">
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span className="font-medium text-sm">{annee.libelle}</span>
                  <span className="text-xs text-ink-400">{annee.dateDebut} → {annee.dateFin}</span>
                  {annee.estActive && (
                    <span className="text-xs font-medium px-2 py-0.5 rounded-full bg-emerald-50 dark:bg-emerald-500/15 text-emerald-700 dark:text-emerald-400">
                      Active
                    </span>
                  )}
                </div>
                {!annee.estActive && (
                  <Bouton variante="discret" onClick={() => activerAnnee(annee.id)}>Activer</Bouton>
                )}
              </div>
              {(periodesParAnnee[annee.id]?.length ?? 0) > 0 && (
                <div className="mt-2 flex flex-wrap gap-2">
                  {periodesParAnnee[annee.id].map((p) => (
                    <span key={p.id} className="inline-flex items-center gap-1 text-xs px-2 py-1 rounded-md bg-ink-50 dark:bg-white/5">
                      {p.estCloturee && <Lock size={11} />} {p.libelle}
                    </span>
                  ))}
                </div>
              )}
            </div>
          ))}
        </div>
      </Card>

      {/* Modules actifs */}
      <Card>
        <div className="flex items-center gap-2 mb-4">
          <ToggleLeft size={18} className="text-brand-500" />
          <h2 className="font-display font-semibold">Modules actifs</h2>
        </div>
        <p className="text-sm text-ink-400 mb-4">
          Activez uniquement les modules dont votre établissement a besoin — déploiement progressif, sans surcharger l'interface.
        </p>
        <div className="grid sm:grid-cols-2 gap-2">
          {modules.map((m) => (
            <label key={m.moduleCode} className="flex items-center justify-between gap-3 px-3 py-2.5 rounded-lg border border-ink-100 dark:border-white/10 text-sm cursor-pointer">
              <span>{LIBELLES_MODULES[m.moduleCode] ?? m.moduleCode}</span>
              <input
                type="checkbox"
                checked={m.actif}
                onChange={(e) => basculerModule(m.moduleCode, e.target.checked)}
                className="h-4 w-4 accent-brand-500"
              />
            </label>
          ))}
        </div>
      </Card>
    </div>
  );
}
