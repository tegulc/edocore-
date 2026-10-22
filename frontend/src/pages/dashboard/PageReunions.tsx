import { useEffect, useState, type FormEvent } from 'react';
import { Video, Loader2, Users } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { reunionApi } from '@/lib/modulesApi';
import { utilisateursApi } from '@/lib/utilisateursApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { ReunionProfil } from '@/types/modules';
import type { UtilisateurProfil } from '@/types/auth';

export default function PageReunions() {
  const [reunions, setReunions] = useState<ReunionProfil[]>([]);
  const [utilisateurs, setUtilisateurs] = useState<UtilisateurProfil[]>([]);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [invitesIds, setInvitesIds] = useState<number[]>([]);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function charger() {
    const [{ data: r }, { data: u }] = await Promise.all([
      reunionApi.lister(),
      utilisateursApi.lister(1, 100),
    ]);
    setReunions(r);
    setUtilisateurs(u.donnees);
  }

  useEffect(() => { charger(); }, []);

  async function gererCreation(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      await reunionApi.creer({
        titre: fd.get('titre') as string,
        description: (fd.get('description') as string) || undefined,
        dateReunion: new Date(fd.get('dateReunion') as string).toISOString(),
        lieu: (fd.get('lieu') as string) || undefined,
        lienVisioconference: (fd.get('lienVisio') as string) || undefined,
        invitesIds,
      });
      setFormulaireOuvert(false);
      setInvitesIds([]);
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
          <h1 className="font-display text-2xl font-bold">Réunions</h1>
          <p className="text-sm text-ink-400 mt-1">Organisation des réunions pédagogiques et parents-professeurs.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)}><Video size={16} /> Planifier</Bouton>
      </div>

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererCreation} className="grid sm:grid-cols-2 gap-4">
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Titre</span>
              <input required name="titre" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Date et heure</span>
              <input required type="datetime-local" name="dateReunion" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Lieu</span>
              <input name="lieu" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Lien visioconférence</span>
              <input name="lienVisio" type="url" placeholder="https://…" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Description</span>
              <textarea name="description" rows={2} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <div className="sm:col-span-2">
              <span className="text-sm font-medium flex items-center gap-1"><Users size={14} /> Invités</span>
              <div className="mt-2 max-h-40 overflow-y-auto space-y-1 border border-ink-100 dark:border-white/10 rounded-lg p-2">
                {utilisateurs.map((u) => (
                  <label key={u.id} className="flex items-center gap-2 text-sm">
                    <input type="checkbox" checked={invitesIds.includes(u.id)}
                      onChange={(e) => setInvitesIds((prev) => e.target.checked ? [...prev, u.id] : prev.filter((id) => id !== u.id))} />
                    {u.prenom} {u.nom}
                  </label>
                ))}
              </div>
            </div>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours || invitesIds.length === 0}>{enCours && <Loader2 size={16} className="animate-spin" />} Créer</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr><th className="px-4 py-3 font-medium">Réunion</th><th className="px-4 py-3 font-medium">Date</th><th className="px-4 py-3 font-medium hidden sm:table-cell">Lieu</th></tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {reunions.length === 0 && <tr><td colSpan={3} className="px-4 py-6 text-center text-ink-400">Aucune réunion planifiée.</td></tr>}
            {reunions.map((r) => (
              <tr key={r.id}>
                <td className="px-4 py-3 font-medium">{r.titre}</td>
                <td className="px-4 py-3 text-ink-400">{new Date(r.dateReunion).toLocaleString('fr-FR')}</td>
                <td className="px-4 py-3 hidden sm:table-cell">{r.lieu ?? (r.lienVisioconference ? 'Visio' : '—')}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
