import { useEffect, useState } from 'react';
import { Bell } from 'lucide-react';
import { Card } from '@/components/ui/Card';
import { Bouton } from '@/components/ui/Bouton';
import { notificationClientApi } from '@/lib/modulesApi';
import type { NotificationProfil } from '@/types/modules';

export default function PageNotifications() {
  const [notifications, setNotifications] = useState<NotificationProfil[]>([]);
  const [chargement, setChargement] = useState(true);
  const [serviceIndisponible, setServiceIndisponible] = useState(false);

  async function charger() {
    setChargement(true);
    try {
      const { data } = await notificationClientApi.lister();
      setNotifications(data);
      setServiceIndisponible(false);
    } catch {
      setServiceIndisponible(true);
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => { charger(); }, []);

  async function marquerLue(id: number) {
    await notificationClientApi.marquerLue(id);
    setNotifications((prev) => prev.map((n) => (n.id === id ? { ...n, lue: true } : n)));
  }

  const nonLues = notifications.filter((n) => !n.lue).length;

  if (serviceIndisponible) {
    return (
      <div className="space-y-6">
        <h1 className="font-display text-2xl font-bold">Notifications</h1>
        <Card>
          <p className="text-sm text-ink-400">Le service notifications (port 8081) n'est pas disponible.</p>
          <code className="mt-2 block text-xs bg-ink-50 dark:bg-white/5 p-3 rounded-lg">docker compose up -d notifications-service</code>
        </Card>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="font-display text-2xl font-bold">Notifications</h1>
          <p className="text-sm text-ink-400 mt-1">{nonLues > 0 ? `${nonLues} non lue(s)` : 'Toutes lues.'}</p>
        </div>
        <Bouton variante="discret" onClick={charger}><Bell size={16} /> Actualiser</Bouton>
      </div>

      <Card className="p-0 divide-y divide-ink-100 dark:divide-white/10">
        {chargement && <p className="px-4 py-6 text-sm text-center text-ink-400">Chargement…</p>}
        {!chargement && notifications.length === 0 && <p className="px-4 py-6 text-sm text-center text-ink-400">Aucune notification.</p>}
        {notifications.map((n) => (
          <div key={n.id} className={`px-4 py-4 flex items-start justify-between gap-4 ${!n.lue ? 'bg-brand-50/50 dark:bg-brand-500/5' : ''}`}>
            <div>
              <p className="text-sm font-medium">{n.titre}</p>
              {n.contenu && <p className="text-sm text-ink-400 mt-1">{n.contenu}</p>}
              <p className="text-xs text-ink-400 mt-1 capitalize">{n.categorie} · {n.canal}</p>
            </div>
            {!n.lue && (
              <Bouton variante="discret" onClick={() => marquerLue(n.id)}>Marquer lue</Bouton>
            )}
          </div>
        ))}
      </Card>
    </div>
  );
}
