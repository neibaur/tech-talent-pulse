/// <reference types="astro/client" />

interface ImportMetaEnv {
  readonly PUBLIC_TECH_TALENT_PULSE_API_URL?: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
