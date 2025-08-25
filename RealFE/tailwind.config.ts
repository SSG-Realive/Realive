// tailwind.config.ts
import type { Config } from 'tailwindcss';
import lineClamp from '@tailwindcss/line-clamp';

const config: Config = {
  content: [
    './app/**/*.{ts,tsx}',
    './components/**/*.{ts,tsx}',
    './pages/**/*.{ts,tsx}',
  ],
  theme: {
    extend: {
      fontFamily: {
        scdLight: ['S-CoreDream-3Light', 'sans-serif'],
      },
    },
  },
  plugins: [lineClamp],
};

export default config;
