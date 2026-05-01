import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({
  variable: "--font-sans",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "OptiPit | Advanced F1 Race Strategy Simulator",
  description:
    "Professional-grade Formula 1 race strategy engine. Calculate optimal pit stop windows, tyre strategies, and race pace for the entire 2026 calendar.",
  keywords: ["F1", "Formula 1", "OptiPit", "race strategy", "pit stop", "tyre strategy", "2026 calendar", "simulator"],
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="en" className={`${inter.variable} dark h-full antialiased`}>
      <body className="min-h-full flex flex-col">{children}</body>
    </html>
  );
}
