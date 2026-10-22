import { useEffect, useState } from 'react';
import { BarChart3, Loader2, Users, Wallet, ClipboardCheck, AlertTriangle } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { rapportApi } from '@/lib/modulesApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import type {
  RapportEffectifClasse, RapportMoyenneClasse, RapportSoldeEleve,
  RapportSynthese, RapportTauxPresence,
} from '@/types/pedagogie';

type Onglet = 'synthese' | 'effectifs' | 'finances' | 'presences' | 'moyennes';

export default function PageRapports() {
  const { classes, periodes, chargement } = useSelecteursPedagogiques();
  const [onglet, setOnglet] = useState<Onglet>('synthese');
  const [synthese, setSynthese] = useState<RapportSynthese | null>(null);
  const [effectifs, setEffectifs] = useState<RapportEffectifClasse[]>([]);
  const [soldes, setSoldes] = useState<RapportSoldeEleve[]>([]);
  const [presences, setPresences] = useState<RapportTauxPresence[]>([]);
  const [moyennes, setMoyennes] = useState<RapportMoyenneClasse[]>([]);
  const [classeId, setClasseId] = useState('');
  const [periodeId, setPeriodeId] = useState('');
  const [enCours, setEnCours] = useState(false);

  useEffect(() => {
    setEnCours(true);
    rapportApi.synthese().then(({ data }) => setSynthese(data)).finally(() => setEnCours(false));
  }, []);

  useEffect(() => {
    if (onglet === 'effectifs') rapportApi.effectifs().then(({ data }) => setEffectifs(data));
    if (onglet === 'finances') rapportApi.soldes().then(({ data }) => setSoldes(data));
    if (onglet === 'presences') rapportApi.presences().then(({ data }) => setPresences(data));
  }, [onglet]);

  useEffect(() => {
    if (onglet === 'moyennes' && classeId && periodeId) {
      rapportApi.moyennes(Number(classeId), Number(periodeId)).then(({ data }) => setMoyennes(data));
    }
  }, [onglet, classeId, periodeId]);

  const onglets: { id: Onglet; libelle: string }[] = [
    { id: 'synthese', libelle: 'Synthèse' },
    { id: 'effectifs', libelle: 'Effectifs' },
    { id: 'finances', libelle: 'Finances' },
    { id: 'presences', libelle: 'Présences' },
    { id: 'moyennes', libelle: 'Moyennes' },
  ];

  if (chargement) {
    return <div className="flex justify-center py-12"><Loader2 size={24} className="animate-spin text-ink-400" /></div>;
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Rapports</h1>
        <p className="text-sm text-ink-400 mt-1">Statistiques et exports pour le pilotage de l'établissement.</p>
      </div>

      <div className="flex flex-wrap gap-2">
        {onglets.map((o) => (
          <button key={o.id} onClick={() => setOnglet(o.id)}
            className={`px-3 py-1.5 rounded-lg text-sm font-medium transition-colors ${onglet === o.id ? 'bg-brand-600 text-white' : 'bg-ink-50 dark:bg-white/5 text-ink-600 dark:text-ink-300 hover:bg-ink-100'}`}>
            {o.libelle}
          </button>
        ))}
      </div>

      {onglet === 'synthese' && synthese && (
        <div className="grid sm:grid-cols-2 xl:grid-cols-5 gap-4">
          <Card><Users size={16} className="text-ink-400" /><p className="text-2xl font-bold mt-2">{synthese.elevesActifs}</p><p className="text-xs text-ink-400">Élèves actifs</p></Card>
          <Card><ClipboardCheck size={16} className="text-ink-400" /><p className="text-2xl font-bold mt-2">{synthese.tauxPresenceMoyen.toFixed(1)}%</p><p className="text-xs text-ink-400">Présence aujourd'hui</p></Card>
          <Card><Wallet size={16} className="text-ink-400" /><p className="text-2xl font-bold mt-2">{Number(synthese.recettesMois).toLocaleString('fr-FR')}</p><p className="text-xs text-ink-400">Recettes du mois</p></Card>
          <Card><AlertTriangle size={16} className="text-ink-400" /><p className="text-2xl font-bold mt-2">{synthese.incidentsDisciplinaires}</p><p className="text-xs text-ink-400">Incidents discipline</p></Card>
          <Card><BarChart3 size={16} className="text-ink-400" /><p className="text-2xl font-bold mt-2">{synthese.congesEnAttente}</p><p className="text-xs text-ink-400">Congés en attente</p></Card>
        </div>
      )}

      {onglet === 'moyennes' && (
        <Card>
          <div className="grid sm:grid-cols-2 gap-4">
            <label className="block"><span className="text-sm font-medium">Classe</span>
              <select value={classeId} onChange={(e) => setClasseId(e.target.value)}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="">Sélectionner…</option>
                {classes.map((c) => <option key={c.id} value={c.id}>{c.libelle}</option>)}
              </select></label>
            <label className="block"><span className="text-sm font-medium">Période</span>
              <select value={periodeId} onChange={(e) => setPeriodeId(e.target.value)}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="">Sélectionner…</option>
                {periodes.map((p) => <option key={p.id} value={p.id}>{p.libelle}</option>)}
              </select></label>
          </div>
        </Card>
      )}

      <Card className="p-0 overflow-hidden">
        {enCours && <div className="flex justify-center py-8"><Loader2 size={20} className="animate-spin text-ink-400" /></div>}
        {!enCours && onglet === 'effectifs' && (
          <table className="w-full text-sm">
            <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
              <tr><th className="px-4 py-3">Classe</th><th className="px-4 py-3">Effectif</th><th className="px-4 py-3">Capacité</th><th className="px-4 py-3">Taux remplissage</th></tr>
            </thead>
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {effectifs.map((e) => (
                <tr key={e.classeId}>
                  <td className="px-4 py-3 font-medium">{e.libelle}</td>
                  <td className="px-4 py-3">{e.effectif}</td>
                  <td className="px-4 py-3">{e.capacite}</td>
                  <td className="px-4 py-3">{e.capacite > 0 ? Math.round((e.effectif / e.capacite) * 100) : 0}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {!enCours && onglet === 'finances' && (
          <table className="w-full text-sm">
            <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
              <tr><th className="px-4 py-3">Élève</th><th className="px-4 py-3">Total dû</th><th className="px-4 py-3">Payé</th><th className="px-4 py-3">Solde</th></tr>
            </thead>
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {soldes.filter((s) => Number(s.soldeRestant) > 0).map((s) => (
                <tr key={s.eleveId}>
                  <td className="px-4 py-3 font-medium">{s.prenom} {s.nom}</td>
                  <td className="px-4 py-3">{Number(s.totalDu).toLocaleString('fr-FR')}</td>
                  <td className="px-4 py-3">{Number(s.totalPaye).toLocaleString('fr-FR')}</td>
                  <td className="px-4 py-3 text-red-600 font-medium">{Number(s.soldeRestant).toLocaleString('fr-FR')}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {!enCours && onglet === 'presences' && (
          <table className="w-full text-sm">
            <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
              <tr><th className="px-4 py-3">Élève</th><th className="px-4 py-3">Présents</th><th className="px-4 py-3">Absents</th><th className="px-4 py-3">Taux</th></tr>
            </thead>
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {presences.slice(0, 50).map((p) => (
                <tr key={p.eleveId}>
                  <td className="px-4 py-3 font-medium">{p.prenom} {p.nom}</td>
                  <td className="px-4 py-3">{p.joursPresents}</td>
                  <td className="px-4 py-3">{p.joursAbsents}</td>
                  <td className="px-4 py-3">{p.tauxPresence?.toFixed(1) ?? 0}%</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {!enCours && onglet === 'moyennes' && moyennes.length > 0 && (
          <table className="w-full text-sm">
            <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
              <tr><th className="px-4 py-3">Rang</th><th className="px-4 py-3">Élève</th><th className="px-4 py-3">Moyenne</th></tr>
            </thead>
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {moyennes.map((m) => (
                <tr key={m.eleveId}>
                  <td className="px-4 py-3">{m.rang ?? '—'}</td>
                  <td className="px-4 py-3 font-medium">{m.prenom} {m.nom}</td>
                  <td className="px-4 py-3 font-medium">{m.moyenne?.toFixed(2) ?? '—'}/20</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </Card>
    </div>
  );
}
