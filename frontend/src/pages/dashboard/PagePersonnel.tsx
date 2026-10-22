import { useEffect, useState, type FormEvent } from 'react';
import { Loader2, Plus, CalendarDays } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { personnelApi } from '@/lib/modulesApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { DemandeCongeProfil, PersonnelProfil } from '@/types/pedagogie';

const ROLES_PERSONNEL = ['PERSONNEL_ADMIN', 'SECURITE', 'ECONOME', 'COMPTABLE', 'SECRETAIRE', 'BIBLIOTHECAIRE', 'INFIRMIER', 'CHAUFFEUR'];

export default function PagePersonnel() {
  const [personnel, setPersonnel] = useState<PersonnelProfil[]>([]);
  const [congesEnAttente, setCongesEnAttente] = useState<DemandeCongeProfil[]>([]);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [motDePasse, setMotDePasse] = useState<string | null>(null);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function charger() {
    const [p, c] = await Promise.all([personnelApi.lister(), personnelApi.congesEnAttente()]);
    setPersonnel(p.data);
    setCongesEnAttente(c.data);
  }

  useEffect(() => { charger(); }, []);

  async function gererCreation(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    setMotDePasse(null);
    try {
      const { data } = await personnelApi.creer({
        nom: fd.get('nom') as string,
        prenom: fd.get('prenom') as string,
        email: fd.get('email') as string,
        telephone: (fd.get('telephone') as string) || undefined,
        matricule: (fd.get('matricule') as string) || undefined,
        roleCode: fd.get('roleCode') as string,
        fonction: (fd.get('fonction') as string) || undefined,
        departement: (fd.get('departement') as string) || undefined,
        typeContrat: (fd.get('typeContrat') as string) || undefined,
        salaireBase: fd.get('salaireBase') ? Number(fd.get('salaireBase')) : undefined,
      });
      setMotDePasse(data.motDePasseTemporaire);
      setFormulaireOuvert(false);
      await charger();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function validerConge(congeId: number, statut: 'approuve' | 'refuse') {
    setEnCours(true);
    try {
      await personnelApi.validerConge(congeId, statut);
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
          <h1 className="font-display text-2xl font-bold">Personnel</h1>
          <p className="text-sm text-ink-400 mt-1">Gestion RH et demandes de congé.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)}><Plus size={16} /> Ajouter</Bouton>
      </div>

      {motDePasse && (
        <Card className="border-amber-200 bg-amber-50 dark:bg-amber-500/10">
          <p className="text-sm font-medium text-amber-800 dark:text-amber-200">
            Mot de passe temporaire (affiché une seule fois) : <code className="font-mono">{motDePasse}</code>
          </p>
        </Card>
      )}

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererCreation} className="grid sm:grid-cols-2 gap-4">
            <label className="block"><span className="text-sm font-medium">Nom</span>
              <input required name="nom" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Prénom</span>
              <input required name="prenom" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Email</span>
              <input required type="email" name="email" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Téléphone</span>
              <input name="telephone" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Rôle</span>
              <select required name="roleCode" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                {ROLES_PERSONNEL.map((r) => <option key={r} value={r}>{r}</option>)}
              </select></label>
            <label className="block"><span className="text-sm font-medium">Fonction</span>
              <input name="fonction" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Département</span>
              <input name="departement" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Salaire base</span>
              <input type="number" name="salaireBase" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Créer</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      {congesEnAttente.length > 0 && (
        <Card>
          <h2 className="font-semibold mb-3 flex items-center gap-2"><CalendarDays size={16} /> Congés en attente ({congesEnAttente.length})</h2>
          {congesEnAttente.map((c) => (
            <div key={c.id} className="flex flex-wrap items-center justify-between gap-2 py-2 border-t border-ink-100 dark:border-white/10">
              <div>
                <p className="text-sm font-medium">{c.typeConge} — utilisateur #{c.utilisateurId}</p>
                <p className="text-xs text-ink-400">{c.dateDebut} → {c.dateFin}</p>
              </div>
              <div className="flex gap-2">
                <Bouton variante="discret" disabled={enCours} onClick={() => validerConge(c.id, 'approuve')}>Approuver</Bouton>
                <Bouton variante="discret" disabled={enCours} onClick={() => validerConge(c.id, 'refuse')}>Refuser</Bouton>
              </div>
            </div>
          ))}
        </Card>
      )}

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr>
              <th className="px-4 py-3 font-medium">Nom</th>
              <th className="px-4 py-3 font-medium hidden sm:table-cell">Fonction</th>
              <th className="px-4 py-3 font-medium hidden md:table-cell">Email</th>
              <th className="px-4 py-3 font-medium">Statut</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {personnel.length === 0 && <tr><td colSpan={4} className="px-4 py-6 text-center text-ink-400">Aucun personnel enregistré.</td></tr>}
            {personnel.map((p) => (
              <tr key={p.id}>
                <td className="px-4 py-3 font-medium">{p.prenom} {p.nom}</td>
                <td className="px-4 py-3 text-ink-400 hidden sm:table-cell">{p.fonction ?? '—'}</td>
                <td className="px-4 py-3 text-ink-400 hidden md:table-cell">{p.email ?? '—'}</td>
                <td className="px-4 py-3"><span className="text-xs px-2 py-0.5 rounded-full bg-brand-50 dark:bg-brand-500/15 text-brand-700">{p.statut}</span></td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
