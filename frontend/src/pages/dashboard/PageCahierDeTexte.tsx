import { useEffect, useState, type FormEvent } from 'react';
import { BookMarked, Loader2, Plus } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { cahierApi } from '@/lib/modulesApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import { structureApi, classeApi } from '@/lib/classeApi';
import type { CahierDeTexteProfil } from '@/types/pedagogie';

export default function PageCahierDeTexte() {
  const { classes, matieresClasse, matieres, chargement, chargerMatieres } = useSelecteursPedagogiques();
  const [classeId, setClasseId] = useState('');
  const [classeMatiereId, setClasseMatiereId] = useState('');
  const [entrees, setEntrees] = useState<CahierDeTexteProfil[]>([]);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [matiereFormOuvert, setMatiereFormOuvert] = useState(false);
  const [nouvelleMatiere, setNouvelleMatiere] = useState({ code: '', libelle: '', couleur: '#3498db', coefficient: 1, volumeHoraireHebdo: 2 });
  const [modeMatiere, setModeMatiere] = useState<'selection' | 'creation'>('selection');
  const [matiereExistanteId, setMatiereExistanteId] = useState('');

  useEffect(() => { if (classeId) chargerMatieres(Number(classeId)); }, [classeId, chargerMatieres]);

  useEffect(() => {
    if (classeMatiereId) {
      cahierApi.lister(Number(classeMatiereId)).then(({ data }) => setEntrees(data)).catch(() => setEntrees([]));
    }
  }, [classeMatiereId]);

  async function gererCreation(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!classeMatiereId) return;
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      await cahierApi.creer({
        classeMatiereId: Number(classeMatiereId),
        dateCours: fd.get('dateCours') as string,
        chapitre: (fd.get('chapitre') as string) || undefined,
        lecon: (fd.get('lecon') as string) || undefined,
        contenu: (fd.get('contenu') as string) || undefined,
        exercices: (fd.get('exercices') as string) || undefined,
      });
      setFormulaireOuvert(false);
      const { data } = await cahierApi.lister(Number(classeMatiereId));
      setEntrees(data);
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
          <h1 className="font-display text-2xl font-bold">Cahier de texte</h1>
          <p className="text-sm text-ink-400 mt-1">Contenu pédagogique dispensé en classe.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)} disabled={!classeMatiereId}>
          <Plus size={16} /> Nouvelle entrée
        </Bouton>
      </div>

      <Card>
        <div className="grid sm:grid-cols-2 gap-4">
          <label className="block">
            <span className="text-sm font-medium">Classe</span>
            <select value={classeId} onChange={(e) => { setClasseId(e.target.value); setClasseMatiereId(''); }}
              className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
              <option value="">Sélectionner…</option>
              {classes.map((c) => <option key={c.id} value={c.id}>{c.libelle}</option>)}
            </select>
          </label>
          <label className="block">
            <span className="text-sm font-medium">Matière</span>
            <div className="flex gap-2">
              <select value={classeMatiereId} onChange={(e) => setClasseMatiereId(e.target.value)}
                className="mt-1.5 flex-1 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="">Sélectionner…</option>
                {matieresClasse.map((m) => <option key={m.id} value={m.id}>{m.matiereLibelle}</option>)}
              </select>
              <Bouton type="button" variante="discret" onClick={() => setMatiereFormOuvert(true)} disabled={!classeId}><Plus size={16} /></Bouton>
            </div>
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
            <label className="block"><span className="text-sm font-medium">Date du cours</span>
              <input required type="date" name="dateCours" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Chapitre</span>
              <input name="chapitre" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Leçon</span>
              <input name="lecon" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Contenu</span>
              <textarea name="contenu" rows={3} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Exercices</span>
              <textarea name="exercices" rows={2} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Enregistrer</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      <div className="space-y-3">
        {entrees.length === 0 && <Card><p className="text-sm text-ink-400">Aucune entrée pour cette matière.</p></Card>}
        {entrees.map((e) => (
          <Card key={e.id}>
            <div className="flex items-start gap-3">
              <BookMarked size={18} className="text-brand-600 mt-0.5 shrink-0" />
              <div>
                <p className="font-semibold">{e.chapitre ?? 'Sans chapitre'} — {e.lecon ?? 'Sans leçon'}</p>
                <p className="text-xs text-ink-400">{new Date(e.dateCours).toLocaleDateString('fr-FR')}</p>
                {e.contenu && <p className="text-sm mt-2">{e.contenu}</p>}
                {e.exercices && <p className="text-sm mt-1 text-ink-400">Exercices : {e.exercices}</p>}
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}
