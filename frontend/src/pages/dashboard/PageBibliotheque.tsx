import { useEffect, useState, type FormEvent } from 'react';
import { BookOpen, Loader2 } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { bibliothequeApi } from '@/lib/modulesApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useAuth } from '@/hooks/useAuth';
import type { LivreProfil } from '@/types/modules';

export default function PageBibliotheque() {
  const { utilisateur } = useAuth();
  const [livres, setLivres] = useState<LivreProfil[]>([]);
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function charger() {
    const { data } = await bibliothequeApi.livres();
    setLivres(data);
  }

  useEffect(() => { charger(); }, []);

  async function gererCreation(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      await bibliothequeApi.creerLivre({
        titre: fd.get('titre') as string,
        auteur: (fd.get('auteur') as string) || undefined,
        isbn: (fd.get('isbn') as string) || undefined,
        categorie: (fd.get('categorie') as string) || undefined,
        nombreExemplaires: Number(fd.get('nombreExemplaires')),
      });
      setFormulaireOuvert(false);
      await charger();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function emprunter(livreId: number) {
    if (!utilisateur) return;
    setEnCours(true);
    try {
      const retour = new Date();
      retour.setDate(retour.getDate() + 14);
      await bibliothequeApi.emprunter({
        livreId,
        utilisateurId: utilisateur.id,
        dateRetourPrevue: retour.toISOString().slice(0, 10),
      });
      await charger();
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Bibliothèque</h1>
          <p className="text-sm text-ink-400 mt-1">Catalogue et emprunts de livres.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)}><BookOpen size={16} /> Ajouter un livre</Bouton>
      </div>

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererCreation} className="grid sm:grid-cols-2 gap-4">
            <label className="block sm:col-span-2"><span className="text-sm font-medium">Titre</span>
              <input required name="titre" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Auteur</span>
              <input name="auteur" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">ISBN</span>
              <input name="isbn" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Catégorie</span>
              <input name="categorie" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Exemplaires</span>
              <input required type="number" name="nombreExemplaires" defaultValue={1} min={1} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Enregistrer</Bouton>
              <Bouton type="button" variante="discret" onClick={() => setFormulaireOuvert(false)}>Annuler</Bouton>
            </div>
          </form>
        </Card>
      )}

      <Card className="p-0 overflow-hidden">
        <table className="w-full text-sm">
          <thead className="bg-ink-50 dark:bg-white/5 text-left text-ink-400">
            <tr>
              <th className="px-4 py-3 font-medium">Titre</th>
              <th className="px-4 py-3 font-medium hidden sm:table-cell">Auteur</th>
              <th className="px-4 py-3 font-medium">Disponibles</th>
              <th className="px-4 py-3 font-medium">Action</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {livres.length === 0 && <tr><td colSpan={4} className="px-4 py-6 text-center text-ink-400">Catalogue vide.</td></tr>}
            {livres.map((l) => (
              <tr key={l.id}>
                <td className="px-4 py-3 font-medium">{l.titre}</td>
                <td className="px-4 py-3 text-ink-400 hidden sm:table-cell">{l.auteur ?? '—'}</td>
                <td className="px-4 py-3">{l.exemplairesDisponibles}/{l.nombreExemplaires}</td>
                <td className="px-4 py-3">
                  <Bouton variante="discret" disabled={l.exemplairesDisponibles === 0 || enCours} onClick={() => emprunter(l.id)}>
                    Emprunter
                  </Bouton>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
