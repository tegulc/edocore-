import { useEffect, useState, type FormEvent } from 'react';
import { UserPlus, Loader2, GraduationCap } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { eleveApi } from '@/lib/eleveApi';
import { classeApi } from '@/lib/classeApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { EleveProfil } from '@/types/eleve';
import type { ClasseProfil } from '@/types/classe';

const VIDE = { nom: '', prenom: '', matricule: '', sexe: 'M' as 'M' | 'F', dateNaissance: '', lieuNaissance: '', classeId: 0 };

export default function PageEleves() {
  const [eleves, setEleves] = useState<EleveProfil[]>([]);
  const [classes, setClasses] = useState<ClasseProfil[]>([]);
  const [chargement, setChargement] = useState(true);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [formulaire, setFormulaire] = useState(VIDE);
  const [erreur, setErreur] = useState<string | null>(null);
  const [enCours, setEnCours] = useState(false);
  const [inscriptionEnCours, setInscriptionEnCours] = useState<number | null>(null);
  const [classeChoisie, setClasseChoisie] = useState<Record<number, string>>({});

  async function toutCharger() {
    setChargement(true);
    try {
      const [e, c] = await Promise.all([eleveApi.lister(), classeApi.lister()]);
      setEleves(e.data.donnees);
      setClasses(c.data);
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => { toutCharger(); }, []);

  async function gererCreation(e: FormEvent) {
    e.preventDefault();
    setEnCours(true);
    setErreur(null);
    try {
      await eleveApi.creer(formulaire);
      setFormulaire(VIDE);
      setFormulaireOuvert(false);
      await toutCharger();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function inscrireDansClasse(eleveId: number) {
    const classeId = classeChoisie[eleveId];
    if (!classeId) return;
    setInscriptionEnCours(eleveId);
    try {
      await eleveApi.inscrire(eleveId, Number(classeId));
      await toutCharger();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setInscriptionEnCours(null);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Élèves</h1>
          <p className="text-sm text-ink-400 mt-1">Dossiers élèves de l'établissement.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)}><UserPlus size={16} /> Nouvel élève</Bouton>
      </div>

      {formulaireOuvert && (
        <Card>
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
              <span className="text-sm font-medium">Sexe</span>
              <select value={formulaire.sexe} onChange={(e) => setFormulaire({ ...formulaire, sexe: e.target.value as 'M' | 'F' })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="M">Masculin</option>
                <option value="F">Féminin</option>
              </select>
            </label>
            <label className="block">
              <span className="text-sm font-medium">Matricule</span>
              <input required value={formulaire.matricule} onChange={(e) => setFormulaire({ ...formulaire, matricule: e.target.value })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm"
                placeholder="Insérer le matricule" />
            </label>
            <label className="block">
              <span className="text-sm font-medium">Date de naissance</span>
              <input required type="date" value={formulaire.dateNaissance} onChange={(e) => setFormulaire({ ...formulaire, dateNaissance: e.target.value })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            <label className="block">
              <span className="text-sm font-medium">Lieu de naissance</span>
              <input required value={formulaire.lieuNaissance} onChange={(e) => setFormulaire({ ...formulaire, lieuNaissance: e.target.value })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" />
            </label>
            <label className="block sm:col-span-2">
              <span className="text-sm font-medium">Classe (année scolaire courante)</span>
              <select required value={formulaire.classeId || ''} onChange={(e) => setFormulaire({ ...formulaire, classeId: Number(e.target.value) })}
                className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="">Choisir une classe…</option>
                {classes.map((c) => <option key={c.id} value={c.id}>{c.libelle}</option>)}
              </select>
            </label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600 bg-red-50 dark:bg-red-500/10 rounded-lg px-3 py-2">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Créer le dossier</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr>
              <th className="px-4 py-3 font-medium">Élève</th>
              <th className="px-4 py-3 font-medium hidden sm:table-cell">Matricule</th>
              <th className="px-4 py-3 font-medium">Statut</th>
              <th className="px-4 py-3 font-medium">Classe</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {chargement && <tr><td colSpan={4} className="px-4 py-6 text-center text-ink-400">Chargement...</td></tr>}
            {!chargement && eleves.length === 0 && <tr><td colSpan={4} className="px-4 py-6 text-center text-ink-400">Aucun élève pour le moment.</td></tr>}
            {eleves.map((el) => (
              <tr key={el.id}>
                <td className="px-4 py-3 font-medium flex items-center gap-2">
                  <GraduationCap size={14} className="text-ink-400" /> {el.prenom} {el.nom}
                </td>
                <td className="px-4 py-3 text-ink-400 font-mono text-xs hidden sm:table-cell">{el.matricule}</td>
                <td className="px-4 py-3">
                  <span className="inline-flex items-center rounded-full bg-emerald-50 dark:bg-emerald-500/15 text-emerald-700 dark:text-emerald-400 px-2.5 py-0.5 text-xs font-medium capitalize">
                    {el.statut}
                  </span>
                </td>
                <td className="px-4 py-3">
                  <div className="flex items-center gap-2">
                    <select
                      value={classeChoisie[el.id] ?? ''}
                      onChange={(e) => setClasseChoisie({ ...classeChoisie, [el.id]: e.target.value })}
                      className="rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-2 py-1 text-xs"
                    >
                      <option value="">Inscrire…</option>
                      {classes.map((c) => <option key={c.id} value={c.id}>{c.libelle}</option>)}
                    </select>
                    <Bouton
                      variante="discret"
                      disabled={inscriptionEnCours === el.id || !classeChoisie[el.id]}
                      onClick={() => inscrireDansClasse(el.id)}
                    >
                      {inscriptionEnCours === el.id ? <Loader2 size={14} className="animate-spin" /> : 'OK'}
                    </Bouton>
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
