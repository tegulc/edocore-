import { useEffect, useState } from 'react';
import { classeApi, structureApi } from '@/lib/classeApi';
import { anneeScolaireApi } from '@/lib/etablissementApi';
import { eleveApi } from '@/lib/eleveApi';
import type { ClasseProfil, ClasseMatiereProfil, MatiereProfil } from '@/types/classe';
import type { PeriodeProfil } from '@/types/etablissement';
import type { EleveProfil } from '@/types/eleve';

/** Charge les selecteurs communs (classes, periodes, eleves, matieres) utilises par plusieurs modules. */
export function useSelecteursPedagogiques(chargerEleves = false) {
  const [classes, setClasses] = useState<ClasseProfil[]>([]);
  const [periodes, setPeriodes] = useState<PeriodeProfil[]>([]);
  const [eleves, setEleves] = useState<EleveProfil[]>([]);
  const [matieresClasse, setMatieresClasse] = useState<ClasseMatiereProfil[]>([]);
  const [matieres, setMatieres] = useState<MatiereProfil[]>([]);
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    async function charger() {
      try {
        const [{ data: cls }, { data: annee }, { data: mat }] = await Promise.all([
          classeApi.lister(),
          anneeScolaireApi.active(),
          structureApi.matieres(),
        ]);
        setClasses(cls);
        setMatieres(mat);
        const { data: per } = await anneeScolaireApi.periodes(annee.id);
        setPeriodes(per);
        if (chargerEleves) {
          const { data: page } = await eleveApi.lister(1, 500);
          setEleves(page.donnees);
        }
      } finally {
        setChargement(false);
      }
    }
    charger();
  }, [chargerEleves]);

  async function chargerMatieres(classeId: number) {
    const { data } = await classeApi.matieres(classeId);
    setMatieresClasse(data);
    return data;
  }

  return { classes, periodes, eleves, matieresClasse, matieres, chargement, chargerMatieres };
}
