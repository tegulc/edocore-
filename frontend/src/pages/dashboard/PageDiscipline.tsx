import { useState, type FormEvent } from 'react';
import { ShieldAlert, Loader2 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { disciplineApi } from '@/lib/modulesApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import type { IncidentProfil, SanctionProfil } from '@/types/modules';

export default function PageDiscipline() {
  const { eleves, chargement } = useSelecteursPedagogiques(true);
  const [eleveId, setEleveId] = useState('');
  const [incidents, setIncidents] = useState<IncidentProfil[]>([]);
  const [sanctions, setSanctions] = useState<SanctionProfil[]>([]);
  const [onglet, setOnglet] = useState<'incident' | 'sanction'>('incident');
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function chargerDonnees(id: string) {
    if (!id) return;
    const [inc, san] = await Promise.all([
      disciplineApi.incidentsEleve(Number(id)),
      disciplineApi.sanctionsEleve(Number(id)),
    ]);
    setIncidents(inc.data);
    setSanctions(san.data);
  }

  async function gererSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!eleveId) return;
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      if (onglet === 'incident') {
        await disciplineApi.creerIncident({
          eleveId: Number(eleveId),
          dateIncident: fd.get('dateIncident') as string,
          description: fd.get('description') as string,
        });
      } else {
        await disciplineApi.creerSanction({
          eleveId: Number(eleveId),
          typeSanctionCode: fd.get('typeSanctionCode') as string,
          dureeJours: Number(fd.get('dureeJours')) || undefined,
          motif: (fd.get('motif') as string) || undefined,
        });
      }
      await chargerDonnees(eleveId);
      e.currentTarget.reset();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Discipline</h1>
        <p className="text-sm text-ink-400 mt-1">Incidents et sanctions disciplinaires.</p>
      </div>

      <Card>
        <label className="block max-w-md">
          <span className="text-sm font-medium">Élève</span>
          <select value={eleveId} onChange={(e) => { setEleveId(e.target.value); chargerDonnees(e.target.value); }}
            className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
            <option value="">{chargement ? 'Chargement…' : 'Choisir un élève…'}</option>
            {eleves.map((el) => <option key={el.id} value={el.id}>{el.prenom} {el.nom}</option>)}
          </select>
        </label>
      </Card>

      {eleveId && (
        <>
          <div className="flex gap-2">
            {(['incident', 'sanction'] as const).map((o) => (
              <button key={o} onClick={() => setOnglet(o)}
                className={`px-4 py-2 rounded-lg text-sm font-medium ${onglet === o ? 'bg-brand-500 text-white' : 'bg-ink-50 dark:bg-white/5'}`}>
                {o === 'incident' ? 'Nouvel incident' : 'Nouvelle sanction'}
              </button>
            ))}
          </div>

          <Card>
            <form onSubmit={gererSubmit} className="grid sm:grid-cols-2 gap-4">
              {onglet === 'incident' ? (
                <>
                  <label className="block"><span className="text-sm font-medium">Date</span>
                    <input required type="date" name="dateIncident" defaultValue={new Date().toISOString().slice(0, 10)}
                      className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                  <label className="block sm:col-span-2"><span className="text-sm font-medium">Description</span>
                    <textarea required name="description" rows={3} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                </>
              ) : (
                <>
                  <label className="block"><span className="text-sm font-medium">Type de sanction</span>
                    <select required name="typeSanctionCode" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                      <option value="avertissement">Avertissement</option>
                      <option value="blame">Blâme</option>
                      <option value="exclusion_temporaire">Exclusion temporaire</option>
                      <option value="exclusion_definitive">Exclusion définitive</option>
                    </select></label>
                  <label className="block"><span className="text-sm font-medium">Durée (jours)</span>
                    <input type="number" name="dureeJours" min={1} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                  <label className="block sm:col-span-2"><span className="text-sm font-medium">Motif</span>
                    <input name="motif" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                </>
              )}
              {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
              <div className="sm:col-span-2">
                <Bouton type="submit" disabled={enCours}>
                  {enCours ? <Loader2 size={16} className="animate-spin" /> : <ShieldAlert size={16} />} Enregistrer
                </Bouton>
              </div>
            </form>
          </Card>

          <div className="grid lg:grid-cols-2 gap-6">
            <Card className="p-0 overflow-hidden">
              <h2 className="px-4 py-3 font-semibold border-b">Incidents ({incidents.length})</h2>
              {incidents.length === 0 ? <p className="px-4 py-4 text-sm text-ink-400">Aucun incident.</p> : incidents.map((i) => (
                <div key={i.id} className="px-4 py-3 border-b last:border-0">
                  <p className="text-xs text-ink-400">{i.dateIncident}</p>
                  <p className="text-sm mt-1">{i.description}</p>
                </div>
              ))}
            </Card>
            <Card className="p-0 overflow-hidden">
              <h2 className="px-4 py-3 font-semibold border-b">Sanctions ({sanctions.length})</h2>
              {sanctions.length === 0 ? <p className="px-4 py-4 text-sm text-ink-400">Aucune sanction.</p> : sanctions.map((s) => (
                <div key={s.id} className="px-4 py-3 border-b last:border-0">
                  <p className="text-sm font-medium capitalize">{s.typeSanctionLibelle ?? s.typeSanctionCode}</p>
                  <p className="text-xs text-ink-400 mt-1">{s.dateSanction}{s.dureeJours ? ` — ${s.dureeJours} jours` : ''}</p>
                </div>
              ))}
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
