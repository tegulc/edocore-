import { useEffect, useState } from 'react';
import { GraduationCap, Award, Wallet, ClipboardCheck, Loader2, BookMarked, ClipboardList } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { useAuth } from '@/hooks/useAuth';
import { possedeUnRole } from '@/types/auth';
import { portailApi, cahierApi, devoirApi } from '@/lib/modulesApi';
import { bulletinApi } from '@/lib/noteApi';
import { echeancierApi, paiementApi } from '@/lib/paiementApi';
import { presenceApi } from '@/lib/emploiDuTempsApi';
import { anneeScolaireApi } from '@/lib/etablissementApi';
import { eleveApi } from '@/lib/eleveApi';
import { classeApi } from '@/lib/classeApi';
import type { EleveProfil } from '@/types/eleve';
import type { BulletinProfil } from '@/types/note';
import type { EcheancierProfil, PaiementProfil } from '@/types/paiement';
import type { BilanPresence } from '@/types/emploiDuTemps';
import type { CahierDeTexteProfil, DevoirProfil } from '@/types/pedagogie';

export default function PageMonEspace() {
  const { utilisateur } = useAuth();
  const estParent = possedeUnRole(utilisateur, ['PARENT']);
  const estEleve = possedeUnRole(utilisateur, ['ELEVE']);

  const [enfants, setEnfants] = useState<EleveProfil[]>([]);
  const [monProfil, setMonProfil] = useState<EleveProfil | null>(null);
  const [eleveActif, setEleveActif] = useState<number | null>(null);
  const [bulletin, setBulletin] = useState<BulletinProfil | null>(null);
  const [echeanciers, setEcheanciers] = useState<EcheancierProfil[]>([]);
  const [paiements, setPaiements] = useState<PaiementProfil[]>([]);
  const [bilan, setBilan] = useState<BilanPresence | null>(null);
  const [cahier, setCahier] = useState<CahierDeTexteProfil[]>([]);
  const [devoirs, setDevoirs] = useState<DevoirProfil[]>([]);
  const [chargement, setChargement] = useState(true);
  const [enCours, setEnCours] = useState(false);

  useEffect(() => {
    async function init() {
      try {
        if (estParent) {
          const { data } = await portailApi.mesEnfants();
          setEnfants(data);
          if (data.length > 0) setEleveActif(data[0].id);
        } else if (estEleve) {
          const { data } = await portailApi.monDossierEleve();
          setMonProfil(data);
          setEleveActif(data.id);
        }
      } finally {
        setChargement(false);
      }
    }
    init();
  }, [estParent, estEleve]);

  useEffect(() => {
    if (!eleveActif) return;
    const debut = new Date(new Date().getFullYear(), 0, 1).toISOString().slice(0, 10);
    const fin = new Date().toISOString().slice(0, 10);

    Promise.all([
      anneeScolaireApi.active().then(({ data: annee }) =>
        anneeScolaireApi.periodes(annee.id).then(({ data: periodes }) => {
          const active = periodes.find((p) => !p.estCloturee) ?? periodes[0];
          if (active) return bulletinApi.obtenirPourEleve(eleveActif, active.id).then(({ data }) => setBulletin(data)).catch(() => setBulletin(null));
        })
      ),
      echeancierApi.lister(eleveActif).then(({ data }) => setEcheanciers(data)).catch(() => setEcheanciers([])),
      paiementApi.historique(eleveActif).then(({ data }) => setPaiements(data)).catch(() => setPaiements([])),
      presenceApi.bilan(eleveActif, debut, fin).then(({ data }) => setBilan(data)).catch(() => setBilan(null)),
    ]);

    async function chargerPedagogie() {
      if (!eleveActif) return;
      try {
        const { data: inscriptions } = await eleveApi.inscriptions(eleveActif);
        const inscriptionActive = inscriptions.find((i) => i.statut === 'validee') ?? inscriptions[0];
        if (!inscriptionActive) return;
        const { data: matieres } = await classeApi.matieres(inscriptionActive.classeId);
        const tousCahiers: CahierDeTexteProfil[] = [];
        const tousDevoirs: DevoirProfil[] = [];
        for (const m of matieres) {
          const [c, d] = await Promise.all([
            cahierApi.lister(m.id).then(({ data }) => data).catch(() => []),
            devoirApi.lister(m.id).then(({ data }) => data).catch(() => []),
          ]);
          tousCahiers.push(...c);
          tousDevoirs.push(...d);
        }
        setCahier(tousCahiers.sort((a, b) => b.dateCours.localeCompare(a.dateCours)).slice(0, 5));
        setDevoirs(tousDevoirs.sort((a, b) => b.dateLimite.localeCompare(a.dateLimite)).slice(0, 5));
      } catch {
        setCahier([]);
        setDevoirs([]);
      }
    }
    chargerPedagogie();
  }, [eleveActif]);

  async function rendreDevoir(devoirId: number) {
    setEnCours(true);
    try {
      await devoirApi.rendre({ devoirId, fichierReponseUrl: 'rendu-en-ligne' });
    } finally {
      setEnCours(false);
    }
  }

  if (chargement) {
    return <div className="flex justify-center py-12"><Loader2 size={24} className="animate-spin text-ink-400" /></div>;
  }

  const elevesAffichables = estParent ? enfants : monProfil ? [monProfil] : [];

  if (elevesAffichables.length === 0) {
    return (
      <div className="space-y-6">
        <h1 className="font-display text-2xl font-bold">Mon espace</h1>
        <Card><p className="text-sm text-ink-400">Aucun dossier élève associé à votre compte. Contactez le secrétariat.</p></Card>
      </div>
    );
  }

  const eleveCourant = elevesAffichables.find((e) => e.id === eleveActif);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Mon espace</h1>
        <p className="text-sm text-ink-400 mt-1">
          {estParent ? 'Suivi scolaire de vos enfants.' : 'Votre tableau de bord élève.'}
        </p>
      </div>

      {estParent && enfants.length > 1 && (
        <Card>
          <label className="block max-w-md">
            <span className="text-sm font-medium">Enfant</span>
            <select value={eleveActif ?? ''} onChange={(e) => setEleveActif(Number(e.target.value))}
              className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
              {enfants.map((e) => <option key={e.id} value={e.id}>{e.prenom} {e.nom}</option>)}
            </select>
          </label>
        </Card>
      )}

      {eleveCourant && (
        <Card>
          <div className="flex items-center gap-3">
            <div className="h-12 w-12 rounded-full bg-brand-50 dark:bg-brand-500/15 flex items-center justify-center">
              <GraduationCap size={20} className="text-brand-600" />
            </div>
            <div>
              <p className="font-semibold">{eleveCourant.prenom} {eleveCourant.nom}</p>
              <p className="text-xs text-ink-400 font-mono">{eleveCourant.matricule}</p>
            </div>
          </div>
        </Card>
      )}

      <div className="grid sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <Card>
          <div className="flex items-center gap-2 text-ink-400 text-sm"><Award size={16} /> Bulletin</div>
          <p className="font-display text-2xl font-bold mt-2">
            {bulletin?.moyenneGenerale != null ? bulletin.moyenneGenerale.toFixed(2) : '—'}
            <span className="text-sm font-normal text-ink-400"> /20</span>
          </p>
          {bulletin?.rang && <p className="text-xs text-ink-400 mt-1">Rang : {bulletin.rang}</p>}
        </Card>
        <Card>
          <div className="flex items-center gap-2 text-ink-400 text-sm"><ClipboardCheck size={16} /> Présence</div>
          <p className="font-display text-2xl font-bold mt-2">{bilan ? bilan.retards : '—'}</p>
          {bilan && <p className="text-xs text-ink-400 mt-1">{bilan.absencesNonJustifiees} absence(s) non justifiée(s)</p>}
        </Card>
        <Card>
          <div className="flex items-center gap-2 text-ink-400 text-sm"><Wallet size={16} /> Paiements</div>
          <p className="font-display text-2xl font-bold mt-2">{paiements.filter((p) => p.statut === 'confirme').length}</p>
          <p className="text-xs text-ink-400 mt-1">confirmé(s) cette année</p>
        </Card>
        <Card>
          <div className="flex items-center gap-2 text-ink-400 text-sm"><Wallet size={16} /> Échéancier</div>
          <p className="font-display text-2xl font-bold mt-2">
            {echeanciers.length > 0
              ? echeanciers[0].tranches.filter((t) => t.statut === 'paye').length + '/' + echeanciers[0].tranches.length
              : '—'}
          </p>
          <p className="text-xs text-ink-400 mt-1">tranches payées</p>
        </Card>
      </div>

      {cahier.length > 0 && (
        <Card>
          <h2 className="font-semibold mb-3 flex items-center gap-2"><BookMarked size={16} /> Cahier de texte récent</h2>
          {cahier.map((e) => (
            <div key={e.id} className="py-2 border-t border-ink-100 dark:border-white/10 first:border-0 first:pt-0">
              <p className="text-sm font-medium">{e.chapitre ?? 'Cours'} — {e.lecon ?? ''}</p>
              <p className="text-xs text-ink-400">{new Date(e.dateCours).toLocaleDateString('fr-FR')}</p>
              {e.contenu && <p className="text-sm mt-1 text-ink-600 dark:text-ink-300">{e.contenu}</p>}
            </div>
          ))}
        </Card>
      )}

      {devoirs.length > 0 && (
        <Card>
          <h2 className="font-semibold mb-3 flex items-center gap-2"><ClipboardList size={16} /> Devoirs</h2>
          {devoirs.map((d) => (
            <div key={d.id} className="py-2 border-t border-ink-100 dark:border-white/10 first:border-0 first:pt-0 flex justify-between items-center gap-2">
              <div>
                <p className="text-sm font-medium">{d.titre}</p>
                <p className="text-xs text-ink-400">À rendre avant le {new Date(d.dateLimite).toLocaleDateString('fr-FR')}</p>
              </div>
              {estEleve && (
                <Bouton variante="discret" disabled={enCours} onClick={() => rendreDevoir(d.id)}>Rendre</Bouton>
              )}
            </div>
          ))}
        </Card>
      )}

      {bulletin && bulletin.moyennesParMatiere.length > 0 && (
        <Card className="p-0 overflow-hidden">
          <h2 className="px-4 py-3 font-semibold border-b">Moyennes par matière</h2>
          <table className="w-full text-sm">
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {bulletin.moyennesParMatiere.map((m) => (
                <tr key={m.classeMatiereId}>
                  <td className="px-4 py-3">{m.matiereLibelle}</td>
                  <td className="px-4 py-3 font-medium">{m.moyenne?.toFixed(2) ?? '—'}</td>
                  <td className="px-4 py-3 text-ink-400">{m.rangMatiere ? `Rang ${m.rangMatiere}` : '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  );
}
