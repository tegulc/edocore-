import { useEffect, useState, type FormEvent } from 'react';
import { Plus, Loader2, Users } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { classeApi, structureApi } from '@/lib/classeApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { ClasseProfil, NiveauProfil, SerieProfil } from '@/types/classe';

export default function PageClasses() {
  const [classes, setClasses] = useState<ClasseProfil[]>([]);
  const [niveaux, setNiveaux] = useState<NiveauProfil[]>([]);
  const [series, setSeries] = useState<SerieProfil[]>([]);
  const [chargement, setChargement] = useState(true);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [formulaire, setFormulaire] = useState({ niveauId: '', serieId: '', libelle: '', capacite: '60' });
  const [erreur, setErreur] = useState<string | null>(null);
  const [enCours, setEnCours] = useState(false);

  async function toutCharger() {
    setChargement(true);
    try {
      const [c, n, s] = await Promise.all([classeApi.lister(), structureApi.niveaux(), structureApi.series()]);
      setClasses(c.data);
      setNiveaux(n.data);
      setSeries(s.data);
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => { toutCharger(); }, []);

  async function gererCreation(e: FormEvent) {
    e.preventDefault();
    if (!formulaire.niveauId || !formulaire.libelle) return;
    setEnCours(true);
    setErreur(null);
    try {
      await classeApi.creer({
        niveauId: Number(formulaire.niveauId),
        serieId: formulaire.serieId ? Number(formulaire.serieId) : undefined,
        libelle: formulaire.libelle,
        capacite: Number(formulaire.capacite),
      });
      setFormulaire({ niveauId: '', serieId: '', libelle: '', capacite: '60' });
      setFormulaireOuvert(false);
      await toutCharger();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Classes</h1>
          <p className="text-sm text-ink-400 mt-1">Classes de l'année scolaire active.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)}><Plus size={16} /> Nouvelle classe</Bouton>
      </div>

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererCreation} className="grid sm:grid-cols-4 gap-3">
            <select required value={formulaire.niveauId} onChange={(e) => setFormulaire({ ...formulaire, niveauId: e.target.value })}
              className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3 py-2 text-sm">
              <option value="">Niveau…</option>
              {niveaux.map((n) => <option key={n.id} value={n.id}>{n.libelle}</option>)}
            </select>
            <select value={formulaire.serieId} onChange={(e) => setFormulaire({ ...formulaire, serieId: e.target.value })}
              className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3 py-2 text-sm">
              <option value="">Série (optionnel)…</option>
              {series.map((s) => <option key={s.id} value={s.id}>{s.libelle}</option>)}
            </select>
            <input required placeholder="Libellé (ex: 3ème A1)" value={formulaire.libelle}
              onChange={(e) => setFormulaire({ ...formulaire, libelle: e.target.value })}
              className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3 py-2 text-sm" />
            <input type="number" placeholder="Capacité" value={formulaire.capacite}
              onChange={(e) => setFormulaire({ ...formulaire, capacite: e.target.value })}
              className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3 py-2 text-sm" />
            {erreur && <p className="sm:col-span-4 text-sm text-red-600 bg-red-50 dark:bg-red-500/10 rounded-lg px-3 py-2">{erreur}</p>}
            <Bouton type="submit" disabled={enCours} className="sm:col-span-4 w-fit">
              {enCours && <Loader2 size={16} className="animate-spin" />} Créer
            </Bouton>
          </form>
        </Card>
      )}

      <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">
        {chargement && <p className="text-sm text-ink-400">Chargement…</p>}
        {!chargement && classes.length === 0 && <p className="text-sm text-ink-400">Aucune classe pour l'année active.</p>}
        {classes.map((c) => (
          <Card key={c.id}>
            <div className="flex items-center justify-between">
              <span className="font-display font-semibold">{c.libelle}</span>
              <span className="text-xs text-ink-400">{c.niveauCode}{c.serieCode ? ` · ${c.serieCode}` : ''}</span>
            </div>
            <div className="flex items-center gap-1.5 mt-3 text-sm text-ink-400">
              <Users size={14} /> {c.effectif} / {c.capacite} élèves
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
