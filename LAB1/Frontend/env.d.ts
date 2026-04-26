/// <reference types="vite/client" />

// Ayuda a TypeScript a entender los archivos .vue como componentes de Vue
declare module '*.vue' {
  import type { DefineComponent } from 'vue'
  // eslint-disable-next-line @typescript-eslint/no-explicit-any, @typescript-eslint/ban-types
  const component: DefineComponent<{}, {}, any>
  export default component
}