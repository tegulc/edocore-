import { useEffect, useState, type FormEvent } from 'react';
import { CalendarClock, Loader2, Plus } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { classeApi, enseignantApi, structureApi } from '@/lib/classeApi';
import { emploiDuTempsApi } from '@/lib/emploiDuTempsApi';
import { extraireErreurApi } from '@/lib/authApi';
import { JOURS_SEMAINE, type CreneauHoraireProfil, type SeanceProfil } from '@/types/emploiDuTemps';
import type { ClasseMatiereProfil, ClasseProfil, EnseignantProfil, MatiereProfil } from '@/types/classe';

export default function PageEmploiDuTemps() {
  const [classes, setClasses] = useState<ClasseProfil[]>([]);
  const [classeId, setClasseId] = useState('');
  const [seances, setSeances] = useState<SeanceProfil[]>([]);
  const [creneaux, setCreneaux] = useState<CreneauHoraireProfil[]>([]);
  const [matieres, setMatieres] = useState<ClasseMatiereProfil[]>([]);
  const [matieresGenerales, setMatieresGenerales] = useState<MatiereProfil[]>([]);
  const [matiereId, setMatiereId] = useState('');
  const [enseignants, setEnseignants] = useState<EnseignantProfil[]>([]);
  const [chargement, setChargement] = useState(true);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [matiereFormOuvert, setMatiereFormOuvert] = useState(false);
  const [nouvelleMatiere, setNouvelleMatiere] = useState({ code: '', libelle: '', couleur: '#3498db' });

  useEffect(() => {
    Promise.all([
      classeApi.lister(),
      emploiDuTempsApi.creneaux(),
      enseignantApi.lister(),
      structureApi.matieres(),
    ]).then(([cls, cr, ens, mat]) => {
      setClasses(cls.data);
      setCreneaux(cr.data);
      setEnseignants(ens.data);
      setMatieresGenerales(mat.data);
      setChargement(false);
    });
  }, []);

  useEffect(() => {
    if (classeId) {
      emploiDuTempsApi.parClasse(Number(classeId)).then(({ data }) => setSeances(data));
      classeApi.matieres(Number(classeId)).then(({ data }) => setMatieres(data));
    }
  }, [classeId]);

  useEffect(() => {
    if (matiereId && classeId) {
      // Filtrer les matières de classe qui correspondent à la matière sélectionnée
      const matieresFiltrees = matieres.filter(m => m.matiereId === Number(matiereId));
      if (matieresFiltrees.length > 0) {
        // Laisser le formulaire utiliser la première matière filtrée
      }
    }
  }, [matiereId, classeId, matieres]);

  async function gererCreation(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    if (!classeId) return;
    const fd = new FormData(e.currentTarget);
    const matiereClasse = matieres.find((m) => m.id === Number(fd.get('classeMatiereId')));
    setEnCours(true);
    setErreur(null);
    try {
      await emploiDuTempsApi.creerSeance({
        classeId: Number(classeId),
        matiereId: matiereClasse!.matiereId,
        enseignantId: Number(fd.get('enseignantId')),
        creneauId: Number(fd.get('creneauId')),
        jourSemaine: Number(fd.get('jourSemaine')),
      });
      setFormulaireOuvert(false);
      const { data } = await emploiDuTempsApi.parClasse(Number(classeId));
      setSeances(data);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function creerMatiere(e: FormEvent) {
    e.preventDefault();
    if (!nouvelleMatiere.code || !nouvelleMatiere.libelle) return;
    setEnCours(true);
    setErreur(null);
    try {
      await structureApi.creerMatiere(nouvelleMatiere);
      setMatiereFormOuvert(false);
      setNouvelleMatiere({ code: '', libelle: '', couleur: '#3498db' });
      const { data } = await structureApi.matieres();
      setMatieresGenerales(data);
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
          <h1 className="font-display text-2xl font-bold">Emploi du temps</h1>
          <p className="text-sm text-ink-400 mt-1">Grille hebdomadaire par classe, année scolaire active.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)} disabled={!classeId}>
          <Plus size={16} /> Ajouter une séance
        </Bouton>
      </div>

      <Card>
        <label className="block max-w-xs">
          <span className="text-sm font-medium">Classe</span>
          <select value={classeId} onChange={(e) => setClasseId(e.target.value)}
            className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
            <option value="">{chargement ? 'Chargement…' : 'Choisir une classe…'}</option>
            {classes.map((c) => <option key={c.id} value={c.id}>{c.libelle}</option>)}
          </select>
        </label>
      </Card>

      {formulaireOuvert && classeId && (
        <Card>
          <form onSubmit={gererCreation} className="grid sm:grid-cols-2 gap-4">
            <label className="block">
              <span className="text-sm font-medium">Matière</span>
              <select required name="classeMatiereId" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                {matieres.map((m) => <option key={m.id} value={m.id}>{m.matiereLibelle}</option>)}
              </select>
            </label>
            <label className="block">
              <span className="text-sm font-medium">Matière générale</span>
              <div className="flex gap-2">
                <select value={matiereId} onChange={(e) => setMatiereId(e.target.value)} className="mt-1.5 flex-1 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                  <option value="">Sélectionner…</option>
                  {matieresGenerales.map((m) => <option key={m.id} value={m.id}>{m.libelle}</option>)}
                </select>
                <Bouton type="button" variante="discret" onClick={() => setMatiereFormOuvert(true)}><Plus size={16} /></Bouton>
              </div>
            </label>
            <label className="block">
              <span className="text-sm font-medium">Enseignant</span>
              <select required name="enseignantId" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                {enseignants.map((e) => <option key={e.id} value={e.id}>{e.utilisateur.prenom} {e.utilisateur.nom}</option>)}
              </select>
            </label>
            <label className="block">
              <span className="text-sm font-medium">Créneau</span>
              <select required name="creneauId" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                {creneaux.map((c) => <option key={c.id} value={c.id}>{c.libelle} ({c.heureDebut}-{c.heureFin})</option>)}
              </select>
            </label>
            <label className="block">
              <span className="text-sm font-medium">Jour</span>
              <select required name="jourSemaine" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                {[1, 2, 3, 4, 5, 6].map((j) => <option key={j} value={j}>{JOURS_SEMAINE[j]}</option>)}
              </select>
            </label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Planifier</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      {matiereFormOuvert && (
        <Card>
          <form onSubmit={creerMatiere} className="grid sm:grid-cols-3 gap-4">
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
            <label className="block">
              <span className="text-sm font-medium">Couleur</span>
              <input type="color" value={nouvelleMatiere.couleur} onChange={(e) => setNouvelleMatiere({ ...nouvelleMatiere, couleur: e.target.value })}
                className="mt-1.5 w-full h-10 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            {erreur && <p className="sm:col-span-3 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-3 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Créer matière</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setMatiereFormOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      {classeId && (
        <Card className="p-0 overflow-hidden">
          {seances.length === 0 ? (
            <p className="p-6 text-sm text-ink-400 text-center">Aucune séance planifiée pour cette classe.</p>
          ) : (
            <table className="w-full text-sm">
              <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
                <tr>
                  <th className="px-4 py-3 font-medium">Jour</th>
                  <th className="px-4 py-3 font-medium">Créneau</th>
                  <th className="px-4 py-3 font-medium">Matière</th>
                  <th className="px-4 py-3 font-medium hidden sm:table-cell">Enseignant</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100 dark:divide-white/10">
                {[...seances].sort((a, b) => a.jourSemaine - b.jourSemaine).map((s) => (
                  <tr key={s.id}>
                    <td className="px-4 py-3 font-medium">{JOURS_SEMAINE[s.jourSemaine]}</td>
                    <td className="px-4 py-3 text-ink-400 flex items-center gap-1.5">
                      <CalendarClock size={14} /> {s.creneauLibelle ?? `#${s.creneauId}`}
                    </td>
                    <td className="px-4 py-3">{s.matiereLibelle}</td>
                    <td className="px-4 py-3 text-ink-400 hidden sm:table-cell">{s.enseignantNom}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </Card>
      )}
    </div>
  );
}
