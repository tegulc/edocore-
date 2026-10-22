import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { QRCodeSVG } from 'qrcode.react';
import { ShieldCheck, ShieldOff, Loader2, KeyRound } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { authApi, extraireErreurApi } from '@/lib/authApi';
import { useAuth } from '@/hooks/useAuth';

const REGEX_MOT_DE_PASSE = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,}$/;

/**
 * Page "Mon profil" : changement de mot de passe + activation/etat du 2FA.
 */
export default function PageProfil() {
  const navigate = useNavigate();
  const { utilisateur, connecter } = useAuth();
  const [provisionnement, setProvisionnement] = useState<{ secretBase32: string; uriProvisionnement: string } | null>(null);
  const [codeConfirmation, setCodeConfirmation] = useState('');
  const [chargement, setChargement] = useState(false);
  const [message, setMessage] = useState<{ type: 'succes' | 'erreur'; texte: string } | null>(null);

  const [ancienMotDePasse, setAncienMotDePasse] = useState('');
  const [nouveauMotDePasse, setNouveauMotDePasse] = useState('');
  const [confirmerMotDePasse, setConfirmerMotDePasse] = useState('');
  const [changementEnCours, setChangementEnCours] = useState(false);
  const [messageMotDePasse, setMessageMotDePasse] = useState<{ type: 'succes' | 'erreur'; texte: string } | null>(null);

  async function gererChangementMotDePasse(e: FormEvent) {
    e.preventDefault();
    setMessageMotDePasse(null);

    if (!REGEX_MOT_DE_PASSE.test(nouveauMotDePasse)) {
      setMessageMotDePasse({
        type: 'erreur',
        texte: 'Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre.',
      });
      return;
    }

    if (nouveauMotDePasse !== confirmerMotDePasse) {
      setMessageMotDePasse({ type: 'erreur', texte: 'La confirmation ne correspond pas au nouveau mot de passe.' });
      return;
    }

    if (ancienMotDePasse === nouveauMotDePasse) {
      setMessageMotDePasse({ type: 'erreur', texte: 'Le nouveau mot de passe doit être différent de l\'actuel.' });
      return;
    }

    setChangementEnCours(true);
    try {
      const { data } = await authApi.changerMotDePasse(ancienMotDePasse, nouveauMotDePasse);
      if (data.jeton && data.jetonRafraichissement && data.utilisateur) {
        connecter(data.utilisateur, data.jeton, data.jetonRafraichissement);
      }
      setMessageMotDePasse({
        type: 'succes',
        texte: 'Mot de passe modifié avec succès. Vos autres sessions ont été déconnectées par sécurité.',
      });
      setAncienMotDePasse('');
      setNouveauMotDePasse('');
      setConfirmerMotDePasse('');
      if (data.utilisateur && !data.utilisateur.doitChangerMotDePasse) {
        setTimeout(() => navigate('/', { replace: true }), 1500);
      }
    } catch (err) {
      setMessageMotDePasse({ type: 'erreur', texte: extraireErreurApi(err).message });
    } finally {
      setChangementEnCours(false);
    }
  }

  async function demarrerActivation() {
    setChargement(true);
    setMessage(null);
    try {
      const { data } = await authApi.initierDeuxFa();
      setProvisionnement(data);
    } catch (e) {
      setMessage({ type: 'erreur', texte: extraireErreurApi(e).message });
    } finally {
      setChargement(false);
    }
  }

  async function confirmerActivation() {
    setChargement(true);
    setMessage(null);
    try {
      await authApi.confirmerDeuxFa(codeConfirmation);
      setMessage({ type: 'succes', texte: 'Authentification à deux facteurs activée avec succès.' });
      setProvisionnement(null);
      setCodeConfirmation('');
    } catch (e) {
      setMessage({ type: 'erreur', texte: extraireErreurApi(e).message });
    } finally {
      setChargement(false);
    }
  }

  if (!utilisateur) return null;

  return (
    <div className="space-y-6 max-w-2xl">
      <div>
        <h1 className="font-display text-2xl font-bold">Mon profil</h1>
        <p className="text-sm text-ink-400 mt-1">{utilisateur.prenom} {utilisateur.nom} — {utilisateur.email}</p>
      </div>

      {utilisateur.doitChangerMotDePasse && (
        <p className="text-sm text-amber-800 bg-amber-50 dark:bg-amber-500/10 dark:text-amber-300 rounded-lg px-4 py-3">
          Vous devez changer votre mot de passe temporaire avant de continuer.
          Saisissez le mot de passe reçu à l&apos;inscription dans le champ « Mot de passe actuel ».
        </p>
      )}

      <Card>
        <div className="flex items-start gap-3">
          <div className="h-10 w-10 rounded-lg bg-ink-50 dark:bg-white/5 flex items-center justify-center text-ink-400 shrink-0">
            <KeyRound size={20} />
          </div>
          <div className="flex-1">
            <h2 className="font-display font-semibold">Mot de passe</h2>
            <p className="text-sm text-ink-400 mt-0.5">
              Doit contenir au moins 8 caractères, une majuscule, une minuscule et un chiffre.
            </p>

            <form onSubmit={gererChangementMotDePasse} className="mt-4 space-y-3 max-w-xs">
              <label className="block">
                <span className="text-sm font-medium">Mot de passe actuel</span>
                <input
                  type="password"
                  required
                  autoComplete="current-password"
                  value={ancienMotDePasse}
                  onChange={(e) => setAncienMotDePasse(e.target.value)}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm"
                />
              </label>
              <label className="block">
                <span className="text-sm font-medium">Nouveau mot de passe</span>
                <input
                  type="password"
                  required
                  minLength={8}
                  autoComplete="new-password"
                  value={nouveauMotDePasse}
                  onChange={(e) => setNouveauMotDePasse(e.target.value)}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm"
                />
              </label>
              <label className="block">
                <span className="text-sm font-medium">Confirmer le nouveau mot de passe</span>
                <input
                  type="password"
                  required
                  minLength={8}
                  autoComplete="new-password"
                  value={confirmerMotDePasse}
                  onChange={(e) => setConfirmerMotDePasse(e.target.value)}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm"
                />
              </label>

              {messageMotDePasse && (
                <p className={
                  messageMotDePasse.type === 'succes'
                    ? 'text-sm text-emerald-700 bg-emerald-50 dark:bg-emerald-500/10 dark:text-emerald-400 rounded-lg px-3 py-2'
                    : 'text-sm text-red-600 bg-red-50 dark:bg-red-500/10 rounded-lg px-3 py-2'
                }>
                  {messageMotDePasse.texte}
                </p>
              )}

              <Bouton type="submit" disabled={changementEnCours}>
                {changementEnCours && <Loader2 size={16} className="animate-spin" />} Mettre à jour
              </Bouton>
            </form>
          </div>
        </div>
      </Card>

      <Card>
        <div className="flex items-start justify-between gap-4">
          <div className="flex items-start gap-3">
            <div className={
              utilisateur.deuxFaActive
                ? 'h-10 w-10 rounded-lg bg-emerald-50 dark:bg-emerald-500/15 flex items-center justify-center text-emerald-600 dark:text-emerald-400 shrink-0'
                : 'h-10 w-10 rounded-lg bg-ink-50 dark:bg-white/5 flex items-center justify-center text-ink-400 shrink-0'
            }>
              {utilisateur.deuxFaActive ? <ShieldCheck size={20} /> : <ShieldOff size={20} />}
            </div>
            <div>
              <h2 className="font-display font-semibold">Authentification à deux facteurs</h2>
              <p className="text-sm text-ink-400 mt-0.5">
                {utilisateur.deuxFaActive
                  ? 'Activée — un code est demandé à chaque connexion.'
                  : 'Non activée — recommandée pour les comptes administrateurs.'}
              </p>
            </div>
          </div>
          {!utilisateur.deuxFaActive && !provisionnement && (
            <Bouton variante="discret" onClick={demarrerActivation} disabled={chargement}>
              {chargement ? <Loader2 size={16} className="animate-spin" /> : 'Activer'}
            </Bouton>
          )}
        </div>

        {provisionnement && (
          <div className="mt-5 pt-5 border-t border-ink-100 dark:border-white/10 space-y-4">
            <p className="text-sm">
              Scannez ce QR code avec Google Authenticator, Microsoft Authenticator, ou une application compatible.
            </p>
            <div className="bg-white p-3 rounded-lg inline-block">
              <QRCodeSVG value={provisionnement.uriProvisionnement} size={160} />
            </div>
            <p className="text-xs text-ink-400 font-mono">Secret : {provisionnement.secretBase32}</p>

            <label className="block max-w-xs">
              <span className="text-sm font-medium">Code de confirmation</span>
              <input
                type="text"
                inputMode="numeric"
                maxLength={6}
                value={codeConfirmation}
                onChange={(e) => setCodeConfirmation(e.target.value.replace(/\D/g, ''))}
                placeholder="123456"
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm font-mono tracking-widest text-center"
              />
            </label>

            <Bouton onClick={confirmerActivation} disabled={chargement || codeConfirmation.length !== 6}>
              {chargement ? <Loader2 size={16} className="animate-spin" /> : 'Confirmer l\'activation'}
            </Bouton>
          </div>
        )}

        {message && (
          <p className={
            message.type === 'succes'
              ? 'mt-4 text-sm text-emerald-700 bg-emerald-50 dark:bg-emerald-500/10 dark:text-emerald-400 rounded-lg px-3 py-2'
              : 'mt-4 text-sm text-red-600 bg-red-50 dark:bg-red-500/10 rounded-lg px-3 py-2'
          }>
            {message.texte}
          </p>
        )}
      </Card>
    </div>
  );
}
