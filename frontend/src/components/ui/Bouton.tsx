import type { ButtonHTMLAttributes } from 'react';
import clsx from 'clsx';

interface BoutonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variante?: 'primaire' | 'discret' | 'danger';
}

export function Bouton({ variante = 'primaire', className, ...props }: BoutonProps) {
  return (
    <button
      className={clsx(
        'inline-flex items-center justify-center gap-2 rounded-lg px-4 py-2.5 text-sm font-semibold transition-theme disabled:opacity-50 disabled:cursor-not-allowed',
        variante === 'primaire' && 'bg-brand-500 text-white hover:bg-brand-600 shadow-sm',
        variante === 'discret' && 'bg-ink-50 dark:bg-white/5 text-ink-700 dark:text-ink-100 hover:bg-ink-100 dark:hover:bg-white/10',
        variante === 'danger' && 'bg-red-600 text-white hover:bg-red-700',
        className
      )}
      {...props}
    />
  );
}
