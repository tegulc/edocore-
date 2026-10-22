import { useEffect, useState } from 'react';
import { BookOpen, Loader2 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { evaluationApi } from '@/lib/noteApi';
import { extraireErreurApi } from '@/lib/authApi';
import type { EvaluationProfil, NoteProfil } from '@/types/note';

interface CarnetNotesProps {
  classeMatiereId: number;
  periodeId: number;
  eleveId: number;
  eleveNom: string;
}

export default function CarnetNotes({ classeMatiereId, periodeId, eleveId, eleveNom }: CarnetNotesProps) {
  const [evaluations, setEvaluations] = useState<EvaluationProfil[]>([]);
  const [notesParEvaluation, setNotesParEvaluation] = useState<Record<number, NoteProfil>>({});
  const [chargement, setChargement] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  useEffect(() => {
    if (classeMatiereId && periodeId) {
      chargerDonnees();
    }
  }, [classeMatiereId, periodeId]);

  async function chargerDonnees() {
    setChargement(true);
    setErreur(null);
    try {
      const { data: evals } = await evaluationApi.lister(classeMatiereId, periodeId);
      setEvaluations(evals);

      // Charger les notes pour chaque évaluation
      const notesMap: Record<number, NoteProfil> = {};
      for (const evaluation of evals) {
        try {
          const { data: notes } = await evaluationApi.notes(evaluation.id);
          const noteEleve = notes.find(n => n.eleveId === eleveId);
          if (noteEleve) {
            notesMap[evaluation.id] = noteEleve;
          }
        } catch (err) {
          console.error(`Erreur lors du chargement des notes pour l'évaluation ${evaluation.id}:`, err);
        }
      }
      setNotesParEvaluation(notesMap);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setChargement(false);
    }
  }

  function calculerMoyenne() {
    if (evaluations.length === 0) return null;
    
    let sommePonderee = 0;
    let sommeCoefficients = 0;

    const ponderations: Record<string, number> = {
      'DEVOIR': 1,
      'SEQUENCE': 1,
      'COMPOSITION': 2,
      'EXAMEN': 2,
      'PROJET': 1.5,
      'TP': 1
    };

    for (const evaluation of evaluations) {
      const note = notesParEvaluation[evaluation.id];
      if (note && note.valeur !== null && !note.absent) {
        const coefficient = ponderations[evaluation.typeEvaluationCode] || 1;
        // Normaliser sur 20
        const noteSur20 = (note.valeur / evaluation.bareme) * 20;
        sommePonderee += noteSur20 * coefficient;
        sommeCoefficients += coefficient;
      }
    }

    return sommeCoefficients > 0 ? sommePonderee / sommeCoefficients : null;
  }

  const moyenne = calculerMoyenne();

  if (chargement) {
    return (
      <Card className="p-6">
        <div className="flex items-center justify-center gap-2">
          <Loader2 size={20} className="animate-spin" />
          <span>Chargement du carnet de notes...</span>
        </div>
      </Card>
    );
  }

  if (erreur) {
    return (
      <Card className="p-6">
        <p className="text-red-600">{erreur}</p>
      </Card>
    );
  }

  return (
    <Card className="p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-bold flex items-center gap-2">
          <BookOpen size={20} />
          Carnet de notes - {eleveNom}
        </h3>
        <Bouton onClick={chargerDonnees} variante="discret">
          Actualiser
        </Bouton>
      </div>

      {evaluations.length === 0 ? (
        <p className="text-sm text-ink-400">Aucune évaluation pour cette période.</p>
      ) : (
        <>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
                <tr>
                  <th className="px-4 py-3 font-medium">Type</th>
                  <th className="px-4 py-3 font-medium">Titre</th>
                  <th className="px-4 py-3 font-medium">Date</th>
                  <th className="px-4 py-3 font-medium">Barème</th>
                  <th className="px-4 py-3 font-medium">Note</th>
                  <th className="px-4 py-3 font-medium">Note/20</th>
                  <th className="px-4 py-3 font-medium">Observation</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-ink-100 dark:divide-white/10">
                {evaluations.map((evaluation) => {
                  const note = notesParEvaluation[evaluation.id];
                  const noteSur20 = note && note.valeur !== null && !note.absent 
                    ? ((note.valeur / evaluation.bareme) * 20).toFixed(2) 
                    : '—';
                  
                  return (
                    <tr key={evaluation.id}>
                      <td className="px-4 py-3">{getTypeLabel(evaluation.typeEvaluationCode)}</td>
                      <td className="px-4 py-3 font-medium">{evaluation.titre}</td>
                      <td className="px-4 py-3">{evaluation.dateEvaluation}</td>
                      <td className="px-4 py-3">{evaluation.bareme}</td>
                      <td className="px-4 py-3">
                        {note?.absent ? (
                          <span className="text-orange-600">ABS</span>
                        ) : note?.valeur !== null ? (
                          `${note.valeur}/${evaluation.bareme}`
                        ) : (
                          '—'
                        )}
                      </td>
                      <td className="px-4 py-3 font-semibold">{noteSur20}</td>
                      <td className="px-4 py-3 text-xs">{note?.observation || '—'}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="mt-4 p-4 bg-ink-50 dark:bg-white/5 rounded-lg">
            <div className="flex justify-between items-center">
              <span className="text-sm font-medium">Moyenne de la matière</span>
              <span className="text-2xl font-bold">
                {moyenne !== null ? `${moyenne.toFixed(2)}/20` : '—'}
              </span>
            </div>
          </div>
        </>
      )}
    </Card>
  );
}

function getTypeLabel(code: string): string {
  const labels: Record<string, string> = {
    'DEVOIR': 'Devoir',
    'SEQUENCE': 'Séquence',
    'COMPOSITION': 'Composition',
    'EXAMEN': 'Examen',
    'PROJET': 'Projet',
    'TP': 'TP'
  };
  return labels[code] || code;
}
