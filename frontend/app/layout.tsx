import type { Metadata } from "next";
import { Inter } from "next/font/google";
import "./globals.css";

const inter = Inter({
  variable: "--font-sans",
  subsets: ["latin"],
  display: "swap",
});

export const metadata: Metadata = {
  title: "F1 Race Strategy Simulator | Real-Time Pit Stop Optimizer",
  description:
    "Advanced Formula 1 race strategy simulator. Calculate optimal pit stop windows, tyre compound strategies, and race pace for the Miami International Autodrome.",
  keywords: ["F1", "Formula 1", "race strategy", "pit stop", "tyre strategy", "Miami GP", "simulator"],
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
