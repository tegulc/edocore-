import { useState } from 'react';
import { Printer, Download, Mail, Archive, Award, User, GraduationCap } from 'lucide-react';
import { Bouton } from '@/components/ui/Bouton';
import type { BulletinProfil } from '@/types/note';

interface BulletinDetailleProps {
  bulletin: BulletinProfil;
  classeNom: string;
  periodeNom: string;
  onTelecharger?: (bulletin: BulletinProfil) => void;
  onEnvoyer?: () => void;
  onArchiver?: () => void;
}

export default function BulletinDetaille({ 
  bulletin, 
  classeNom, 
  periodeNom,
  onTelecharger,
  onEnvoyer,
  onArchiver
}: BulletinDetailleProps) {
  const [affichageMode, setAffichageMode] = useState<'normal' | 'impression'>('normal');

  const mention = obtenirMention(bulletin.moyenneGenerale);
  const decision = obtenirDecision(bulletin.moyenneGenerale);

  return (
    <div className={`max-w-4xl mx-auto bg-white dark:bg-surface-dark p-8 ${affichageMode === 'impression' ? 'impression-mode' : ''}`}>
      {/* En-tête institutionnel */}
      <div className="text-center border-b-2 border-black dark:border-white pb-4 mb-6">
        <h1 className="text-xl font-bold uppercase mb-1">République du Cameroun</h1>
        <h2 className="text-lg font-semibold mb-1">Ministère de l'Enseignement Secondaire</h2>
        <h3 className="text-xl font-bold mb-2">Lycée Classique de Dschang</h3>
        <p className="text-sm text-ink-600 dark:text-ink-400">Année scolaire 2026-2027</p>
      </div>

      {/* Informations élève */}
      <div className="grid grid-cols-2 gap-4 mb-6 p-4 bg-ink-50 dark:bg-white/5 rounded-lg">
        <div>
          <p className="text-sm text-ink-600 dark:text-ink-400">Nom & Prénom</p>
          <p className="font-semibold">{bulletin.eleveNom}</p>
        </div>
        <div>
          <p className="text-sm text-ink-600 dark:text-ink-400">Classe</p>
          <p className="font-semibold">{classeNom}</p>
        </div>
        <div>
          <p className="text-sm text-ink-600 dark:text-ink-400">Effectif</p>
          <p className="font-semibold">{bulletin.effectif}</p>
        </div>
        <div>
          <p className="text-sm text-ink-600 dark:text-ink-400">Période</p>
          <p className="font-semibold">{periodeNom}</p>
        </div>
      </div>

      {/* Tableau des matières */}
      <div className="mb-6">
        <h3 className="text-lg font-bold mb-4 flex items-center gap-2">
          <GraduationCap size={20} />
          Résultats académiques
        </h3>
        <div className="overflow-x-auto">
          <table className="w-full text-sm border-collapse">
            <thead>
              <tr className="bg-ink-100 dark:bg-white/10">
                <th className="border border-black dark:border-white px-2 py-2 text-left font-semibold">Matière</th>
                <th className="border border-black dark:border-white px-2 py-2 text-center font-semibold">Enseignant</th>
                <th className="border border-black dark:border-white px-2 py-2 text-center font-semibold">Note</th>
                <th className="border border-black dark:border-white px-2 py-2 text-center font-semibold">Coef</th>
                <th className="border border-black dark:border-white px-2 py-2 text-center font-semibold">Total</th>
                <th className="border border-black dark:border-white px-2 py-2 text-center font-semibold">Rang</th>
                <th className="border border-black dark:border-white px-2 py-2 text-center font-semibold">Moyenne</th>
                <th className="border border-black dark:border-white px-2 py-2 text-center font-semibold">Max-Min</th>
                <th className="border border-black dark:border-white px-2 py-2 text-left font-semibold">Appréciation</th>
              </tr>
            </thead>
            <tbody>
              {bulletin.moyennesParMatiere.map((matiere, index) => (
                <tr key={index}>
                  <td className="border border-black dark:border-white px-2 py-2 font-semibold">{matiere.matiereLibelle}</td>
                  <td className="border border-black dark:border-white px-2 py-2 text-center text-xs">{matiere.enseignantNom}</td>
                  <td className="border border-black dark:border-white px-2 py-2 text-center">
                    {matiere.moyenne ? matiere.moyenne.toFixed(2) : '—'}
                  </td>
                  <td className="border border-black dark:border-white px-2 py-2 text-center">{matiere.coefficient}</td>
                  <td className="border border-black dark:border-white px-2 py-2 text-center">
                    {matiere.moyenne ? (matiere.moyenne * matiere.coefficient).toFixed(2) : '—'}
                  </td>
                  <td className="border border-black dark:border-white px-2 py-2 text-center">
                    {matiere.rangMatiere ? `${matiere.rangMatiere}e` : '—'}
                  </td>
                  <td className="border border-black dark:border-white px-2 py-2 text-center font-semibold">
                    {matiere.moyenne ? matiere.moyenne.toFixed(2) : '—'}
                  </td>
                  <td className="border border-black dark:border-white px-2 py-2 text-center text-xs">
                    {matiere.maxClasse && matiere.minClasse ? `${matiere.maxClasse.toFixed(2)} - ${matiere.minClasse.toFixed(2)}` : '—'}
                  </td>
                  <td className="border border-black dark:border-white px-2 py-2 text-xs">
                    {matiere.appreciation}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Synthèse */}
      <div className="grid grid-cols-2 gap-4 mb-6 p-4 bg-ink-50 dark:bg-white/5 rounded-lg">
        <div>
          <p className="text-sm text-ink-600 dark:text-ink-400">Total des points</p>
          <p className="text-2xl font-bold">{bulletin.moyenneGenerale ? (bulletin.moyenneGenerale * bulletin.moyennesParMatiere.reduce((sum, m) => sum + m.coefficient, 0)).toFixed(2) : '—'}</p>
        </div>
        <div>
          <p className="text-sm text-ink-600 dark:text-ink-400">Total des coefficients</p>
          <p className="text-2xl font-bold">{bulletin.moyennesParMatiere.reduce((sum, m) => sum + m.coefficient, 0)}</p>
        </div>
        <div>
          <p className="text-sm text-ink-600 dark:text-ink-400">Moyenne générale</p>
          <p className="text-2xl font-bold">{bulletin.moyenneGenerale ? bulletin.moyenneGenerale.toFixed(2) : '—'}/20</p>
        </div>
        <div>
          <p className="text-sm text-ink-600 dark:text-ink-400">Rang</p>
          <p className="text-2xl font-bold">{bulletin.rang ? `${bulletin.rang}e/${bulletin.effectif}` : '—'}</p>
        </div>
      </div>

      {/* Mention et décision */}
      <div className="grid grid-cols-2 gap-4 mb-6">
        <div className="p-4 bg-emerald-50 dark:bg-emerald-500/10 rounded-lg text-center">
          <p className="text-sm text-emerald-600 dark:text-emerald-400 mb-1">Mention</p>
          <p className="text-xl font-bold text-emerald-700 dark:text-emerald-300">{mention}</p>
        </div>
        <div className="p-4 bg-blue-50 dark:bg-blue-500/10 rounded-lg text-center">
          <p className="text-sm text-blue-600 dark:text-blue-400 mb-1">Décision du conseil</p>
          <p className="text-xl font-bold text-blue-700 dark:text-blue-300">{decision}</p>
        </div>
      </div>

      {/* Section discipline */}
      <div className="mb-6 p-4 border border-ink-200 dark:border-white/10 rounded-lg">
        <h3 className="text-lg font-bold mb-3 flex items-center gap-2">
          <User size={20} />
          Discipline
        </h3>
        <div className="grid grid-cols-2 gap-4 text-sm">
          <div>
            <p className="text-ink-600 dark:text-ink-400">Absences justifiées</p>
            <p className="font-semibold">0</p>
          </div>
          <div>
            <p className="text-ink-600 dark:text-ink-400">Absences non justifiées</p>
            <p className="font-semibold">{bulletin.nombreAbsences}</p>
          </div>
          <div>
            <p className="text-ink-600 dark:text-ink-400">Retards</p>
            <p className="font-semibold">0</p>
          </div>
          <div>
            <p className="text-ink-600 dark:text-ink-400">Sanctions</p>
            <p className="font-semibold">{bulletin.blame ? 'Blâme' : 'Aucune'}</p>
          </div>
        </div>
      </div>

      {/* Section conseil de classe */}
      {bulletin.statut === 'valide' && (
        <div className="mb-6 p-4 border border-ink-200 dark:border-white/10 rounded-lg">
          <h3 className="text-lg font-bold mb-3 flex items-center gap-2">
            <Award size={20} />
            Conseil de classe
          </h3>
          <div className="grid sm:grid-cols-2 gap-4 text-sm">
            <div>
              <p className="text-ink-600 dark:text-ink-400">Date du conseil</p>
              <p className="font-semibold">{bulletin.dateConseil || 'Non définie'}</p>
            </div>
            <div>
              <p className="text-ink-600 dark:text-ink-400">Récompenses</p>
              <p className="font-semibold">{bulletin.recompenses || 'Aucune'}</p>
            </div>
            <div>
              <p className="text-ink-600 dark:text-ink-400">Avertissement travail</p>
              <p className="font-semibold">{bulletin.avertissementTravail ? 'Oui' : 'Non'}</p>
            </div>
            <div>
              <p className="text-ink-600 dark:text-ink-400">Avertissement conduite</p>
              <p className="font-semibold">{bulletin.avertissementConduite ? 'Oui' : 'Non'}</p>
            </div>
            <div>
              <p className="text-ink-600 dark:text-ink-400">Observation</p>
              <p className="font-semibold">{bulletin.observationConseil || '—'}</p>
            </div>
            <div>
              <p className="text-ink-600 dark:text-ink-400">Décision finale</p>
              <p className="font-semibold">{bulletin.decisionFinale || 'En attente'}</p>
            </div>
          </div>
        </div>
      )}

      {/* Statistiques de classe */}
      <div className="mb-6 p-4 border border-ink-200 dark:border-white/10 rounded-lg">
        <h3 className="text-lg font-bold mb-3 flex items-center gap-2">
          <Award size={20} />
          Statistiques de classe
        </h3>
        <div className="grid grid-cols-3 gap-4 text-sm">
          <div>
            <p className="text-ink-600 dark:text-ink-400">Moyenne de classe</p>
            <p className="font-semibold">{bulletin.moyenneClasse ? bulletin.moyenneClasse.toFixed(2) : '—'}/20</p>
          </div>
          <div>
            <p className="text-ink-600 dark:text-ink-400">Meilleure moyenne</p>
            <p className="font-semibold">{bulletin.maxClasse ? bulletin.maxClasse.toFixed(2) : '—'}/20</p>
          </div>
          <div>
            <p className="text-ink-600 dark:text-ink-400">Plus faible moyenne</p>
            <p className="font-semibold">{bulletin.minClasse ? bulletin.minClasse.toFixed(2) : '—'}/20</p>
          </div>
        </div>
      </div>

      {/* Actions */}
      {affichageMode === 'normal' && (
        <div className="flex flex-wrap gap-2 justify-center border-t border-ink-200 dark:border-white/10 pt-4">
          <Bouton onClick={() => setAffichageMode('impression')} className="flex items-center gap-2">
            <Printer size={16} /> Prévisualiser impression
          </Bouton>
          <Bouton onClick={() => onTelecharger?.(bulletin)} className="flex items-center gap-2">
            <Download size={16} /> Télécharger PDF
          </Bouton>
          <Bouton onClick={onEnvoyer} className="flex items-center gap-2">
            <Mail size={16} /> Envoyer par email
          </Bouton>
          <Bouton onClick={onArchiver} className="flex items-center gap-2">
            <Archive size={16} /> Archiver
          </Bouton>
        </div>
      )}

      {affichageMode === 'impression' && (
        <div className="fixed top-4 right-4 flex gap-2 no-print">
          <Bouton onClick={() => window.print()} className="flex items-center gap-2">
            <Printer size={16} /> Imprimer
          </Bouton>
          <Bouton onClick={() => setAffichageMode('normal')} className="flex items-center gap-2">
            Fermer
          </Bouton>
        </div>
      )}
    </div>
  );
}

function obtenirMention(moyenne: number | null | undefined): string {
  if (moyenne === null || moyenne === undefined) return '—';
  if (moyenne >= 16) return 'Excellent';
  if (moyenne >= 14) return 'Très bien';
  if (moyenne >= 12) return 'Bien';
  if (moyenne >= 10) return 'Assez bien';
  if (moyenne >= 8) return 'Passable';
  if (moyenne >= 5) return 'Insuffisant';
  return 'Très insuffisant';
}

function obtenirDecision(moyenne: number | null | undefined): string {
  if (moyenne === null || moyenne === undefined) return 'En attente';
  if (moyenne >= 10) return 'Admis';
  if (moyenne >= 8) return 'Admis avec avertissement';
  return 'Redoublement';
}
