import { useEffect, useState, type FormEvent } from 'react';
import { Wallet, Loader2, CreditCard } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { echeancierApi, paiementApi } from '@/lib/paiementApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import type { EcheancierProfil, PaiementProfil } from '@/types/paiement';

export default function PagePaiements() {
  const { eleves, chargement } = useSelecteursPedagogiques(true);
  const [eleveId, setEleveId] = useState('');
  const [echeanciers, setEcheanciers] = useState<EcheancierProfil[]>([]);
  const [historique, setHistorique] = useState<PaiementProfil[]>([]);
  const [montant, setMontant] = useState('');
  const [moyen, setMoyen] = useState('especes');
  const [instructions, setInstructions] = useState<string | null>(null);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  useEffect(() => {
    if (eleveId) {
      echeancierApi.lister(Number(eleveId)).then(({ data }) => setEcheanciers(data)).catch(() => setEcheanciers([]));
      paiementApi.historique(Number(eleveId)).then(({ data }) => setHistorique(data)).catch(() => setHistorique([]));
    }
  }, [eleveId]);

  async function encaisser(e: FormEvent) {
    e.preventDefault();
    if (!eleveId || !montant) return;
    setEnCours(true);
    setErreur(null);
    setInstructions(null);
    try {
      if (moyen === 'orange_money' || moyen === 'mtn_momo') {
        const { data } = await paiementApi.initier({
          eleveId: Number(eleveId),
          montant: Number(montant),
          moyenPaiement: moyen,
        });
        setInstructions(data.instructions);
      } else {
        await paiementApi.encaisser({ eleveId: Number(eleveId), montant: Number(montant), moyenPaiement: moyen });
        const { data } = await paiementApi.historique(Number(eleveId));
        setHistorique(data);
        setMontant('');
      }
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Paiements</h1>
        <p className="text-sm text-ink-400 mt-1">Échéanciers et encaissement des frais scolaires.</p>
      </div>

      <Card>
        <label className="block max-w-md">
          <span className="text-sm font-medium">Élève</span>
          <select value={eleveId} onChange={(e) => setEleveId(e.target.value)}
            className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
            <option value="">{chargement ? 'Chargement…' : 'Choisir un élève…'}</option>
            {eleves.map((el) => <option key={el.id} value={el.id}>{el.prenom} {el.nom}</option>)}
          </select>
        </label>
      </Card>

      {echeanciers.length > 0 && (
        <Card className="p-0 overflow-hidden">
          <h2 className="px-4 py-3 font-semibold border-b border-ink-100 dark:border-white/10">Échéanciers</h2>
          {echeanciers.map((ech) => (
            <div key={ech.id} className="px-4 py-3 border-b border-ink-100 dark:border-white/10 last:border-0">
              <p className="text-sm font-medium">{ech.montantTotal.toLocaleString('fr-FR')} XAF — {ech.nombreTranches} tranches</p>
              <div className="mt-2 flex flex-wrap gap-2">
                {ech.tranches.map((t) => (
                  <span key={t.id} className={`text-xs px-2 py-1 rounded-full ${t.statut === 'paye' ? 'bg-emerald-50 text-emerald-700' : t.statut === 'en_retard' ? 'bg-red-50 text-red-700' : 'bg-ink-50 text-ink-600'}`}>
                    T{t.numeroTranche}: {t.montantPaye}/{t.montantDu} ({t.statut})
                  </span>
                ))}
              </div>
            </div>
          ))}
        </Card>
      )}

      {eleveId && (
        <Card>
          <h2 className="font-semibold mb-4 flex items-center gap-2"><CreditCard size={18} /> Encaisser un paiement</h2>
          <form onSubmit={encaisser} className="grid sm:grid-cols-3 gap-4">
            <label className="block">
              <span className="text-sm font-medium">Montant (XAF)</span>
              <input required type="number" min={1} value={montant} onChange={(e) => setMontant(e.target.value)}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            <label className="block">
              <span className="text-sm font-medium">Moyen de paiement</span>
              <select value={moyen} onChange={(e) => setMoyen(e.target.value)}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="especes">Espèces</option>
                <option value="virement">Virement</option>
                <option value="orange_money">Orange Money</option>
                <option value="mtn_momo">MTN MoMo</option>
              </select>
            </label>
            <div className="flex items-end">
              <Bouton type="submit" disabled={enCours} className="w-full">
                {enCours ? <Loader2 size={16} className="animate-spin" /> : <Wallet size={16} />} Encaisser
              </Bouton>
            </div>
          </form>
          {instructions && <p className="mt-3 text-sm text-brand-600 bg-brand-50 dark:bg-brand-500/10 rounded-lg px-3 py-2">{instructions}</p>}
          {erreur && <p className="mt-3 text-sm text-red-600">{erreur}</p>}
        </Card>
      )}

      {historique.length > 0 && (
        <Card className="p-0 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
              <tr>
                <th className="px-4 py-3 font-medium">Date</th>
                <th className="px-4 py-3 font-medium">Montant</th>
                <th className="px-4 py-3 font-medium">Moyen</th>
                <th className="px-4 py-3 font-medium">Statut</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {historique.map((p) => (
                <tr key={p.id}>
                  <td className="px-4 py-3">{new Date(p.datePaiement).toLocaleDateString('fr-FR')}</td>
                  <td className="px-4 py-3 font-medium">{p.montant.toLocaleString('fr-FR')} XAF</td>
                  <td className="px-4 py-3 capitalize">{p.moyenPaiement.replace('_', ' ')}</td>
                  <td className="px-4 py-3 capitalize">{p.statut.replace('_', ' ')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  );
}
