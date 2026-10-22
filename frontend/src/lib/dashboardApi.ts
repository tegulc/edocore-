import { api } from './api';

export interface IndicateursDashboard {
  elevesInscrits: number;
  tauxPresenceDuJour: number | null;
  paiementsDuMois: number;
  personnelActif: number;
}

export const dashboardApi = {
  indicateurs: () => api.get<IndicateursDashboard>('/dashboard/indicateurs'),
};
