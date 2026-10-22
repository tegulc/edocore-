import { useEffect, useState, type FormEvent } from 'react';
import { UserPlus, Loader2, Copy, Check } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { utilisateursApi, type CreationUtilisateurEntree } from '@/lib/utilisateursApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { UtilisateurProfil, RoleCode } from '@/types/auth';

const ROLES_DISPONIBLES: RoleCode[] = [
  'ADMIN', 'DIRECTEUR', 'CENSEUR', 'SURVEILLANT_GENERAL', 'ECONOME', 'COMPTABLE',
  'SECRETAIRE', 'PROFESSEUR', 'BIBLIOTHECAIRE', 'INFIRMIER', 'CHAUFFEUR', 'PERSONNEL_ADMIN', 'SECURITE',
];

const VIDE: CreationUtilisateurEntree = { nom: '', prenom: '', email: '', telephone: '', roleCodes: ['PROFESSEUR'], matricule: '' };

/**
 * Gestion des utilisateurs de l'etablissement (Module 1 - Phase 1).
 * Reservee a ADMIN/DIRECTEUR (deja filtre dans la Sidebar ; l'API refuse
 * de toute facon tout autre role, defense en profondeur cote backend).
 */
export default function PageUtilisateurs() {
  const [utilisateurs, setUtilisateurs] = useState<UtilisateurProfil[]>([]);
  const [chargement, setChargement] = useState(true);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [formulaire, setFormulaire] = useState<CreationUtilisateurEntree>(VIDE);
  const [enCreation, setEnCreation] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [motDePasseGenere, setMotDePasseGenere] = useState<string | null>(null);
  const [copie, setCopie] = useState(false);

  async function charger() {
    setChargement(true);
    try {
      const { data } = await utilisateursApi.lister();
      setUtilisateurs(data.donnees);
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => { charger(); }, []);

  async function gererCreation(e: FormEvent) {
    e.preventDefault();
    setEnCreation(true);
    setErreur(null);
    try {
      const { data } = await utilisateursApi.creer(formulaire);
      setMotDePasseGenere(data.motDePasseTemporaire);
      setFormulaire(VIDE);
      await charger();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCreation(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Utilisateurs</h1>
          <p className="text-sm text-ink-400 mt-1">Gérez les comptes du personnel de votre établissement.</p>
        </div>
        <Bouton onClick={() => { setFormulaireOuvert((v) => !v); setMotDePasseGenere(null); }}>
          <UserPlus size={16} /> Nouvel utilisateur
        </Bouton>
      </div>

      {formulaireOuvert && (
        <Card>
          {motDePasseGenere ? (
            <div className="space-y-3">
              <p className="text-sm font-medium">Compte créé avec succès.</p>
              <p className="text-sm text-ink-400">
                Communiquez ce mot de passe temporaire à l'utilisateur — il devra le changer à sa première connexion. Il ne sera plus jamais affiché.
              </p>
              <div className="flex items-center gap-2">
                <code className="px-3 py-2 rounded-lg bg-ink-50 dark:bg-white/5 font-mono text-sm">{motDePasseGenere}</code>
                <button
                  onClick={() => { navigator.clipboard.writeText(motDePasseGenere); setCopie(true); setTimeout(() => setCopie(false), 1500); }}
                  className="p-2 rounded-lg hover:bg-ink-50 dark:hover:bg-white/5 text-ink-400"
                  aria-label="Copier"
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
                <span className="text-sm font-medium">Téléphone</span>
                <input value={formulaire.telephone} onChange={(e) => setFormulaire({ ...formulaire, telephone: e.target.value })}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
              </label>
              <div className="sm:col-span-2">
                <span className="text-sm font-medium">Rôle(s)</span>
                <p className="text-xs text-ink-400 mt-0.5">Le premier rôle coché devient le rôle principal.</p>
                <div className="mt-2 grid grid-cols-2 sm:grid-cols-3 gap-2">
                  {ROLES_DISPONIBLES.map((r) => {
                    const coche = formulaire.roleCodes.includes(r);
                    return (
                      <label key={r} className={
                        `flex items-center gap-2 rounded-lg border px-3 py-2 text-xs cursor-pointer transition-theme ${
                          coche
                            ? 'border-brand-500 bg-brand-50 dark:bg-brand-500/15 text-brand-700 dark:text-brand-300'
                            : 'border-ink-100 dark:border-white/10 hover:bg-ink-50 dark:hover:bg-white/5'
                        }`
                      }>
                        <input
                          type="checkbox"
                          className="accent-brand-500"
                          checked={coche}
                          onChange={(e) => {
                            const roleCodes = e.target.checked
                              ? [...formulaire.roleCodes, r]
                              : formulaire.roleCodes.filter((x) => x !== r);
                            setFormulaire({ ...formulaire, roleCodes });
                          }}
                        />
                        {r}
                      </label>
                    );
                  })}
                </div>
              </div>

              {erreur && <p className="sm:col-span-2 text-sm text-red-600 bg-red-50 dark:bg-red-500/10 rounded-lg px-3 py-2">{erreur}</p>}

              <div className="sm:col-span-2 flex gap-2">
                <Bouton type="submit" disabled={enCreation || formulaire.roleCodes.length === 0}>
                  {enCreation && <Loader2 size={16} className="animate-spin" />} Créer le compte
                </Bouton>
                <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
              </div>
            </form>
          )}
        </Card>
      )}

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr>
              <th className="px-4 py-3 font-medium">Nom</th>
              <th className="px-4 py-3 font-medium hidden sm:table-cell">Email</th>
              <th className="px-4 py-3 font-medium">Rôle</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {chargement && (
              <tr><td colSpan={3} className="px-4 py-6 text-center text-ink-400">Chargement...</td></tr>
            )}
            {!chargement && utilisateurs.length === 0 && (
              <tr><td colSpan={3} className="px-4 py-6 text-center text-ink-400">Aucun utilisateur pour le moment.</td></tr>
            )}
            {utilisateurs.map((u) => (
              <tr key={u.id}>
                <td className="px-4 py-3 font-medium">{u.prenom} {u.nom}</td>
                <td className="px-4 py-3 text-ink-400 hidden sm:table-cell">{u.email}</td>
                <td className="px-4 py-3">
                  <div className="flex flex-wrap gap-1">
                    {u.roles.map((r) => (
                      <span key={r} className={
                        r === u.rolePrincipal
                          ? 'inline-flex items-center rounded-full bg-brand-50 dark:bg-brand-500/15 text-brand-700 dark:text-brand-300 px-2.5 py-0.5 text-xs font-medium'
                          : 'inline-flex items-center rounded-full bg-ink-50 dark:bg-white/5 text-ink-400 px-2.5 py-0.5 text-xs font-medium'
                      }>
                        {r}
                      </span>
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
