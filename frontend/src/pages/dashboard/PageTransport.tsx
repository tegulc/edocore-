import { useEffect, useState, type FormEvent } from 'react';
import { Bus, Loader2 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { transportApi } from '@/lib/modulesApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { BusProfil } from '@/types/modules';

export default function PageTransport() {
  const [bus, setBus] = useState<BusProfil[]>([]);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [typeForm, setTypeForm] = useState<'bus' | 'itineraire'>('bus');
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function charger() {
    const { data } = await transportApi.bus();
    setBus(data);
  }

  useEffect(() => { charger(); }, []);

  async function gererSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      if (typeForm === 'bus') {
        await transportApi.creerBus({
          immatriculation: fd.get('immatriculation') as string,
          capacite: Number(fd.get('capacite')),
        });
      } else {
        await transportApi.creerItineraire({
          busId: Number(fd.get('busId')),
          libelle: fd.get('libelle') as string,
          heureDepartMatin: (fd.get('heureDepartMatin') as string) || undefined,
        });
      }
      setFormulaireOuvert(false);
      await charger();
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
          <h1 className="font-display text-2xl font-bold">Transport scolaire</h1>
          <p className="text-sm text-ink-400 mt-1">Flotte de bus et itinéraires.</p>
        </div>
        <div className="flex gap-2">
          <Bouton variante="discret" onClick={() => { setTypeForm('bus'); setFormulaireOuvert(true); }}><Bus size={16} /> Bus</Bouton>
          <Bouton onClick={() => { setTypeForm('itineraire'); setFormulaireOuvert(true); }}>Itinéraire</Bouton>
        </div>
      </div>

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererSubmit} className="grid sm:grid-cols-2 gap-4">
            {typeForm === 'bus' ? (
              <>
                <label className="block"><span className="text-sm font-medium">Immatriculation</span>
                  <input required name="immatriculation" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                <label className="block"><span className="text-sm font-medium">Capacité</span>
                  <input required type="number" name="capacite" defaultValue={50} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
              </>
            ) : (
              <>
                <label className="block"><span className="text-sm font-medium">Bus</span>
                  <select required name="busId" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                    {bus.map((b) => <option key={b.id} value={b.id}>{b.immatriculation}</option>)}
                  </select></label>
                <label className="block"><span className="text-sm font-medium">Libellé</span>
                  <input required name="libelle" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                <label className="block"><span className="text-sm font-medium">Départ matin</span>
                  <input type="time" name="heureDepartMatin" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
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
            <tr><th className="px-4 py-3 font-medium">Immatriculation</th><th className="px-4 py-3 font-medium">Capacité</th><th className="px-4 py-3 font-medium">Statut</th></tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {bus.length === 0 && <tr><td colSpan={3} className="px-4 py-6 text-center text-ink-400">Aucun bus enregistré.</td></tr>}
            {bus.map((b) => (
              <tr key={b.id}>
                <td className="px-4 py-3 font-medium">{b.immatriculation}</td>
                <td className="px-4 py-3">{b.capacite} places</td>
                <td className="px-4 py-3 capitalize">{b.statut}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
