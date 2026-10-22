import { useEffect, useState } from 'react';
import { ClipboardCheck, Loader2 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { eleveApi } from '@/lib/eleveApi';
import { presenceApi } from '@/lib/emploiDuTempsApi';
import type { EleveProfil } from '@/types/eleve';

const OPTIONS_STATUT: { valeur: string; libelle: string; classe: string }[] = [
  { valeur: 'present', libelle: 'Présent', classe: 'bg-emerald-50 dark:bg-emerald-500/15 text-emerald-700 dark:text-emerald-400' },
  { valeur: 'absent', libelle: 'Absent', classe: 'bg-red-50 dark:bg-red-500/15 text-red-700 dark:text-red-400' },
  { valeur: 'retard', libelle: 'Retard', classe: 'bg-amber-50 dark:bg-amber-500/15 text-amber-700 dark:text-amber-400' },
];

/**
 * Pointage rapide, tous eleves de l'etablissement (le filtrage par classe
 * viendra avec un endpoint dedie "eleves d'une classe" dans une prochaine
 * iteration — pour l'instant, pointage general d'entree, sans cours associe).
 */
export default function PagePresences() {
  const [eleves, setEleves] = useState<EleveProfil[]>([]);
  const [chargement, setChargement] = useState(true);
  const [enCours, setEnCours] = useState<number | null>(null);
  const [marques, setMarques] = useState<Record<number, string>>({});
  const aujourdHui = new Date().toISOString().slice(0, 10);

  useEffect(() => {
    eleveApi.lister(1, 100).then(({ data }) => { setEleves(data.donnees); setChargement(false); });
  }, []);

  async function marquer(eleveId: number, statut: string) {
    setEnCours(eleveId);
    try {
      await presenceApi.enregistrer({ eleveId, dateJour: aujourdHui, typePresence: statut });
      setMarques({ ...marques, [eleveId]: statut });
    } finally {
      setEnCours(null);
    }
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Présences</h1>
        <p className="text-sm text-ink-400 mt-1">Pointage du {new Date().toLocaleDateString('fr-FR', { weekday: 'long', day: 'numeric', month: 'long' })}.</p>
      </div>

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr><th className="px-4 py-3 font-medium">Élève</th><th className="px-4 py-3 font-medium">Statut</th></tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {chargement && <tr><td colSpan={2} className="px-4 py-6 text-center text-ink-400">Chargement...</td></tr>}
            {!chargement && eleves.length === 0 && <tr><td colSpan={2} className="px-4 py-6 text-center text-ink-400">Aucun élève.</td></tr>}
            {eleves.map((el) => (
              <tr key={el.id}>
                <td className="px-4 py-3 font-medium flex items-center gap-2">
                  <ClipboardCheck size={14} className="text-ink-400" /> {el.prenom} {el.nom}
                </td>
                <td className="px-4 py-3">
                  <div className="flex gap-1.5">
                    {OPTIONS_STATUT.map((opt) => (
                      <button
                        key={opt.valeur}
                        onClick={() => marquer(el.id, opt.valeur)}
                        disabled={enCours === el.id}
                        className={`px-2.5 py-1 rounded-full text-xs font-medium transition-theme ${
                          marques[el.id] === opt.valeur ? opt.classe : 'bg-ink-50 dark:bg-white/5 text-ink-400 hover:bg-ink-100 dark:hover:bg-white/10'
                        }`}
                      >
                        {enCours === el.id ? <Loader2 size={12} className="animate-spin" /> : opt.libelle}
                      </button>
                    ))}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
