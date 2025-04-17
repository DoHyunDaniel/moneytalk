// filepath: c:\Users\KDH\git\moneytalk\moneytalk_frontend\vite.config.ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';
import { nodePolyfills } from 'vite-plugin-node-polyfills';

export default defineConfig({
  plugins: [
    react(),
    nodePolyfills()
  ],
  resolve: {
    alias: {
      global: 'global',
    },
  },
});