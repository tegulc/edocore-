import type { ReactNode } from 'react';
import clsx from 'clsx';

interface CardProps {
  children: ReactNode;
  className?: string;
}

/** Unite visuelle de base : reutilisee par tous les modules (dashboard, listes, formulaires). */
export function Card({ children, className }: CardProps) {
  return (
    <div
      className={clsx(
        'rounded-xl2 border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt shadow-card dark:shadow-card-dark p-5',
        className
      )}
    >
      {children}
    </div>
  );
}
