import { useEffect, useState, type FormEvent } from 'react';
import { UtensilsCrossed, Loader2 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { cantineApi } from '@/lib/modulesApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import type { MenuCantineProfil } from '@/types/modules';

export default function PageCantine() {
  const { eleves, periodes, chargement } = useSelecteursPedagogiques(true);
  const [menus, setMenus] = useState<MenuCantineProfil[]>([]);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [eleveId, setEleveId] = useState('');
  const [periodeId, setPeriodeId] = useState('');
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  async function chargerMenus() {
    const { data } = await cantineApi.menus();
    setMenus(data);
  }

  useEffect(() => { chargerMenus(); }, []);

  async function gererMenu(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      await cantineApi.creerMenu({
        dateJour: fd.get('dateJour') as string,
        libellePlat: fd.get('libellePlat') as string,
        informationsNutritionnelles: (fd.get('infos') as string) || undefined,
      });
      setFormulaireOuvert(false);
      await chargerMenus();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function abonner() {
    if (!eleveId || !periodeId) return;
    setEnCours(true);
    setErreur(null);
    try {
      await cantineApi.abonner(Number(eleveId), { periodeId: Number(periodeId), montant: 50000 });
      setMessage('Abonnement cantine créé.');
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function pointer() {
    if (!eleveId) return;
    setEnCours(true);
    try {
      await cantineApi.pointer({ eleveId: Number(eleveId), dateJour: new Date().toISOString().slice(0, 10), statut: 'present' });
      setMessage('Présence cantine enregistrée.');
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
          <h1 className="font-display text-2xl font-bold">Cantine</h1>
          <p className="text-sm text-ink-400 mt-1">Menus, abonnements et présences.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)}><UtensilsCrossed size={16} /> Publier un menu</Bouton>
      </div>

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererMenu} className="grid sm:grid-cols-2 gap-4">
            <label className="block"><span className="text-sm font-medium">Date</span>
              <input required type="date" name="dateJour" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Plat</span>
              <input required name="libellePlat" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Infos nutritionnelles</span>
              <input name="infos" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Publier</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      <Card>
        <h2 className="font-semibold mb-4">Abonnement & pointage</h2>
        <div className="grid sm:grid-cols-3 gap-4">
          <select value={eleveId} onChange={(e) => setEleveId(e.target.value)}
            className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
            <option value="">{chargement ? 'Chargement…' : 'Élève…'}</option>
            {eleves.map((el) => <option key={el.id} value={el.id}>{el.prenom} {el.nom}</option>)}
          </select>
          <select value={periodeId} onChange={(e) => setPeriodeId(e.target.value)}
            className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
            <option value="">Période…</option>
            {periodes.map((p) => <option key={p.id} value={p.id}>{p.libelle}</option>)}
          </select>
          <div className="flex gap-2">
            <Bouton variante="discret" onClick={abonner} disabled={enCours || !eleveId}>Abonner</Bouton>
            <Bouton onClick={pointer} disabled={enCours || !eleveId}>Pointer repas</Bouton>
          </div>
        </div>
        {message && <p className="mt-3 text-sm text-emerald-600">{message}</p>}
      </Card>

      <Card className="p-0 overflow-hidden">
        <h2 className="px-4 py-3 font-semibold border-b">Menus de la semaine</h2>
        {menus.length === 0 ? <p className="px-4 py-6 text-sm text-ink-400">Aucun menu publié.</p> : (
          <table className="w-full text-sm">
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {menus.map((m) => (
                <tr key={m.id}>
                  <td className="px-4 py-3 text-ink-400">{m.dateJour}</td>
                  <td className="px-4 py-3 font-medium">{m.libellePlat}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}
