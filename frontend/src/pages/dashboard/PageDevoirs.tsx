import { useEffect, useState, type FormEvent } from 'react';
import { FileText, Loader2, Plus, CheckCircle } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { devoirApi } from '@/lib/modulesApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import { useAuth } from '@/hooks/useAuth';
import { possedeUnRole } from '@/types/auth';
import { structureApi, classeApi } from '@/lib/classeApi';
import type { DevoirProfil, DevoirRenduProfil } from '@/types/pedagogie';

export default function PageDevoirs() {
  const { utilisateur } = useAuth();
  const estProf = possedeUnRole(utilisateur, ['ADMIN', 'DIRECTEUR', 'CENSEUR', 'PROFESSEUR']);
  const { classes, matieresClasse, matieres, chargement, chargerMatieres } = useSelecteursPedagogiques();
  const [classeId, setClasseId] = useState('');
  const [classeMatiereId, setClasseMatiereId] = useState('');
  const [devoirs, setDevoirs] = useState<DevoirProfil[]>([]);
  const [devoirActif, setDevoirActif] = useState<number | null>(null);
  const [rendus, setRendus] = useState<DevoirRenduProfil[]>([]);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [correction, setCorrection] = useState<Record<number, { note: string; commentaire: string }>>({});
  const [matiereFormOuvert, setMatiereFormOuvert] = useState(false);
  const [nouvelleMatiere, setNouvelleMatiere] = useState({ code: '', libelle: '', couleur: '#3498db', coefficient: 1, volumeHoraireHebdo: 2 });
  const [modeMatiere, setModeMatiere] = useState<'selection' | 'creation'>('selection');
  const [matiereExistanteId, setMatiereExistanteId] = useState('');

  useEffect(() => { if (classeId) chargerMatieres(Number(classeId)); }, [classeId, chargerMatieres]);

  useEffect(() => {
    if (classeMatiereId) {
      devoirApi.lister(Number(classeMatiereId)).then(({ data }) => setDevoirs(data)).catch(() => setDevoirs([]));
    }
  }, [classeMatiereId]);

  async function ouvrirRendus(devoirId: number) {
    setDevoirActif(devoirId);
    const { data } = await devoirApi.rendus(devoirId);
    setRendus(data);
  }

  async function gererCreation(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!classeMatiereId) return;
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      await devoirApi.creer({
        classeMatiereId: Number(classeMatiereId),
        titre: fd.get('titre') as string,
        description: (fd.get('description') as string) || undefined,
        dateLimite: fd.get('dateLimite') as string,
      });
      setFormulaireOuvert(false);
      const { data } = await devoirApi.lister(Number(classeMatiereId));
      setDevoirs(data);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function corriger(renduId: number) {
    const c = correction[renduId];
    if (!c) return;
    setEnCours(true);
    try {
      await devoirApi.corriger(renduId, {
        note: c.note ? Number(c.note) : undefined,
        correctionCommentaire: c.commentaire || undefined,
      });
      if (devoirActif) await ouvrirRendus(devoirActif);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function creerMatiere(e: FormEvent) {
    e.preventDefault();
    if (!classeId) return;
    setEnCours(true);
    setErreur(null);
    try {
      if (modeMatiere === 'selection') {
        // Sélectionner une matière existante et l'affecter à la classe
        if (!matiereExistanteId) return;
        
        await classeApi.affecterMatiere(Number(classeId), {
          matiereId: Number(matiereExistanteId),
          coefficient: nouvelleMatiere.coefficient,
          volumeHoraireHebdo: nouvelleMatiere.volumeHoraireHebdo
        });
      } else {
        // Créer une nouvelle matière
        if (!nouvelleMatiere.code || !nouvelleMatiere.libelle) return;
        
        const matiereResponse = await structureApi.creerMatiere({
          code: nouvelleMatiere.code,
          libelle: nouvelleMatiere.libelle,
          couleur: nouvelleMatiere.couleur
        });
        
        // Affecter la matière à la classe
        await classeApi.affecterMatiere(Number(classeId), {
          matiereId: matiereResponse.data.id,
          coefficient: nouvelleMatiere.coefficient,
          volumeHoraireHebdo: nouvelleMatiere.volumeHoraireHebdo
        });
      }
      
      setMatiereFormOuvert(false);
      setNouvelleMatiere({ code: '', libelle: '', couleur: '#3498db', coefficient: 1, volumeHoraireHebdo: 2 });
      setMatiereExistanteId('');
      setModeMatiere('selection');
      
      // Recharger les matières de classe
      await chargerMatieres(Number(classeId));
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  if (chargement) {
    return <div className="flex justify-center py-12"><Loader2 size={24} className="animate-spin text-ink-400" /></div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Devoirs</h1>
          <p className="text-sm text-ink-400 mt-1">Publication, rendu et correction en ligne.</p>
        </div>
        {estProf && (
          <Bouton onClick={() => setFormulaireOuvert((v) => !v)} disabled={!classeMatiereId}>
            <Plus size={16} /> Nouveau devoir
          </Bouton>
        )}
      </div>

      <Card>
        <div className="grid sm:grid-cols-2 gap-4">
          <label className="block"><span className="text-sm font-medium">Classe</span>
            <select value={classeId} onChange={(e) => { setClasseId(e.target.value); setClasseMatiereId(''); }}
              className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
              <option value="">Sélectionner…</option>
              {classes.map((c) => <option key={c.id} value={c.id}>{c.libelle}</option>)}
            </select></label>
          <label className="block"><span className="text-sm font-medium">Matière</span>
            <div className="flex gap-2">
              <select value={classeMatiereId} onChange={(e) => setClasseMatiereId(e.target.value)}
                className="mt-1.5 flex-1 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="">Sélectionner…</option>
                {matieresClasse.map((m) => <option key={m.id} value={m.id}>{m.matiereLibelle}</option>)}
              </select>
              <Bouton type="button" variante="discret" onClick={() => setMatiereFormOuvert(true)} disabled={!classeId}><Plus size={16} /></Bouton>
            </div></label>
        </div>
      </Card>

      {matiereFormOuvert && (
        <Card>
          <form onSubmit={creerMatiere} className="space-y-4">
            <div className="flex gap-2 border-b border-ink-100 dark:border-white/10 pb-2">
              <Bouton 
                type="button" 
                variante={modeMatiere === 'selection' ? 'primaire' : 'discret'}
                onClick={() => setModeMatiere('selection')}
                className="text-sm"
              >
                Sélectionner matière existante
              </Bouton>
              <Bouton 
                type="button" 
                variante={modeMatiere === 'creation' ? 'primaire' : 'discret'}
                onClick={() => setModeMatiere('creation')}
                className="text-sm"
              >
                Créer nouvelle matière
              </Bouton>
            </div>

            {modeMatiere === 'selection' ? (
              <div className="space-y-4">
                <label className="block">
                  <span className="text-sm font-medium">Matière existante</span>
                  <select 
                    required 
                    value={matiereExistanteId} 
                    onChange={(e) => setMatiereExistanteId(e.target.value)}
                    className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm"
                  >
                    <option value="">Choisir une matière…</option>
                    {matieres.filter(m => !matieresClasse.some(mc => mc.matiereId === m.id)).map((m) => (
                      <option key={m.id} value={m.id}>{m.libelle} ({m.code})</option>
                    ))}
                  </select>
                  {matieres.filter(m => !matieresClasse.some(mc => mc.matiereId === m.id)).length === 0 && (
                    <p className="text-xs text-ink-400 mt-1">Toutes les matières sont déjà affectées à cette classe.</p>
                  )}
                </label>
              </div>
            ) : (
              <div className="grid sm:grid-cols-2 gap-4">
                <label className="block">
                  <span className="text-sm font-medium">Code</span>
                  <input required value={nouvelleMatiere.code} onChange={(e) => setNouvelleMatiere({ ...nouvelleMatiere, code: e.target.value })}
                    className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
                </label>
                <label className="block">
                  <span className="text-sm font-medium">Libellé</span>
                  <input required value={nouvelleMatiere.libelle} onChange={(e) => setNouvelleMatiere({ ...nouvelleMatiere, libelle: e.target.value })}
                    className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
                </label>
                <label className="block sm:col-span-2">
                  <span className="text-sm font-medium">Couleur</span>
                  <input type="color" value={nouvelleMatiere.couleur} onChange={(e) => setNouvelleMatiere({ ...nouvelleMatiere, couleur: e.target.value })}
                    className="mt-1.5 w-full h-10 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
                </label>
              </div>
            )}

            <div className="grid sm:grid-cols-2 gap-4">
              <label className="block">
                <span className="text-sm font-medium">Coefficient</span>
                <input type="number" required min="1" step="0.5" value={nouvelleMatiere.coefficient} onChange={(e) => setNouvelleMatiere({ ...nouvelleMatiere, coefficient: Number(e.target.value) })}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
              </label>
              <label className="block">
                <span className="text-sm font-medium">Volume horaire hebdo</span>
                <input type="number" required min="1" step="0.5" value={nouvelleMatiere.volumeHoraireHebdo} onChange={(e) => setNouvelleMatiere({ ...nouvelleMatiere, volumeHoraireHebdo: Number(e.target.value) })}
                  className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
              </label>
            </div>

            {erreur && <p className="text-sm text-red-600">{erreur}</p>}
            <div className="flex gap-2">
              <Bouton type="submit" disabled={enCours}>
                {enCours && <Loader2 size={16} className="animate-spin" />} 
                {modeMatiere === 'selection' ? 'Affecter matière' : 'Créer et affecter matière'}
              </Bouton>
              <Bouton type="button" variante="discret" onClick={() => {
                setMatiereFormOuvert(false);
                setModeMatiere('selection');
                setMatiereExistanteId('');
              }}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererCreation} className="grid sm:grid-cols-2 gap-4">
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Titre</span>
              <input required name="titre" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Description</span>
              <textarea name="description" rows={2} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Date limite</span>
              <input required type="date" name="dateLimite" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Publier</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr>
              <th className="px-4 py-3 font-medium">Titre</th>
              <th className="px-4 py-3 font-medium hidden sm:table-cell">Date limite</th>
              <th className="px-4 py-3 font-medium">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {devoirs.length === 0 && <tr><td colSpan={3} className="px-4 py-6 text-center text-ink-400">Aucun devoir.</td></tr>}
            {devoirs.map((d) => (
              <tr key={d.id}>
                <td className="px-4 py-3 font-medium">{d.titre}</td>
                <td className="px-4 py-3 text-ink-400 hidden sm:table-cell">{new Date(d.dateLimite).toLocaleDateString('fr-FR')}</td>
                <td className="px-4 py-3">
                  {estProf && <Bouton variante="discret" onClick={() => ouvrirRendus(d.id)}>Rendus</Bouton>}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>

      {devoirActif && estProf && (
        <Card>
          <h2 className="font-semibold mb-4 flex items-center gap-2"><FileText size={16} /> Rendus du devoir</h2>
          {rendus.length === 0 && <p className="text-sm text-ink-400">Aucun rendu pour l'instant.</p>}
          {rendus.map((r) => (
            <div key={r.id} className="border-t border-ink-100 dark:border-white/10 py-3 flex flex-wrap gap-3 items-end">
              <div className="flex-1 min-w-[120px]">
                <p className="text-sm font-medium">Élève #{r.eleveId}</p>
                <p className="text-xs text-ink-400">{r.statut} — {new Date(r.dateSoumission).toLocaleDateString('fr-FR')}</p>
              </div>
              <input type="number" placeholder="Note" min={0} max={20} step={0.5}
                value={correction[r.id]?.note ?? r.note?.toString() ?? ''}
                onChange={(e) => setCorrection((p) => ({ ...p, [r.id]: { ...p[r.id], note: e.target.value, commentaire: p[r.id]?.commentaire ?? '' } }))}
                className="w-20 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-2 py-1.5 text-sm" />
              <input type="text" placeholder="Commentaire"
                value={correction[r.id]?.commentaire ?? r.correctionCommentaire ?? ''}
                onChange={(e) => setCorrection((p) => ({ ...p, [r.id]: { note: p[r.id]?.note ?? '', commentaire: e.target.value } }))}
                className="flex-1 min-w-[160px] rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-2 py-1.5 text-sm" />
              <Bouton variante="discret" disabled={enCours} onClick={() => corriger(r.id)}>
                <CheckCircle size={14} /> Corriger
              </Bouton>
            </div>
          ))}
        </Card>
      )}
    </div>
  );
}
