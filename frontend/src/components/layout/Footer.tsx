export function Footer() {
  const annee = new Date().getFullYear();

  return (
    <footer className="border-t border-ink-100 dark:border-white/10 px-4 sm:px-6 py-4">
      <div className="flex flex-col sm:flex-row items-center justify-between gap-2 text-xs text-ink-400">
        <p>© {annee} EDUCORE — Une seule plateforme pour gérer toute la vie d'un établissement.</p>
        <div className="flex items-center gap-4">
          <a href="#" className="hover:text-brand-500 transition-theme">Aide</a>
          <a href="#" className="hover:text-brand-500 transition-theme">Confidentialité</a>
          <a href="#" className="hover:text-brand-500 transition-theme">v0.1.0 — Phase 0</a>
        </div>
      </div>
    </footer>
  );
}
