import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { fileURLToPath, URL } from 'node:url';

const backendTarget = process.env.VITE_BACKEND_URL ?? 'http://localhost:8080';
const notificationsTarget = process.env.VITE_NOTIFICATIONS_URL ?? 'http://localhost:8081';

export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: backendTarget,
        changeOrigin: true,
      },
      '/notif': {
        target: notificationsTarget,
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/notif/, ''),
      },
    },
  },
});
