import { useEffect, useState, type FormEvent } from 'react';
import { Layers, Loader2 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { structureApi } from '@/lib/classeApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { MatiereProfil, NiveauProfil, SalleProfil, SerieProfil } from '@/types/classe';

type Onglet = 'niveaux' | 'series' | 'salles' | 'matieres';

export default function PageReferentiels() {
  const [onglet, setOnglet] = useState<Onglet>('niveaux');
  const [niveaux, setNiveaux] = useState<NiveauProfil[]>([]);
  const [series, setSeries] = useState<SerieProfil[]>([]);
  const [salles, setSalles] = useState<SalleProfil[]>([]);
  const [matieres, setMatieres] = useState<MatiereProfil[]>([]);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function toutCharger() {
    const [n, s, sa, m] = await Promise.all([
      structureApi.niveaux(), structureApi.series(), structureApi.salles(), structureApi.matieres(),
    ]);
    setNiveaux(n.data);
    setSeries(s.data);
    setSalles(sa.data);
    setMatieres(m.data);
  }

  useEffect(() => { toutCharger(); }, []);

  async function gererSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      if (onglet === 'niveaux') {
        await structureApi.creerNiveau({
          code: fd.get('code') as string,
          libelle: fd.get('libelle') as string,
          ordre: Number(fd.get('ordre')),
          cycle: (fd.get('cycle') as string) || undefined,
        });
      } else if (onglet === 'series') {
        await structureApi.creerSerie({ code: fd.get('code') as string, libelle: fd.get('libelle') as string });
      } else if (onglet === 'salles') {
        await structureApi.creerSalle({ nom: fd.get('nom') as string, capacite: Number(fd.get('capacite')) });
      } else {
        await structureApi.creerMatiere({
          code: fd.get('code') as string,
          libelle: fd.get('libelle') as string,
          couleur: (fd.get('couleur') as string) || undefined,
        });
      }
      setFormulaireOuvert(false);
      await toutCharger();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  const onglets: { id: Onglet; libelle: string }[] = [
    { id: 'niveaux', libelle: 'Niveaux' },
    { id: 'series', libelle: 'Séries' },
    { id: 'salles', libelle: 'Salles' },
    { id: 'matieres', libelle: 'Matières' },
  ];

  const donnees = { niveaux, series, salles, matieres }[onglet];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Référentiels</h1>
          <p className="text-sm text-ink-400 mt-1">Niveaux, séries, salles et matières.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)}><Layers size={16} /> Ajouter</Bouton>
      </div>

      <div className="flex gap-2 flex-wrap">
        {onglets.map(({ id, libelle }) => (
          <button key={id} onClick={() => { setOnglet(id); setFormulaireOuvert(false); }}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-theme ${onglet === id ? 'bg-brand-500 text-white' : 'bg-ink-50 dark:bg-white/5 text-ink-600 dark:text-ink-300 hover:bg-ink-100'}`}>
            {libelle}
          </button>
        ))}
      </div>

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererSubmit} className="grid sm:grid-cols-2 gap-4">
            {onglet === 'niveaux' && (
              <>
                <label className="block"><span className="text-sm font-medium">Code</span>
                  <input required name="code" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                <label className="block"><span className="text-sm font-medium">Libellé</span>
                  <input required name="libelle" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                <label className="block"><span className="text-sm font-medium">Ordre</span>
                  <input required type="number" name="ordre" defaultValue={1} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                <label className="block"><span className="text-sm font-medium">Cycle</span>
                  <input name="cycle" placeholder="college, lycee…" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
              </>
            )}
            {(onglet === 'series' || onglet === 'matieres') && (
              <>
                <label className="block"><span className="text-sm font-medium">Code</span>
                  <input required name="code" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                <label className="block"><span className="text-sm font-medium">Libellé</span>
                  <input required name="libelle" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                {onglet === 'matieres' && (
                  <label className="block sm:col-span-2"><span className="text-sm font-medium">Couleur</span>
                    <input name="couleur" type="color" defaultValue="#2F5DE3" className="mt-1.5 h-10 w-20 rounded-lg border border-ink-100" /></label>
                )}
              </>
            )}
            {onglet === 'salles' && (
              <>
                <label className="block"><span className="text-sm font-medium">Nom</span>
                  <input required name="nom" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                <label className="block"><span className="text-sm font-medium">Capacité</span>
                  <input required type="number" name="capacite" defaultValue={40} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
              </>
            )}
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Enregistrer</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr>
              {onglet === 'salles' ? (
                <><th className="px-4 py-3 font-medium">Nom</th><th className="px-4 py-3 font-medium">Capacité</th></>
              ) : (
                <><th className="px-4 py-3 font-medium">Code</th><th className="px-4 py-3 font-medium">Libellé</th></>
              )}
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {donnees.length === 0 && <tr><td colSpan={2} className="px-4 py-6 text-center text-ink-400">Aucune entrée.</td></tr>}
            {onglet === 'niveaux' && niveaux.map((n) => (
              <tr key={n.id}><td className="px-4 py-3 font-mono text-xs">{n.code}</td><td className="px-4 py-3">{n.libelle}</td></tr>
            ))}
            {onglet === 'series' && series.map((s) => (
              <tr key={s.id}><td className="px-4 py-3 font-mono text-xs">{s.code}</td><td className="px-4 py-3">{s.libelle}</td></tr>
            ))}
            {onglet === 'salles' && salles.map((s) => (
              <tr key={s.id}><td className="px-4 py-3">{s.nom}</td><td className="px-4 py-3">{s.capacite}</td></tr>
            ))}
            {onglet === 'matieres' && matieres.map((m) => (
              <tr key={m.id}>
                <td className="px-4 py-3 font-mono text-xs flex items-center gap-2">
                  <span className="h-3 w-3 rounded-full" style={{ backgroundColor: m.couleur }} />{m.code}
                </td>
                <td className="px-4 py-3">{m.libelle}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
