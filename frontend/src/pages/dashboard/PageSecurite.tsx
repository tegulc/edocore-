import { useEffect, useState } from 'react';
import { ShieldCheck, Loader2 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { auditApi } from '@/lib/modulesApi';
import type { JournalAuditProfil } from '@/types/modules';

export default function PageSecurite() {
  const [entrees, setEntrees] = useState<JournalAuditProfil[]>([]);
  const [page, setPage] = useState(1);
  const [total, setTotal] = useState(0);
  const [filtre, setFiltre] = useState('');
  const [chargement, setChargement] = useState(true);

  async function charger(p = page, table = filtre) {
    setChargement(true);
    try {
      const { data } = await auditApi.lister(p, 30, table || undefined);
      setEntrees(data.donnees);
      setTotal(data.pagination.total);
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => { charger(); }, [page]);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold flex items-center gap-2"><ShieldCheck size={24} /> Sécurité & audit</h1>
        <p className="text-sm text-ink-400 mt-1">Journal des actions sensibles ({total} entrées).</p>
      </div>

      <Card>
        <div className="flex gap-2">
          <input value={filtre} onChange={(e) => setFiltre(e.target.value)} placeholder="Filtrer par table (ex: utilisateurs)…"
            className="flex-1 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
          <Bouton variante="discret" onClick={() => { setPage(1); charger(1, filtre); }}>Filtrer</Bouton>
        </div>
      </Card>

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr>
              <th className="px-4 py-3 font-medium">Horodatage</th>
              <th className="px-4 py-3 font-medium">Action</th>
              <th className="px-4 py-3 font-medium hidden sm:table-cell">Table</th>
              <th className="px-4 py-3 font-medium hidden md:table-cell">Utilisateur</th>
              <th className="px-4 py-3 font-medium hidden lg:table-cell">IP</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {chargement && <tr><td colSpan={5} className="px-4 py-6 text-center"><Loader2 size={20} className="animate-spin mx-auto text-ink-400" /></td></tr>}
            {!chargement && entrees.length === 0 && <tr><td colSpan={5} className="px-4 py-6 text-center text-ink-400">Aucune entrée.</td></tr>}
            {entrees.map((e) => (
              <tr key={e.id}>
                <td className="px-4 py-3 text-xs text-ink-400">{new Date(e.horodatage).toLocaleString('fr-FR')}</td>
                <td className="px-4 py-3 font-mono text-xs">{e.action}</td>
                <td className="px-4 py-3 hidden sm:table-cell text-ink-400">{e.tableCible ?? '—'}</td>
                <td className="px-4 py-3 hidden md:table-cell">#{e.utilisateurId ?? '—'}</td>
                <td className="px-4 py-3 hidden lg:table-cell text-xs text-ink-400">{e.adresseIp ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
        {total > 30 && (
          <div className="px-4 py-3 border-t flex gap-2">
            <Bouton variante="discret" disabled={page <= 1} onClick={() => setPage((p) => p - 1)}>Précédent</Bouton>
            <span className="text-sm text-ink-400 self-center">Page {page}</span>
            <Bouton variante="discret" disabled={entrees.length < 30} onClick={() => setPage((p) => p + 1)}>Suivant</Bouton>
          </div>
        )}
      </Card>
    </div>
  );
}
