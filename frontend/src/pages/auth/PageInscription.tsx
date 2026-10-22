import { useState, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { GraduationCap, Loader2, Copy, Check } from 'lucide-react';
import { Bouton } from '@/components/ui/Bouton';
import { etablissementApi, type InscriptionEtablissementEntree, type InscriptionReponse } from '@/lib/etablissementApi';
import { extraireErreurApi } from '@/lib/authApi';

const VIDE: InscriptionEtablissementEntree = {
  nom: '', pays: '', ville: '', typeEtablissement: 'lycee', email: '', telephone: '',
  administrateur: { nom: '', prenom: '', email: '', telephone: '' },
};

/**
 * Inscription en libre-service : cree l'etablissement ET son premier compte
 * ADMIN en un seul appel (POST /etablissements/inscription, seul endpoint
 * public de tout le module). Le mot de passe temporaire n'est affiche
 * qu'une seule fois, exactement comme pour la creation d'un utilisateur par
 * un admin (meme pattern que PageUtilisateurs).
 */
export default function PageInscription() {
  const navigate = useNavigate();
  const [formulaire, setFormulaire] = useState<InscriptionEtablissementEntree>(VIDE);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [reponse, setReponse] = useState<InscriptionReponse | null>(null);
  const [copie, setCopie] = useState(false);

  async function gererSoumission(e: FormEvent) {
    e.preventDefault();
    setEnCours(true);
    setErreur(null);
    try {
      const { data } = await etablissementApi.inscrire(formulaire);
      setReponse(data);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  if (reponse) {
    return (
      <div className="min-h-screen flex items-center justify-center p-6 bg-ink-50 dark:bg-surface-dark">
        <div className="w-full max-w-md space-y-5 text-center">
          <div className="h-12 w-12 rounded-xl bg-emerald-50 dark:bg-emerald-500/15 text-emerald-600 dark:text-emerald-400 flex items-center justify-center mx-auto">
            <Check size={22} />
          </div>
          <h1 className="font-display text-2xl font-bold">{reponse.etablissement.nom} est inscrit !</h1>
          <p className="text-sm text-ink-400">
            Communiquez ce mot de passe temporaire à {reponse.administrateur.prenom} — il devra le changer à la première connexion. Il ne sera plus jamais affiché.
          </p>
          <div className="flex items-center justify-center gap-2">
            <code className="px-3 py-2 rounded-lg bg-white dark:bg-surface-darkAlt border border-ink-100 dark:border-white/10 font-mono text-sm">
              {reponse.motDePasseTemporaire}
            </code>
            <button
              onClick={() => { navigator.clipboard.writeText(reponse.motDePasseTemporaire); setCopie(true); setTimeout(() => setCopie(false), 1500); }}
              className="p-2 rounded-lg hover:bg-ink-100 dark:hover:bg-white/5 text-ink-400"
              aria-label="Copier"
            >
              {copie ? <Check size={16} className="text-emerald-500" /> : <Copy size={16} />}
            </button>
          </div>
          <Bouton className="w-full" onClick={() => navigate('/connexion')}>Aller à la connexion</Bouton>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen grid lg:grid-cols-2 bg-ink-50 dark:bg-surface-dark">
      <div className="hidden lg:flex flex-col justify-between p-12 bg-gradient-to-br from-brand-700 via-brand-600 to-accent-600 text-white">
        <div className="flex items-center gap-2">
          <div className="h-9 w-9 rounded-lg bg-white/15 flex items-center justify-center"><GraduationCap size={20} /></div>
          <span className="font-display font-bold text-xl">EDUCORE</span>
        </div>
        <h1 className="font-display text-4xl font-bold leading-tight max-w-md">
          Inscrivez votre établissement en quelques minutes.
        </h1>
        <p className="text-xs text-white/50">Essai gratuit — aucune carte bancaire requise.</p>
      </div>

      <div className="flex items-center justify-center p-6 sm:p-10 overflow-y-auto">
        <form onSubmit={gererSoumission} className="w-full max-w-md space-y-5 py-8">
          <div>
            <h2 className="font-display text-2xl font-bold">Inscrire mon établissement</h2>
            <p className="text-sm text-ink-400 mt-1">
              Déjà inscrit ? <Link to="/connexion" className="text-brand-500 font-medium">Se connecter</Link>
            </p>
          </div>

          <div className="grid sm:grid-cols-2 gap-4">
            <label className="block sm:col-span-2">
              <span className="text-sm font-medium">Nom de l'établissement</span>
              <input required value={formulaire.nom} onChange={(e) => setFormulaire({ ...formulaire, nom: e.target.value })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            <label className="block">
              <span className="text-sm font-medium">Pays</span>
              <input required value={formulaire.pays} onChange={(e) => setFormulaire({ ...formulaire, pays: e.target.value })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            <label className="block">
              <span className="text-sm font-medium">Ville</span>
              <input value={formulaire.ville} onChange={(e) => setFormulaire({ ...formulaire, ville: e.target.value })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
          </div>

          <hr className="border-ink-100 dark:border-white/10" />
          <p className="text-sm font-medium">Votre compte administrateur</p>

          <div className="grid sm:grid-cols-2 gap-4">
            <label className="block">
              <span className="text-sm font-medium">Prénom</span>
              <input required value={formulaire.administrateur.prenom}
                onChange={(e) => setFormulaire({ ...formulaire, administrateur: { ...formulaire.administrateur, prenom: e.target.value } })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            <label className="block">
              <span className="text-sm font-medium">Nom</span>
              <input required value={formulaire.administrateur.nom}
                onChange={(e) => setFormulaire({ ...formulaire, administrateur: { ...formulaire.administrateur, nom: e.target.value } })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            <label className="block sm:col-span-2">
              <span className="text-sm font-medium">Email</span>
              <input required type="email" value={formulaire.administrateur.email}
                onChange={(e) => setFormulaire({ ...formulaire, administrateur: { ...formulaire.administrateur, email: e.target.value } })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
          </div>

          {erreur && <p className="text-sm text-red-600 bg-red-50 dark:bg-red-500/10 rounded-lg px-3 py-2">{erreur}</p>}

          <Bouton type="submit" disabled={enCours} className="w-full">
            {enCours && <Loader2 size={16} className="animate-spin" />} Inscrire mon établissement
          </Bouton>
        </form>
      </div>
    </div>
  );
}
