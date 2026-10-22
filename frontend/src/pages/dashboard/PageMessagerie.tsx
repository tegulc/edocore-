import { useEffect, useState } from 'react';
import { MessageSquare, Loader2, Send } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { chatApi } from '@/lib/modulesApi';
import { utilisateursApi } from '@/lib/utilisateursApi';
import { extraireErreurApi } from '@/lib/authApi';
import { useAuth } from '@/hooks/useAuth';
import type { ConversationProfil, MessageProfil } from '@/types/modules';
import type { UtilisateurProfil } from '@/types/auth';

export default function PageMessagerie() {
  const { utilisateur } = useAuth();
  const [conversations, setConversations] = useState<ConversationProfil[]>([]);
  const [active, setActive] = useState<number | null>(null);
  const [messages, setMessages] = useState<MessageProfil[]>([]);
  const [texte, setTexte] = useState('');
  const [utilisateurs, setUtilisateurs] = useState<UtilisateurProfil[]>([]);
  const [nouveauDest, setNouveauDest] = useState<number | null>(null);
  const [enCours, setEnCours] = useState(false);
  const [erreur, setErreur] = useState<string | null>(null);
  const [serviceIndisponible, setServiceIndisponible] = useState(false);

  async function chargerConversations() {
    try {
      const { data } = await chatApi.conversations();
      setConversations(data);
      setServiceIndisponible(false);
    } catch {
      setServiceIndisponible(true);
    }
  }

  useEffect(() => {
    chargerConversations();
    utilisateursApi.lister(1, 100).then(({ data }) => setUtilisateurs(data.donnees)).catch(() => {});
  }, []);

  async function ouvrirConversation(id: number) {
    setActive(id);
    try {
      const { data } = await chatApi.messages(id);
      setMessages(data);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    }
  }

  async function envoyer() {
    if (!active || !texte.trim()) return;
    setEnCours(true);
    try {
      await chatApi.envoyer(active, texte.trim());
      setTexte('');
      const { data } = await chatApi.messages(active);
      setMessages(data);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  async function creerConversation() {
    if (!nouveauDest || !utilisateur) return;
    setEnCours(true);
    try {
      const { data } = await chatApi.creer({ participantIds: [nouveauDest] });
      await chargerConversations();
      ouvrirConversation(data.id);
      setNouveauDest(null);
    } catch (err) {
      setErreur(extraireErreurApi(err).message);
    } finally {
      setEnCours(false);
    }
  }

  if (serviceIndisponible) {
    return (
      <div className="space-y-6">
        <h1 className="font-display text-2xl font-bold">Messagerie</h1>
        <Card>
          <p className="text-sm text-ink-400">Le service de messagerie (port 8081) n'est pas disponible. Lancez-le avec :</p>
          <code className="mt-2 block text-xs bg-ink-50 dark:bg-white/5 p-3 rounded-lg">docker compose up -d notifications-service</code>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="font-display text-2xl font-bold">Messagerie</h1>
        <p className="text-sm text-ink-400 mt-1">Conversations avec les membres de l'établissement.</p>
      </div>

      <Card>
        <div className="flex gap-2">
          <select value={nouveauDest ?? ''} onChange={(e) => setNouveauDest(Number(e.target.value) || null)}
            className="flex-1 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3.5 py-2.5 text-sm">
            <option value="">Nouvelle conversation avec…</option>
            {utilisateurs.filter((u) => u.id !== utilisateur?.id).map((u) => (
              <option key={u.id} value={u.id}>{u.prenom} {u.nom}</option>
            ))}
          </select>
          <Bouton onClick={creerConversation} disabled={!nouveauDest || enCours}><MessageSquare size={16} /> Démarrer</Bouton>
        </div>
      </Card>

      <div className="grid lg:grid-cols-3 gap-4 h-[500px]">
        <Card className="lg:col-span-1 overflow-y-auto p-0">
          {conversations.length === 0 ? <p className="p-4 text-sm text-ink-400">Aucune conversation.</p> : conversations.map((c) => (
            <button key={c.id} onClick={() => ouvrirConversation(c.id)}
              className={`w-full text-left px-4 py-3 border-b border-ink-100 dark:border-white/10 hover:bg-ink-50 dark:hover:bg-white/5 ${active === c.id ? 'bg-brand-50 dark:bg-brand-500/10' : ''}`}>
              <p className="text-sm font-medium">{c.titre ?? `Conversation #${c.id}`}</p>
              <p className="text-xs text-ink-400">{c.participantIds.length} participant(s)</p>
            </button>
          ))}
        </Card>

        <Card className="lg:col-span-2 flex flex-col">
          {active ? (
            <>
              <div className="flex-1 overflow-y-auto space-y-3 p-4">
                {messages.map((m) => (
                  <div key={m.id} className={`flex ${m.expediteurId === utilisateur?.id ? 'justify-end' : 'justify-start'}`}>
                    <div className={`max-w-[75%] rounded-lg px-3 py-2 text-sm ${m.expediteurId === utilisateur?.id ? 'bg-brand-500 text-white' : 'bg-ink-50 dark:bg-white/5'}`}>
                      {m.contenu}
                    </div>
                  </div>
                ))}
              </div>
              <div className="border-t border-ink-100 dark:border-white/10 p-3 flex gap-2">
                <input value={texte} onChange={(e) => setTexte(e.target.value)} onKeyDown={(e) => e.key === 'Enter' && envoyer()}
                  placeholder="Votre message…" className="flex-1 rounded-lg border border-ink-100 dark:border-white/10 bg-white dark:bg-surface-darkAlt px-3 py-2 text-sm" />
                <Bouton onClick={envoyer} disabled={enCours || !texte.trim()}>
                  {enCours ? <Loader2 size={16} className="animate-spin" /> : <Send size={16} />}
                </Bouton>
              </div>
            </>
          ) : (
            <p className="flex-1 flex items-center justify-center text-sm text-ink-400">Sélectionnez une conversation.</p>
          )}
          {erreur && <p className="px-4 pb-3 text-sm text-red-600">{erreur}</p>}
        </Card>
      </div>
    </div>
  );
}
