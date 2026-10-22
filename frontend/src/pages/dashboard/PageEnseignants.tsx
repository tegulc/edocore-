import { useEffect, useState, type FormEvent } from 'react';
import { UserPlus, Loader2, Copy, Check } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { enseignantApi } from '@/lib/classeApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { EnseignantProfil } from '@/types/classe';

const VIDE = { nom: '', prenom: '', email: '', telephone: '', specialite: '' };

export default function PageEnseignants() {
  const [enseignants, setEnseignants] = useState<EnseignantProfil[]>([]);
  const [chargement, setChargement] = useState(true);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [formulaire, setFormulaire] = useState(VIDE);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [motDePasseGenere, setMotDePasseGenere] = useState<string | null>(null);
  const [copie, setCopie] = useState(false);

  async function charger() {
    setChargement(true);
    try {
      const { data } = await enseignantApi.lister();
      setEnseignants(data);
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => { charger(); }, []);

  async function gererCreation(e: FormEvent) {
    e.preventDefault();
    setEnCours(true);
    setErreur(null);
    try {
      const { data } = await enseignantApi.creer(formulaire);
      setMotDePasseGenere(data.motDePasseTemporaire);
      setFormulaire(VIDE);
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
          <h1 className="font-display text-2xl font-bold">Enseignants</h1>
          <p className="text-sm text-ink-400 mt-1">Personnel enseignant de l'établissement.</p>
        </div>
        <Bouton onClick={() => { setFormulaireOuvert((v) => !v); setMotDePasseGenere(null); }}>
          <UserPlus size={16} /> Nouvel enseignant
        </Bouton>
      </div>

      {formulaireOuvert && (
        <Card>
          {motDePasseGenere ? (
            <div className="space-y-3">
              <p className="text-sm font-medium">Compte créé avec succès.</p>
              <p className="text-sm text-ink-400">Mot de passe temporaire — affiché une seule fois :</p>
              <div className="flex items-center gap-2">
                <code className="px-3 py-2 rounded-lg bg-ink-50 dark:bg-white/5 font-mono text-sm">{motDePasseGenere}</code>
                <button
                  onClick={() => { navigator.clipboard.writeText(motDePasseGenere); setCopie(true); setTimeout(() => setCopie(false), 1500); }}
                  className="p-2 rounded-lg hover:bg-ink-50 dark:hover:bg-white/5 text-ink-400"
                >
                  {copie ? <Check size={16} className="text-emerald-500" /> : <Copy size={16} />}
                </button>
              </div>
              <Bouton variante="discret" onClick={() => { setFormulaireOuvert(false); setMotDePasseGenere(null); }}>Fermer</Bouton>
            </div>
          ) : (
            <form onSubmit={gererCreation} className="grid sm:grid-cols-2 gap-4">
              <label className="block">
                <span className="text-sm font-medium">Prénom</span>
                <input required value={formulaire.prenom} onChange={(e) => setFormulaire({ ...formulaire, prenom: e.target.value })}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
              </label>
              <label className="block">
                <span className="text-sm font-medium">Nom</span>
                <input required value={formulaire.nom} onChange={(e) => setFormulaire({ ...formulaire, nom: e.target.value })}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
              </label>
              <label className="block">
                <span className="text-sm font-medium">Email</span>
                <input required type="email" value={formulaire.email} onChange={(e) => setFormulaire({ ...formulaire, email: e.target.value })}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
              </label>
              <label className="block">
                <span className="text-sm font-medium">Spécialité</span>
                <input value={formulaire.specialite} onChange={(e) => setFormulaire({ ...formulaire, specialite: e.target.value })}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
              </label>
              {erreur && <p className="sm:col-span-2 text-sm text-red-600 bg-red-50 dark:bg-red-500/10 rounded-lg px-3 py-2">{erreur}</p>}
              <div className="sm:col-span-2 flex gap-2">
                <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Créer le compte</Bouton>
                <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
              </div>
            </form>
          )}
        </Card>
      )}

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr><th className="px-4 py-3 font-medium">Nom</th><th className="px-4 py-3 font-medium hidden sm:table-cell">Email</th><th className="px-4 py-3 font-medium">Spécialité</th></tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {chargement && <tr><td colSpan={3} className="px-4 py-6 text-center text-ink-400">Chargement...</td></tr>}
            {!chargement && enseignants.length === 0 && <tr><td colSpan={3} className="px-4 py-6 text-center text-ink-400">Aucun enseignant pour le moment.</td></tr>}
            {enseignants.map((ens) => (
              <tr key={ens.id}>
                <td className="px-4 py-3 font-medium">{ens.utilisateur.prenom} {ens.utilisateur.nom}</td>
                <td className="px-4 py-3 text-ink-400 hidden sm:table-cell">{ens.utilisateur.email}</td>
                <td className="px-4 py-3">{ens.specialite ?? '—'}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
