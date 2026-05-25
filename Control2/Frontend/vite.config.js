import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  // --- SECCIÓN AÑADIDA PARA SOLUCIONAR RECARGA EN DOCKER ---
  server: {
    host: true, // Permite que el contenedor exponga el puerto hacia afuera
    watch: {
      usePolling: true, // Fuerza a Vite a inspeccionar cambios mediante intervalos de tiempo
    },
  },
})
