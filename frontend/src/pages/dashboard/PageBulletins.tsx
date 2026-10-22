import { useState } from 'react';
import { Award, Loader2, Send, X, Download, Trophy } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { bulletinApi } from '@/lib/noteApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import BulletinDetaille from '@/components/bulletin/BulletinDetaille';
import type { BulletinProfil } from '@/types/note';

export default function PageBulletins() {
  const { classes, periodes, chargement } = useSelecteursPedagogiques();
  const [classeId, setClasseId] = useState('');
  const [periodeId, setPeriodeId] = useState('');
  const [bulletins, setBulletins] = useState<BulletinProfil[]>([]);
  const [bulletinSelectionne, setBulletinSelectionne] = useState<BulletinProfil | null>(null);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  async function recalculer() {
    if (!classeId || !periodeId) return;
    setEnCours(true);
    setErreur(null);
    try {
      await bulletinApi.recalculerMoyennes(Number(classeId), Number(periodeId));
      setMessage('Moyennes recalculées avec succès.');
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function generer() {
    if (!classeId || !periodeId) return;
    setEnCours(true);
    setErreur(null);
    try {
      const { data } = await bulletinApi.genererBulletins(Number(classeId), Number(periodeId));
      setBulletins(data);
      setMessage(`${data.length} bulletin(s) généré(s).`);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function valider(id: number) {
    setEnCours(true);
    try {
      const { data } = await bulletinApi.valider(id);
      setBulletins((prev) => prev.map((b) => (b.id === id ? data : b)));
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function publier(id: number) {
    setEnCours(true);
    try {
      const { data } = await bulletinApi.publier(id);
      setBulletins((prev) => prev.map((b) => (b.id === id ? data : b)));
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  function voirDetail(bulletin: BulletinProfil) {
    setBulletinSelectionne(bulletin);
  }

  function fermerDetail() {
    setBulletinSelectionne(null);
  }

  async function telechargerBulletin(bulletin: BulletinProfil) {
    try {
      const response = await bulletinApi.telechargerPdf(bulletin.id);
      const url = window.URL.createObjectURL(new Blob([response.data], { type: 'application/pdf' }));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `bulletin_${bulletin.eleveNom.replace(/\s+/g, '_')}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      setMessage('PDF téléchargé avec succès');
    } catch (err) {
      setErreur('Erreur lors du téléchargement du PDF');
    }
  }

  function envoyerBulletin() {
    // TODO: Implémenter l'envoi par email
    setMessage('Fonctionnalité d\'envoi par email à venir');
  }

  function archiverBulletin() {
    // TODO: Implémenter l'archivage
    setMessage('Fonctionnalité d\'archivage à venir');
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Bulletins</h1>
        <p className="text-sm text-ink-400 mt-1">Génération, validation et publication des bulletins scolaires.</p>
      </div>

      <Card>
        <div className="grid sm:grid-cols-2 gap-4 mb-4">
          <label className="block">
            <span className="text-sm font-medium">Classe</span>
            <select value={classeId} onChange={(e) => setClasseId(e.target.value)}
              className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
              <option value="">{chargement ? 'Chargement…' : 'Choisir…'}</option>
              {classes.map((c) => <option key={c.id} value={c.id}>{c.libelle}</option>)}
            </select>
          </label>
          <label className="block">
            <span className="text-sm font-medium">Période</span>
            <select value={periodeId} onChange={(e) => setPeriodeId(e.target.value)}
              className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
              <option value="">Choisir…</option>
              {periodes.map((p) => <option key={p.id} value={p.id}>{p.libelle}</option>)}
            </select>
          </label>
        </div>
        <div className="flex flex-wrap gap-2">
          <Bouton onClick={recalculer} disabled={enCours || !classeId || !periodeId}>
            {enCours && <Loader2 size={16} className="animate-spin" />} Recalculer moyennes
          </Bouton>
          <Bouton onClick={generer} disabled={enCours || !classeId || !periodeId}>
            <Award size={16} /> Générer bulletins
          </Bouton>
          <Bouton onClick={() => {/* TODO: Voir tableau d'honneur */}} disabled={!classeId || !periodeId} variante="discret">
            <Trophy size={16} /> Tableau d'honneur
          </Bouton>
        </div>
        {message && <p className="mt-3 text-sm text-emerald-600">{message}</p>}
        {erreur && <p className="mt-3 text-sm text-red-600">{erreur}</p>}
      </Card>

      {bulletinSelectionne ? (
        <div className="relative">
          <button 
            onClick={fermerDetail}
            className="absolute top-4 right-4 p-2 rounded-full bg-ink-100 dark:bg-white/10 hover:bg-ink-200 dark:hover:bg-white/20 z-10"
          >
            <X size={20} />
          </button>
          <BulletinDetaille
            bulletin={bulletinSelectionne}
            classeNom={classes.find(c => c.id === Number(classeId))?.libelle || 'Classe inconnue'}
            periodeNom={periodes.find(p => p.id === Number(periodeId))?.libelle || 'Période inconnue'}
            onTelecharger={(bulletin) => telechargerBulletin(bulletin)}
            onEnvoyer={envoyerBulletin}
            onArchiver={archiverBulletin}
          />
        </div>
      ) : bulletins.length > 0 && (
        <Card className="p-0 overflow-hidden">
          <table className="w-full text-sm">
            <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
              <tr>
                <th className="px-4 py-3 font-medium">Élève</th>
                <th className="px-4 py-3 font-medium">Moyenne</th>
                <th className="px-4 py-3 font-medium">Rang</th>
                <th className="px-4 py-3 font-medium">Absences</th>
                <th className="px-4 py-3 font-medium">Statut</th>
                <th className="px-4 py-3 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {bulletins.map((b) => (
                <tr key={b.id}>
                  <td className="px-4 py-3 font-medium">{b.eleveNom}</td>
                  <td className="px-4 py-3">{b.moyenneGenerale?.toFixed(2) ?? '—'}</td>
                  <td className="px-4 py-3">{b.rang ?? '—'}</td>
                  <td className="px-4 py-3">{b.nombreAbsences}</td>
                  <td className="px-4 py-3 capitalize">{b.statut}</td>
                  <td className="px-4 py-3 flex gap-2">
                    <Bouton variante="discret" onClick={() => voirDetail(b)} disabled={enCours}>
                      <Award size={14} /> Voir bulletin
                    </Bouton>
                    <Bouton variante="discret" onClick={() => telechargerBulletin(b)} disabled={enCours}>
                      <Download size={14} /> PDF
                    </Bouton>
                    {b.statut === 'brouillon' && (
                      <Bouton variante="discret" onClick={() => valider(b.id)} disabled={enCours}>Valider</Bouton>
                    )}
                    {b.statut === 'valide' && (
                      <Bouton variante="discret" onClick={() => publier(b.id)} disabled={enCours}>
                        <Send size={14} /> Publier
                      </Bouton>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      )}
    </div>
  );
}
