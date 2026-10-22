import { useEffect, useState, type FormEvent } from 'react';
import { FileText, Loader2, CheckCircle, Plus } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { evaluationApi } from '@/lib/noteApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import { structureApi, classeApi } from '@/lib/classeApi';
import type { EvaluationProfil, NoteProfil } from '@/types/note';

export default function PageNotes() {
  const { classes, periodes, matieresClasse, matieres, chargement, chargerMatieres } = useSelecteursPedagogiques();
  const [classeId, setClasseId] = useState('');
  const [classeMatiereId, setClasseMatiereId] = useState('');
  const [periodeId, setPeriodeId] = useState('');
  const [evaluations, setEvaluations] = useState<EvaluationProfil[]>([]);
  const [evaluationActive, setEvaluationActive] = useState<number | null>(null);
  const [evaluationActiveObjet, setEvaluationActiveObjet] = useState<EvaluationProfil | null>(null);
  const [notes, setNotes] = useState<NoteProfil[]>([]);
  const [saisieNotes, setSaisieNotes] = useState<Record<number, { valeur: string; absent: boolean }>>({});
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [nouvelleEval, setNouvelleEval] = useState({ titre: '', typeEvaluationCode: 'DEVOIR', dateEvaluation: '', bareme: 20 });
  const [erreur, setErreur] = useState<string | null>(null);
  const [enCours, setEnCours] = useState(false);
  const [matiereFormOuvert, setMatiereFormOuvert] = useState(false);
  const [nouvelleMatiere, setNouvelleMatiere] = useState({ code: '', libelle: '', couleur: '#3498db', coefficient: 1, volumeHoraireHebdo: 2 });
  const [modeMatiere, setModeMatiere] = useState<'selection' | 'creation'>('selection');
  const [matiereExistanteId, setMatiereExistanteId] = useState('');

  useEffect(() => {
    if (classeId) chargerMatieres(Number(classeId));
  }, [classeId, chargerMatieres]);

  useEffect(() => {
    if (classeMatiereId && periodeId) {
      evaluationApi.lister(Number(classeMatiereId), Number(periodeId))
        .then(({ data }) => setEvaluations(data))
        .catch(() => setEvaluations([]));
    }
  }, [classeMatiereId, periodeId]);

  async function ouvrirSaisie(evalId: number) {
    setEvaluationActive(evalId);
    const evalObjet = evaluations.find(e => e.id === evalId);
    setEvaluationActiveObjet(evalObjet || null);
    const { data } = await evaluationApi.notes(evalId);
    setNotes(data);
    const init: Record<number, { valeur: string; absent: boolean }> = {};
    data.forEach((n) => { init[n.eleveId] = { valeur: n.valeur?.toString() ?? '', absent: n.absent }; });
    setSaisieNotes(init);
  }

  async function gererCreation(e: FormEvent) {
    e.preventDefault();
    if (!classeMatiereId || !periodeId) return;
    setEnCours(true);
    setErreur(null);
    try {
      await evaluationApi.creer({
        classeMatiereId: Number(classeMatiereId),
        typeEvaluationCode: nouvelleEval.typeEvaluationCode,
        periodeId: Number(periodeId),
        titre: nouvelleEval.titre,
        dateEvaluation: nouvelleEval.dateEvaluation,
        bareme: nouvelleEval.bareme,
      });
      setFormulaireOuvert(false);
      const { data } = await evaluationApi.lister(Number(classeMatiereId), Number(periodeId));
      setEvaluations(data);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function enregistrerNotes() {
    if (!evaluationActive) return;
    setEnCours(true);
    setErreur(null);
    try {
      const payload = Object.entries(saisieNotes).map(([eleveId, n]) => ({
        eleveId: Number(eleveId),
        valeur: n.absent ? undefined : (n.valeur ? Number(n.valeur) : undefined),
        absent: n.absent,
      }));
      await evaluationApi.saisirNotes(evaluationActive, payload);
      await ouvrirSaisie(evaluationActive);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function validerEvaluation(id: number) {
    setEnCours(true);
    try {
      await evaluationApi.valider(id);
      if (classeMatiereId && periodeId) {
        const { data } = await evaluationApi.lister(Number(classeMatiereId), Number(periodeId));
        setEvaluations(data);
      }
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
      console.error('Erreur lors de la création/affectation de matière:', err);
      const erreurInfo = extraireErreurApi(err);
      console.error('Détails de l\'erreur:', erreurInfo);
      setErreur(erreurInfo.message || 'Une erreur inattendue est survenue.');
    } finally {
      setEnCours(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Notes</h1>
          <p className="text-sm text-ink-400 mt-1">Saisie et validation des évaluations.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)} disabled={!classeMatiereId || !periodeId}>
          <FileText size={16} /> Nouvelle évaluation
        </Bouton>
      </div>

      <Card>
        <div className="grid sm:grid-cols-3 gap-4">
          <label className="block">
            <span className="text-sm font-medium">Classe</span>
            <select value={classeId} onChange={(e) => { setClasseId(e.target.value); setClasseMatiereId(''); }}
              className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
              <option value="">{chargement ? 'Chargement…' : 'Choisir…'}</option>
              {classes.map((c) => <option key={c.id} value={c.id}>{c.libelle}</option>)}
            </select>
          </label>
          <label className="block">
            <span className="text-sm font-medium">Matière</span>
            <div className="flex gap-2">
              <select value={classeMatiereId} onChange={(e) => setClasseMatiereId(e.target.value)}
                className="mt-1.5 flex-1 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="">Choisir…</option>
                {matieresClasse.map((m) => <option key={m.id} value={m.id}>{m.matiereLibelle}</option>)}
              </select>
              <Bouton type="button" variante="discret" onClick={() => setMatiereFormOuvert(true)} disabled={!classeId}><Plus size={16} /></Bouton>
            </div>
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
            <label className="block sm:col-span-2">
              <span className="text-sm font-medium">Titre</span>
              <input required value={nouvelleEval.titre} onChange={(e) => setNouvelleEval({ ...nouvelleEval, titre: e.target.value })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            <label className="block">
              <span className="text-sm font-medium">Type</span>
              <select value={nouvelleEval.typeEvaluationCode} onChange={(e) => setNouvelleEval({ ...nouvelleEval, typeEvaluationCode: e.target.value })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="DEVOIR">Devoir</option>
                <option value="SEQUENCE">Séquence</option>
                <option value="COMPOSITION">Composition</option>
                <option value="EXAMEN">Examen</option>
                <option value="PROJET">Projet</option>
                <option value="TP">Travaux pratiques</option>
              </select>
            </label>
            <label className="block">
              <span className="text-sm font-medium">Date</span>
              <input required type="date" value={nouvelleEval.dateEvaluation} onChange={(e) => setNouvelleEval({ ...nouvelleEval, dateEvaluation: e.target.value })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600 bg-red-50 dark:bg-red-500/10 rounded-lg px-3 py-2">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Créer</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      {evaluations.length > 0 && (
        <>
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold">Évaluations</h2>
          </div>
          <Card className="p-0 overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
                <tr>
                  <th className="px-4 py-3 font-medium">Évaluation</th>
                  <th className="px-4 py-3 font-medium">Type</th>
                  <th className="px-4 py-3 font-medium">Statut</th>
                  <th className="px-4 py-3 font-medium">Actions</th>
                </tr>
              </thead>
            <tbody className="divide-y divide-ink-100 dark:divide-white/10">
              {evaluations.map((ev) => (
                <tr key={ev.id}>
                  <td className="px-4 py-3 font-medium">{ev.titre}</td>
                  <td className="px-4 py-3 text-ink-400 capitalize">{ev.typeEvaluationCode}</td>
                  <td className="px-4 py-3"><span className="capitalize text-xs font-medium px-2 py-0.5 rounded-full bg-brand-50 dark:bg-brand-500/15 text-brand-700">{ev.statut}</span></td>
                  <td className="px-4 py-3 flex gap-2">
                    {ev.statut !== 'validee' && ev.statut !== 'publiee' && (
                      <>
                        <Bouton variante="discret" onClick={() => ouvrirSaisie(ev.id)}>Saisir</Bouton>
                        <Bouton variante="discret" onClick={() => validerEvaluation(ev.id)} disabled={enCours}>
                          <CheckCircle size={14} /> Valider
                        </Bouton>
                      </>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
        </>
      )}

      {evaluationActive && notes.length > 0 && (
        <Card>
          <div className="flex items-center justify-between mb-4">
            <h2 className="font-semibold">Saisie des notes</h2>
            {evaluationActiveObjet && (
              <span className="text-sm text-ink-400">
                {evaluationActiveObjet.titre} 
                (barème: {evaluationActiveObjet.bareme})
              </span>
            )}
          </div>
          <div className="space-y-2">
            {notes.map((n) => (
              <div key={n.eleveId} className="flex items-center gap-3">
                <span className="flex-1 text-sm font-medium">{n.eleveNom}</span>
                <label className="flex items-center gap-1 text-xs">
                  <input type="checkbox" checked={saisieNotes[n.eleveId]?.absent ?? false}
                    onChange={(e) => setSaisieNotes({ ...saisieNotes, [n.eleveId]: { ...saisieNotes[n.eleveId], absent: e.target.checked, valeur: e.target.checked ? '' : saisieNotes[n.eleveId]?.valeur || '' } })} />
                  Absent
                </label>
                <input 
                  type="number" 
                  min={0} 
                  max={evaluationActiveObjet?.bareme || 20} 
                  step={0.25} 
                  disabled={saisieNotes[n.eleveId]?.absent}
                  value={saisieNotes[n.eleveId]?.valeur ?? ''}
                  onChange={(e) => setSaisieNotes({ ...saisieNotes, [n.eleveId]: { ...saisieNotes[n.eleveId], valeur: e.target.value } })}
                  className="w-20 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-2 py-1 text-sm text-center"
                  placeholder="0"
                />
              </div>
            ))}
          </div>
          {erreur && <p className="mt-3 text-sm text-red-600">{erreur}</p>}
          <div className="mt-4 flex gap-2">
            <Bouton onClick={enregistrerNotes} disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Enregistrer</Bouton>
            <Bouton variante="discret" onClick={() => setEvaluationActive(null)}>Fermer</Bouton>
          </div>
        </Card>
      )}
    </div>
  );
}
