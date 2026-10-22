/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: {
    extend: {
      colors: {
        // Palette EDUCORE - moderne/corporate, validee avec l'utilisateur
        brand: {
          50: '#EEF2FE',
          100: '#DCE4FD',
          300: '#7C97F2',
          500: '#2F5DE3', // bleu primaire
          600: '#1E4FD6',
          700: '#173FAD',
          900: '#0F2A73',
        },
        accent: {
          400: '#2DD4C7',
          500: '#0EA5A4', // teal signature - differencie du bleu SaaS generique
          600: '#0B8483',
        },
        ink: {
          50: '#F5F7FB',   // fond clair
          100: '#EBEEF5',
          400: '#5B6478',
          700: '#232A3B',
          900: '#0B1220',  // texte fonce / fond mode sombre
        },
        surface: {
          light: '#FFFFFF',
          dark: '#121A2B',
          darkAlt: '#0F1725',
        },
      },
      fontFamily: {
        display: ['"Sora"', 'sans-serif'],
        body: ['"Inter"', 'sans-serif'],
        mono: ['"JetBrains Mono"', 'monospace'],
      },
      boxShadow: {
        card: '0 1px 2px 0 rgb(11 18 32 / 0.04), 0 1px 3px 0 rgb(11 18 32 / 0.06)',
        'card-dark': '0 1px 2px 0 rgb(0 0 0 / 0.3), 0 1px 3px 0 rgb(0 0 0 / 0.4)',
      },
      borderRadius: {
        xl2: '1rem',
      },
    },
  },
  plugins: [],
};
