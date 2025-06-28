// tailwind.config.ts
import type { Config } from 'tailwindcss';

const config: Config = {
  content: [
    './src/pages/**/*.{js,ts,jsx,tsx}',
    './src/components/**/*.{js,ts,jsx,tsx}',
    './src/app/**/*.{js,ts,jsx,tsx}', // Next.js 앱 라우팅 구조를 사용하는 경우
  ],
  theme: {
    extend: {
      fontFamily: {
        minhye: ['"Minhye"', 'serif'],
      },
    },
  },
  plugins: [],
};

export default config;
