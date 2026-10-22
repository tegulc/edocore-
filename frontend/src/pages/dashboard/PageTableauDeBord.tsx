import { useEffect, useState } from 'react';
import { Users, Wallet, ClipboardCheck, GraduationCap } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { dashboardApi, type IndicateursDashboard } from '@/lib/dashboardApi';

/**
 * Tableau de bord (Module 26, Phase 8) : indicateurs reels, agreges depuis
 * les modules Eleves/Presence/Paiements/Enseignants. Les cartes etaient des
 * "—" en Phase 0 en attendant que ces modules existent — boucle bouclee.
 */
export default function PageTableauDeBord() {
  const [donnees, setDonnees] = useState<IndicateursDashboard | null>(null);

  useEffect(() => {
    dashboardApi.indicateurs().then(({ data }) => setDonnees(data)).catch(() => {});
  }, []);

  const indicateurs = [
    { libelle: 'Élèves inscrits', valeur: donnees ? donnees.elevesInscrits.toLocaleString('fr-FR') : '—', icone: GraduationCap, accent: 'brand' as const },
    { libelle: 'Taux de présence', valeur: donnees?.tauxPresenceDuJour != null ? `${donnees.tauxPresenceDuJour}%` : '—', icone: ClipboardCheck, accent: 'accent' as const },
    { libelle: 'Paiements du mois', valeur: donnees ? `${Math.round(donnees.paiementsDuMois).toLocaleString('fr-FR')} XAF` : '—', icone: Wallet, accent: 'brand' as const },
    { libelle: 'Personnel actif', valeur: donnees ? donnees.personnelActif.toLocaleString('fr-FR') : '—', icone: Users, accent: 'accent' as const },
  ];

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Tableau de bord</h1>
        <p className="text-sm text-ink-400 mt-1">Vue d'ensemble de votre établissement.</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        {indicateurs.map(({ libelle, valeur, icone: Icone, accent }) => (
          <Card key={libelle}>
            <div className="flex items-center justify-between">
              <span className="text-sm text-ink-400">{libelle}</span>
              <div
                className={
                  accent === 'brand'
                    ? 'h-8 w-8 rounded-lg bg-brand-50 dark:bg-brand-500/15 flex items-center justify-center text-brand-600 dark:text-brand-300'
                    : 'h-8 w-8 rounded-lg bg-accent-500/10 flex items-center justify-center text-accent-600 dark:text-accent-400'
                }
              >
                <Icone size={16} />
              </div>
            </div>
            <p className="font-display text-2xl font-bold mt-3">{valeur}</p>
          </Card>
        ))}
      </div>
    </div>
  );
}
