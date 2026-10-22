import { useEffect, useState, type FormEvent } from 'react';
import { Loader2, Plus, ExternalLink } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { documentApi } from '@/lib/modulesApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useSelecteursPedagogiques } from '@/hooks/useSelecteursPedagogiques';
import type { DocumentProfil } from '@/types/pedagogie';

const TYPES_DOCUMENT = ['acte', 'diplome', 'bulletin', 'attestation', 'contrat', 'certificat_medical', 'photo', 'autre'];

export default function PageDocuments() {
  const { eleves, chargement } = useSelecteursPedagogiques(true);
  const [documents, setDocuments] = useState<DocumentProfil[]>([]);
  const [filtreEleve, setFiltreEleve] = useState('');
  const [formulaireOuvert, setFormulaireOuvert] = useState(false);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);

  async function charger(params?: { proprietaireType?: string; proprietaireId?: number }) {
    const { data } = await documentApi.lister(params);
    setDocuments(data);
  }

  useEffect(() => { charger(); }, []);

  useEffect(() => {
    if (filtreEleve) {
      charger({ proprietaireType: 'eleve', proprietaireId: Number(filtreEleve) });
    } else {
      charger();
    }
  }, [filtreEleve]);

  async function gererCreation(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    const fd = new FormData(e.currentTarget);
    setEnCours(true);
    setErreur(null);
    try {
      await documentApi.creer({
        proprietaireType: fd.get('proprietaireType') as string,
        proprietaireId: Number(fd.get('proprietaireId')),
        typeDocument: fd.get('typeDocument') as string,
        titre: fd.get('titre') as string,
        fichierUrl: fd.get('fichierUrl') as string,
      });
      setFormulaireOuvert(false);
      await charger(filtreEleve ? { proprietaireType: 'eleve', proprietaireId: Number(filtreEleve) } : undefined);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  if (chargement) {
    return <div className="flex justify-center py-12"><Loader2 size={24} className="animate-spin text-ink-400" /></div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Documents</h1>
          <p className="text-sm text-ink-400 mt-1">Archivage centralisé des documents officiels.</p>
        </div>
        <Bouton onClick={() => setFormulaireOuvert((v) => !v)}><Plus size={16} /> Archiver</Bouton>
      </div>

      <Card>
        <label className="block max-w-md">
          <span className="text-sm font-medium">Filtrer par élève</span>
          <select value={filtreEleve} onChange={(e) => setFiltreEleve(e.target.value)}
            className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
            <option value="">Tous les documents</option>
            {eleves.map((e) => <option key={e.id} value={e.id}>{e.prenom} {e.nom}</option>)}
          </select>
        </label>
      </Card>

      {formulaireOuvert && (
        <Card>
          <form onSubmit={gererCreation} className="grid sm:grid-cols-2 gap-4">
            <label className="block"><span className="text-sm font-medium">Type propriétaire</span>
              <select name="proprietaireType" defaultValue="eleve" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                <option value="eleve">Élève</option>
                <option value="utilisateur">Utilisateur</option>
                <option value="etablissement">Établissement</option>
              </select></label>
            <label className="block"><span className="text-sm font-medium">ID propriétaire</span>
              <input required type="number" name="proprietaireId" defaultValue={filtreEleve || ''} className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block"><span className="text-sm font-medium">Type document</span>
              <select required name="typeDocument" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
                {TYPES_DOCUMENT.map((t) => <option key={t} value={t}>{t}</option>)}
              </select></label>
            <label className="block"><span className="text-sm font-medium">Titre</span>
              <input required name="titre" className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            <label className="block sm:col-span-2"><span className="text-sm font-medium">URL du fichier</span>
              <input required name="fichierUrl" placeholder="https://..." className="mt-1.5 w-full rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm" /></label>
            {erreur && <p className="sm:col-span-2 text-sm text-red-600">{erreur}</p>}
            <div className="sm:col-span-2 flex gap-2">
              <Bouton type="submit" disabled={enCours}>{enCours && <Loader2 size={16} className="animate-spin" />} Archiver</Bouton>
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
              <th className="px-4 py-3 font-medium hidden sm:table-cell">Type</th>
              <th className="px-4 py-3 font-medium hidden md:table-cell">Date</th>
              <th className="px-4 py-3 font-medium">Lien</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-ink-100 dark:divide-white/10">
            {documents.length === 0 && <tr><td colSpan={4} className="px-4 py-6 text-center text-ink-400">Aucun document archivé.</td></tr>}
            {documents.map((d) => (
              <tr key={d.id}>
                <td className="px-4 py-3 font-medium">{d.titre}</td>
                <td className="px-4 py-3 text-ink-400 hidden sm:table-cell">{d.typeDocument}</td>
                <td className="px-4 py-3 text-ink-400 hidden md:table-cell">{new Date(d.dateAjout).toLocaleDateString('fr-FR')}</td>
                <td className="px-4 py-3">
                  <a href={d.fichierUrl} target="_blank" rel="noreferrer" className="inline-flex items-center gap-1 text-brand-600 hover:underline text-xs">
                    <ExternalLink size={12} /> Ouvrir
                  </a>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}
