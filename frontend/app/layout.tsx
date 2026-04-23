import './globals.css';
import { Manrope, IBM_Plex_Mono } from 'next/font/google';

const manrope = Manrope({ subsets: ['latin'], variable: '--font-sans' });
const mono = IBM_Plex_Mono({ weight: '400', subsets: ['latin'], variable: '--font-mono' });

export const metadata = {
  title: 'AI GitHub Codebase Intelligence',
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="en" className={`${manrope.variable} ${mono.variable}`}>
      <body>{children}</body>
    </html>
  );
}
