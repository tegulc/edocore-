import { useState, useEffect, type FormEvent } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { GraduationCap, Loader2, ShieldCheck } from 'lucide-react';
import { Bouton } from '@/components/ui/Bouton';
import { authApi, extraireErreurApi, type ReponseConnexion } from '@/lib/authApi';
import { useAuth } from '@/hooks/useAuth';

/**
 * Page de connexion. Deux etapes possibles, conformes au Volume 6
 * (Documentation API) :
 *  1) POST /auth/login (email + mot de passe uniquement) - si le compte a le
 *     2FA actif, la reponse contient deuxFaRequis=true + un jetonDeuxFa transitoire,
 *     PAS d'access/refresh token, PAS d'erreur.
 *  2) POST /auth/2fa/verifier (jetonDeuxFa + code) - termine reellement la connexion.
 */
export default function PageConnexion() {
  const navigate = useNavigate();
  const connecter = useAuth((s) => s.connecter);
  const deconnecter = useAuth((s) => s.deconnecter);

  useEffect(() => {
    deconnecter();
  }, [deconnecter]);

  const [email, setEmail] = useState('');
  const [motDePasse, setMotDePasse] = useState('');
  const [codeDeuxFa, setCodeDeuxFa] = useState('');
  const [jetonDeuxFa, setJetonDeuxFa] = useState<string | null>(null);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  function terminerConnexion(utilisateur: NonNullable<ReponseConnexion['utilisateur']>, jeton: string, jetonRafraichissement: string) {
    connecter(utilisateur, jeton, jetonRafraichissement);
    // Politique de mot de passe (cahier des charges 4.3) : expiration ou premiere connexion
    // forcent un changement avant d'utiliser le reste de la plateforme.
    navigate(utilisateur.doitChangerMotDePasse ? '/profil' : '/', { replace: true });
  }

  async function gererEtapeMotDePasse(e: FormEvent) {
    e.preventDefault();
    setEnCours(true);
    setErreur(null);

    try {
      const { data } = await authApi.login(email, motDePasse);
      if (data.deuxFaRequis && data.jetonDeuxFa) {
        setJetonDeuxFa(data.jetonDeuxFa);
      } else if (data.jeton && data.jetonRafraichissement && data.utilisateur) {
        terminerConnexion(data.utilisateur, data.jeton, data.jetonRafraichissement);
      }
    } catch (e) {
      setErreur(extraireErreurApi(e).message);
    } finally {
      setEnCours(false);
    }
  }

  async function gererEtapeDeuxFa(e: FormEvent) {
    e.preventDefault();
    if (!jetonDeuxFa) return;
    setEnCours(true);
    setErreur(null);

    try {
      const { data } = await authApi.verifierDeuxFa(jetonDeuxFa, codeDeuxFa);
      if (data.jeton && data.jetonRafraichissement && data.utilisateur) {
        terminerConnexion(data.utilisateur, data.jeton, data.jetonRafraichissement);
      }
    } catch (e) {
      setErreur(extraireErreurApi(e).message);
    } finally {
      setEnCours(false);
    }
  }

  const etapeDeuxFa = jetonDeuxFa !== null;
  const gererSoumission = etapeDeuxFa ? gererEtapeDeuxFa : gererEtapeMotDePasse;

  return (
    <div className="min-h-screen grid lg:grid-cols-2 bg-ink-50 dark:bg-surface-dark">
      <div className="hidden lg:flex flex-col justify-between p-12 bg-gradient-to-br from-brand-700 via-brand-600 to-accent-600 text-white">
        <div className="flex items-center gap-2">
          <div className="h-9 w-9 rounded-lg bg-white/15 flex items-center justify-center">
            <GraduationCap size={20} />
          </div>
          <span className="font-display font-bold text-xl">EDUCORE</span>
        </div>
        <div>
          <h1 className="font-display text-4xl font-bold leading-tight max-w-md">
            Une seule plateforme pour gérer toute la vie d'un établissement.
          </h1>
          <p className="mt-4 text-white/70 max-w-sm">
            Élèves, enseignants, parents, finances, présence — tout, sans papier.
          </p>
        </div>
        <p className="text-xs text-white/50">© {new Date().getFullYear()} EDUCORE — SIGES</p>
      </div>

      <div className="flex items-center justify-center p-6 sm:p-10">
        <form onSubmit={gererSoumission} className="w-full max-w-sm space-y-5">
          <div className="lg:hidden flex items-center gap-2 mb-8">
            <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-brand-500 to-accent-500" />
            <span className="font-display font-bold text-lg">EDUCORE</span>
          </div>

          {!etapeDeuxFa ? (
            <>
              <div>
                <h2 className="font-display text-2xl font-bold">Connexion</h2>
                <p className="text-sm text-ink-400 mt-1">Accédez à votre espace établissement.</p>
              </div>

              <label className="block">
                <span className="text-sm font-medium">Adresse email</span>
                <input
                  type="email"
                  required
                  autoComplete="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  placeholder="vous@etablissement.com"
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm focus:border-brand-500"
                />
              </label>

              <label className="block">
                <span className="text-sm font-medium">Mot de passe</span>
                <input
                  type="password"
                  required
                  autoComplete="current-password"
                  value={motDePasse}
                  onChange={(e) => setMotDePasse(e.target.value)}
                  placeholder="••••••••"
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm focus:border-brand-500"
                />
              </label>
            </>
          ) : (
            <>
              <div>
                <div className="h-10 w-10 rounded-lg bg-brand-50 dark:bg-brand-500/15 flex items-center justify-center text-brand-600 dark:text-brand-300 mb-3">
                  <ShieldCheck size={20} />
                </div>
                <h2 className="font-display text-2xl font-bold">Vérification en deux étapes</h2>
                <p className="text-sm text-ink-400 mt-1">
                  Saisissez le code à 6 chiffres généré par votre application d'authentification.
                </p>
              </div>

              <label className="block">
                <span className="text-sm font-medium">Code de vérification</span>
                <input
                  type="text"
                  inputMode="numeric"
                  pattern="\d{6}"
                  maxLength={6}
                  required
                  autoFocus
                  value={codeDeuxFa}
                  onChange={(e) => setCodeDeuxFa(e.target.value.replace(/\D/g, ''))}
                  placeholder="123456"
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm tracking-[0.3em] text-center font-mono focus:border-brand-500"
                />
              </label>
            </>
          )}

          {erreur && (
            <p className="text-sm text-red-600 bg-red-50 dark:bg-red-500/10 rounded-lg px-3 py-2">{erreur}</p>
          )}

          <Bouton type="submit" disabled={enCours} className="w-full">
            {enCours && <Loader2 size={16} className="animate-spin" />}
            {etapeDeuxFa ? 'Vérifier' : 'Se connecter'}
          </Bouton>

          {!etapeDeuxFa && (
            <p className="text-center text-xs text-ink-400">
              Mot de passe oublié ? <a href="#" className="text-brand-500 font-medium">Réinitialiser</a>
            </p>
          )}
          {!etapeDeuxFa && (
            <p className="text-center text-xs text-ink-400">
              Pas encore inscrit ? <Link to="/inscription" className="text-brand-500 font-medium">Inscrire mon établissement</Link>
            </p>
          )}
          {etapeDeuxFa && (
            <button
              type="button"
              onClick={() => setJetonDeuxFa(null)}
              className="w-full text-center text-xs text-ink-400 hover:text-brand-500"
            >
              ← Retour
            </button>
          )}
        </form>
      </div>
    </div>
  );
}
