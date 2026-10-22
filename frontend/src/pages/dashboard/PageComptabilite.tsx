import { useEffect, useState, type FormEvent } from 'react';
import { Calculator, Loader2 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { comptabiliteApi } from '@/lib/paiementApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { BalanceComptable, CompteComptableProfil, EcritureComptableProfil } from '@/types/paiement';

const DEBUT = new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().slice(0, 10);
const FIN = new Date().toISOString().slice(0, 10);

export default function PageComptabilite() {
  const [comptes, setComptes] = useState<CompteComptableProfil[]>([]);
  const [ecritures, setEcritures] = useState<EcritureComptableProfil[]>([]);
  const [balance, setBalance] = useState<BalanceComptable[]>([]);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function toutCharger() {
    const [c, e, b] = await Promise.all([
      comptabiliteApi.comptes(),
      comptabiliteApi.ecritures(DEBUT, FIN),
      comptabiliteApi.balance(DEBUT, FIN),
    ]);
    setComptes(c.data);
    setEcritures(e.data);
    setBalance(b.data);
  }

  useEffect(() => { toutCharger(); }, []);

  async function gererEcriture(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      await comptabiliteApi.enregistrerEcriture({
        compteId: Number(fd.get('compteId')),
        dateEcriture: fd.get('dateEcriture') as string,
        libelle: fd.get('libelle') as string,
        debit: Number(fd.get('debit')) || undefined,
        credit: Number(fd.get('credit')) || undefined,
      });
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
          <h1 className="font-display text-2xl font-bold">Comptabilité</h1>
          <p className="text-sm text-ink-400 mt-1">Journal comptable et balance du mois en cours.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)}><Calculator size={16} /> Nouvelle écriture</Bouton>
      </div>

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererEcriture} className="grid sm:grid-cols-2 gap-4">
            <label className="block sm:col-span-2">
              <span className="text-sm font-medium">Compte</span>
              <select required name="compteId" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="">Choisir…</option>
                {comptes.map((c) => <option key={c.id} value={c.id}>{c.code} — {c.libelle}</option>)}
              </select>
            </label>
            <label className="block"><span className="text-sm font-medium">Date</span>
              <input required type="date" name="dateEcriture" defaultValue={FIN} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Libellé</span>
              <input required name="libelle" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Débit</span>
              <input type="number" name="debit" min={0} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Crédit</span>
              <input type="number" name="credit" min={0} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Enregistrer</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      <div className="grid lg:grid-cols-2 gap-6">
        <Card className="p-0 overflow-hidden">
          <h2 className="px-4 py-3 font-semibold border-b border-ink-100 dark:border-white/10">Écritures récentes</h2>
          <table className="w-full text-sm">
            <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
              <tr><th className="px-4 py-2 font-medium">Date</th><th className="px-4 py-2 font-medium">Libellé</th><th className="px-4 py-2 font-medium">Débit</th><th className="px-4 py-2 font-medium">Crédit</th></tr>
            </thead>
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {ecritures.length === 0 && <tr><td colSpan={4} className="px-4 py-4 text-center text-ink-400">Aucune écriture.</td></tr>}
              {ecritures.map((e) => (
                <tr key={e.id}>
                  <td className="px-4 py-2 text-xs">{e.dateEcriture}</td>
                  <td className="px-4 py-2">{e.libelle}</td>
                  <td className="px-4 py-2">{e.debit > 0 ? e.debit.toLocaleString('fr-FR') : '—'}</td>
                  <td className="px-4 py-2">{e.credit > 0 ? e.credit.toLocaleString('fr-FR') : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>

        <Card className="p-0 overflow-hidden">
          <h2 className="px-4 py-3 font-semibold border-b border-ink-100 dark:border-white/10">Balance</h2>
          <table className="w-full text-sm">
            <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
              <tr><th className="px-4 py-2 font-medium">Compte</th><th className="px-4 py-2 font-medium">Solde</th></tr>
            </thead>
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {balance.length === 0 && <tr><td colSpan={2} className="px-4 py-4 text-center text-ink-400">Aucune donnée.</td></tr>}
              {balance.map((b) => (
                <tr key={b.compteCode}>
                  <td className="px-4 py-2"><span className="font-mono text-xs">{b.compteCode}</span> {b.compteLibelle}</td>
                  <td className="px-4 py-2 font-medium">{b.solde.toLocaleString('fr-FR')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      </div>
    </div>
  );
}
