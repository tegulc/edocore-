import { useState, type FormEvent } from 'react';
import { HeartPulse, Loader2, AlertTriangle } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { infirmerieApi } from '@/lib/modulesApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import type { ConsultationProfil, UrgenceProfil, VaccinProfil } from '@/types/modules';

type Onglet = 'consultation' | 'urgence' | 'vaccin';

export default function PageInfirmerie() {
  const { eleves, chargement } = useSelecteursPedagogiques(true);
  const [eleveId, setEleveId] = useState('');
  const [onglet, setOnglet] = useState<Onglet>('consultation');
  const [consultations, setConsultations] = useState<ConsultationProfil[]>([]);
  const [urgences, setUrgences] = useState<UrgenceProfil[]>([]);
  const [vaccins, setVaccins] = useState<VaccinProfil[]>([]);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function chargerDonnees(id: string) {
    if (!id) return;
    const [c, u, v] = await Promise.all([
      infirmerieApi.consultations(Number(id)),
      infirmerieApi.urgences(Number(id)),
      infirmerieApi.vaccins(Number(id)),
    ]);
    setConsultations(c.data);
    setUrgences(u.data);
    setVaccins(v.data);
  }

  async function gererSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!eleveId) return;
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      if (onglet === 'consultation') {
        await infirmerieApi.creerConsultation({
          eleveId: Number(eleveId),
          motif: fd.get('motif') as string,
          diagnostic: (fd.get('diagnostic') as string) || undefined,
          traitement: (fd.get('traitement') as string) || undefined,
        });
      } else if (onglet === 'urgence') {
        await infirmerieApi.creerUrgence({
          eleveId: Number(eleveId),
          description: fd.get('description') as string,
          actionPrise: (fd.get('actionPrise') as string) || undefined,
        });
      } else {
        await infirmerieApi.creerVaccin({
          eleveId: Number(eleveId),
          nomVaccin: fd.get('nomVaccin') as string,
          dateAdministration: fd.get('dateAdministration') as string,
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
        <h1 className="font-display text-2xl font-bold">Infirmerie</h1>
        <p className="text-sm text-ink-400 mt-1">Consultations, urgences et carnet de vaccination.</p>
      </div>

      <Card>
        <label className="block max-w-md">
          <span className="text-sm font-medium">Élève</span>
          <select value={eleveId} onChange={(e) => { setEleveId(e.target.value); chargerDonnees(e.target.value); }}
            className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
            <option value="">{chargement ? 'Chargement…' : 'Choisir…'}</option>
            {eleves.map((el) => <option key={el.id} value={el.id}>{el.prenom} {el.nom}</option>)}
          </select>
        </label>
      </Card>

      {eleveId && (
        <>
          <div className="flex gap-2 flex-wrap">
            {([['consultation', 'Consultation', HeartPulse], ['urgence', 'Urgence', AlertTriangle], ['vaccin', 'Vaccin', HeartPulse]] as const).map(([id, label]) => (
              <button key={id} onClick={() => setOnglet(id)}
                className={`px-4 py-2 rounded-lg text-sm font-medium ${onglet === id ? 'bg-brand-500 text-white' : 'bg-ink-50 dark:bg-white/5'}`}>
                {label}
              </button>
            ))}
          </div>

          <Card>
            <form onSubmit={gererSubmit} className="grid sm:grid-cols-2 gap-4">
              {onglet === 'consultation' && (
                <>
                  <label className="block sm:col-span-2"><span className="text-sm font-medium">Motif</span>
                    <input required name="motif" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                  <label className="block"><span className="text-sm font-medium">Diagnostic</span>
                    <input name="diagnostic" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                  <label className="block"><span className="text-sm font-medium">Traitement</span>
                    <input name="traitement" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                </>
              )}
              {onglet === 'urgence' && (
                <>
                  <label className="block sm:col-span-2"><span className="text-sm font-medium">Description</span>
                    <textarea required name="description" rows={3} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                  <label className="block sm:col-span-2"><span className="text-sm font-medium">Action prise</span>
                    <input name="actionPrise" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                </>
              )}
              {onglet === 'vaccin' && (
                <>
                  <label className="block"><span className="text-sm font-medium">Vaccin</span>
                    <input required name="nomVaccin" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                  <label className="block"><span className="text-sm font-medium">Date</span>
                    <input required type="date" name="dateAdministration" defaultValue={new Date().toISOString().slice(0, 10)}
                      className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
                </>
              )}
              {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
              <div className="sm:col-span-2">
                <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Enregistrer</Bouton>
              </div>
            </form>
          </Card>

          <div className="grid lg:grid-cols-3 gap-4">
            <Card>
              <h3 className="font-semibold mb-2">Consultations</h3>
              {consultations.length === 0 ? <p className="text-sm text-ink-400">Aucune.</p> : consultations.map((c) => (
                <div key={c.id} className="text-sm py-2 border-b last:border-0"><strong>{c.motif}</strong><br /><span className="text-xs text-ink-400">{c.dateConsultation}</span></div>
              ))}
            </Card>
            <Card>
              <h3 className="font-semibold mb-2">Urgences</h3>
              {urgences.length === 0 ? <p className="text-sm text-ink-400">Aucune.</p> : urgences.map((u) => (
                <div key={u.id} className="text-sm py-2 border-b last:border-0">{u.description}{u.parentNotifie && <span className="text-xs text-emerald-600 ml-1">✓ parent notifié</span>}</div>
              ))}
            </Card>
            <Card>
              <h3 className="font-semibold mb-2">Vaccins</h3>
              {vaccins.length === 0 ? <p className="text-sm text-ink-400">Aucun.</p> : vaccins.map((v) => (
                <div key={v.id} className="text-sm py-2 border-b last:border-0">{v.nomVaccin} — {v.dateAdministration}</div>
              ))}
            </Card>
          </div>
        </>
      )}
    </div>
  );
}
