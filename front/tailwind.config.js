/** @type {import('tailwindcss').Config} */
module.exports = {
    content: [
        './app/**/*.{js,ts,jsx,tsx}',
        './components/**/*.{js,ts,jsx,tsx}',
        './src/**/*.{js,ts,jsx,tsx}', // ← src/app 포함 시 필요
    ],
    theme: {
        extend: {},
    },
    plugins: [],
};