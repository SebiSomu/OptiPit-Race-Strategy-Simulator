"use client";

import Link from "next/link";
import { Navbar } from "@/components/Navbar";
import { useEffect, useState } from "react";

function AnimatedCounter({ target, label, suffix = "" }: { target: number; label: string; suffix?: string }) {
  const [count, setCount] = useState(0);

  useEffect(() => {
    const duration = 2000;
    const steps = 60;
    const increment = target / steps;
    let current = 0;
    const timer = setInterval(() => {
      current += increment;
      if (current >= target) {
        setCount(target);
        clearInterval(timer);
      } else {
        setCount(Math.floor(current));
      }
    }, duration / steps);
    return () => clearInterval(timer);
  }, [target]);

  return (
    <div className="text-center">
      <div className="text-3xl md:text-4xl font-black text-white">
        {count}{suffix}
      </div>
      <div className="text-xs uppercase tracking-widest text-white/40 mt-1">{label}</div>
    </div>
  );
}

export default function HomePage() {
  return (
    <div className="min-h-screen">
      <Navbar />

      {/* ─── Hero Section ──────────────────────────── */}
      <section className="hero-bg relative min-h-screen flex items-center justify-center overflow-hidden">
        {/* Speed Lines */}
        <div className="speed-line top-[20%] left-0 w-[40%]" style={{ animationDelay: "0s" }} />
        <div className="speed-line top-[35%] left-0 w-[60%]" style={{ animationDelay: "1.5s" }} />
        <div className="speed-line top-[55%] left-0 w-[35%]" style={{ animationDelay: "0.8s" }} />
        <div className="speed-line top-[70%] left-0 w-[50%]" style={{ animationDelay: "2.2s" }} />
        <div className="speed-line top-[85%] left-0 w-[45%]" style={{ animationDelay: "3s" }} />

        <div className="relative z-10 max-w-5xl mx-auto px-6 text-center">
          {/* Tagline */}
          <div className="animate-slide-up mb-6">
            <span className="inline-block px-4 py-1.5 text-[11px] font-bold uppercase tracking-[0.25em] text-[var(--f1-red)] border border-[var(--f1-red)]/20 rounded-full bg-[var(--f1-red)]/5">
              Real-Time Strategy Engine
            </span>
          </div>

          {/* Main Title */}
          <h1 className="animate-slide-up-delay-1 text-5xl sm:text-6xl md:text-8xl font-black uppercase leading-[0.9] tracking-tight">
            <span className="block text-white">Race</span>
            <span className="block text-transparent bg-clip-text bg-gradient-to-r from-[var(--f1-red)] via-[var(--f1-red-glow)] to-[var(--f1-red)] animate-gradient animate-text-glow">
              Strategy
            </span>
            <span className="block text-white/80 text-3xl sm:text-4xl md:text-5xl mt-2 font-extrabold tracking-wider">
              Simulator
            </span>
          </h1>

          {/* Description */}
          <p className="animate-slide-up-delay-2 mt-8 text-base md:text-lg text-white/50 max-w-2xl mx-auto leading-relaxed">
            Calculate the most optimal pit stop strategies for the{" "}
            <span className="text-white font-semibold">Miami International Autodrome</span>.
            Analyze 1-stop, 2-stop, and 3-stop strategies across Soft, Medium, and Hard tyre compounds.
          </p>

          {/* CTA */}
          <div className="animate-slide-up-delay-3 mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link href="/simulation">
              <button className="btn-f1 text-sm">
                Launch Simulator
                <svg className="inline-block ml-2 w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </button>
            </Link>
            <a href="#features" className="text-sm font-semibold text-white/40 hover:text-white/70 transition-colors uppercase tracking-wider">
              Learn More ↓
            </a>
          </div>
        </div>

        {/* Bottom gradient fade */}
        <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-[var(--f1-dark)] to-transparent pointer-events-none" />
      </section>

      {/* ─── Stats Bar ─────────────────────────────── */}
      <section className="relative bg-[var(--f1-dark)] py-16 border-y border-white/5">
        <div className="max-w-5xl mx-auto px-6 grid grid-cols-2 md:grid-cols-4 gap-8">
          <AnimatedCounter target={57} label="Race Laps" />
          <AnimatedCounter target={3} label="Tyre Compounds" />
          <AnimatedCounter target={3} label="Strategy Types" />
          <AnimatedCounter target={21} label="Pit Stop Loss" suffix="s" />
        </div>
      </section>

      {/* ─── Features ──────────────────────────────── */}
      <section id="features" className="relative bg-[var(--f1-dark)] py-24">
        <div className="max-w-6xl mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-black uppercase tracking-tight text-white">
              Powered by{" "}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-[var(--f1-red)] to-[var(--f1-red-glow)]">
                Data
              </span>
            </h2>
            <p className="mt-4 text-white/40 max-w-lg mx-auto">
              Our strategy engine evaluates thousands of possible pit stop combinations to find the fastest race strategy.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Feature 1 */}
            <div className="glass glass-hover rounded-xl p-8 racing-stripe">
              <div className="w-12 h-12 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center mb-5">
                <svg className="w-6 h-6 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 3v11.25A2.25 2.25 0 006 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0118 16.5h-2.25m-7.5 0h7.5m-7.5 0l-1 3m8.5-3l1 3m0 0l.5 1.5m-.5-1.5h-9.5m0 0l-.5 1.5m.75-9l3-3 2.148 2.148A12.061 12.061 0 0116.5 7.605" />
                </svg>
              </div>
              <h3 className="text-lg font-bold text-white mb-2">Multi-Stop Analysis</h3>
              <p className="text-sm text-white/40 leading-relaxed">
                Evaluates 1-stop, 2-stop, and 3-stop strategies simultaneously, ranking them by total race time.
              </p>
            </div>

            {/* Feature 2 */}
            <div className="glass glass-hover rounded-xl p-8 racing-stripe">
              <div className="w-12 h-12 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center mb-5">
                <svg className="w-6 h-6 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M12 6v6h4.5m4.5 0a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h3 className="text-lg font-bold text-white mb-2">Optimal Pit Windows</h3>
              <p className="text-sm text-white/40 leading-relaxed">
                Pinpoints the exact lap to pit for each stint, minimizing total race time with tyre degradation modeling.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="glass glass-hover rounded-xl p-8 racing-stripe">
              <div className="w-12 h-12 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center mb-5">
                <svg className="w-6 h-6 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M9.813 15.904L9 18.75l-.813-2.846a4.5 4.5 0 00-3.09-3.09L2.25 12l2.846-.813a4.5 4.5 0 003.09-3.09L9 5.25l.813 2.846a4.5 4.5 0 003.09 3.09L15.75 12l-2.846.813a4.5 4.5 0 00-3.09 3.09zM18.259 8.715L18 9.75l-.259-1.035a3.375 3.375 0 00-2.455-2.456L14.25 6l1.036-.259a3.375 3.375 0 002.455-2.456L18 2.25l.259 1.035a3.375 3.375 0 002.455 2.456L21.75 6l-1.036.259a3.375 3.375 0 00-2.455 2.456z" />
                </svg>
              </div>
              <h3 className="text-lg font-bold text-white mb-2">Compound Strategy</h3>
              <p className="text-sm text-white/40 leading-relaxed">
                Tests every combination of Soft, Medium, and Hard compounds to find the fastest possible tire strategy.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* ─── Footer ────────────────────────────────── */}
      <footer className="bg-[var(--f1-dark)] border-t border-white/5 py-8">
        <div className="max-w-6xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-4">
          <div className="text-xs text-white/30 uppercase tracking-wider">
            F1 Race Strategy Simulator © 2026
          </div>
          <div className="text-xs text-white/20">
            Built with Next.js • Spring Boot • PostgreSQL
          </div>
        </div>
      </footer>
    </div>
  );
}
