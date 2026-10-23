import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { UtilisateurProfil } from '@/types/auth';

interface AuthState {
  utilisateur: UtilisateurProfil | null;
  accessToken: string | null;
  refreshToken: string | null;
  hydrate: boolean;
  connecter: (utilisateur: UtilisateurProfil, accessToken: string, refreshToken: string) => void;
  deconnecter: () => void;
  terminerHydratation: () => void;
}

export const useAuth = create<AuthState>()(
  persist<AuthState, [], [], Pick<AuthState, 'refreshToken'>>(
    (set) => ({
      utilisateur: null,
      accessToken: null,
      refreshToken: null,
      hydrate: false,
      connecter: (utilisateur, accessToken, refreshToken) =>
        set({ utilisateur, accessToken, refreshToken }),
      deconnecter: () => set({ utilisateur: null, accessToken: null, refreshToken: null }),
      terminerHydratation: () => set({ hydrate: true }),
    }),
    {
      name: 'educore-auth',
      partialize: ({ refreshToken }) => ({ refreshToken }),
      merge: (persistedState, currentState) => ({
        ...currentState,
        ...(persistedState ?? {}),
      }),
    },
  ),
);
