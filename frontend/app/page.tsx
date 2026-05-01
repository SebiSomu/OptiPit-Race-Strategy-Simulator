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
              Professional Pit Stop Strategy Engine
            </span>
          </div>

          {/* Main Title */}
          <h1 className="animate-slide-up-delay-1 text-6xl sm:text-8xl md:text-9xl font-black uppercase leading-[0.85] tracking-tighter">
            <div className="block">
              <span className="text-white">Opti</span>
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-[var(--f1-red)] via-[var(--f1-red-glow)] to-[var(--f1-red)] animate-gradient animate-text-glow">
                Pit
              </span>
            </div>
            <div className="block text-white/20 text-4xl sm:text-6xl md:text-7xl font-light tracking-[0.3em] mt-4">
              Strategy
            </div>
          </h1>

          {/* Description */}
          <p className="animate-slide-up-delay-2 mt-8 text-base md:text-lg text-white/50 max-w-2xl mx-auto leading-relaxed">
            Master the 2026 Season with the world's most advanced F1 simulation engine. 
            From <span className="text-white font-semibold">Bahrain to Abu Dhabi</span>, 
            OptiPit calculates every possible scenario using real-world physics.
          </p>

          {/* CTA */}
          <div className="animate-slide-up-delay-3 mt-10 flex flex-col sm:flex-row items-center justify-center gap-4">
            <Link href="/simulation">
              <button className="btn-f1 text-sm">
                Enter Mission Control
                <svg className="inline-block ml-2 w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M13 7l5 5m0 0l-5 5m5-5H6" />
                </svg>
              </button>
            </Link>
            <a href="#features" className="text-sm font-semibold text-white/40 hover:text-white/70 transition-colors uppercase tracking-wider">
              Explore Features ↓
            </a>
          </div>
        </div>

        {/* Bottom gradient fade */}
        <div className="absolute bottom-0 left-0 right-0 h-32 bg-gradient-to-t from-[var(--f1-dark)] to-transparent pointer-events-none" />
      </section>

      {/* ─── Stats Bar ─────────────────────────────── */}
      <section className="relative bg-[var(--f1-dark)] py-16 border-y border-white/5">
        <div className="max-w-5xl mx-auto px-6 grid grid-cols-2 md:grid-cols-4 gap-8">
          <AnimatedCounter target={23} label="Circuits Supported" />
          <AnimatedCounter target={3} label="Tyre Compounds" />
          <AnimatedCounter target={50} label="Optimal Strategies" suffix="+" />
          <AnimatedCounter target={0.001} label="Precision" suffix="s" />
        </div>
      </section>

      {/* ─── Features ──────────────────────────────── */}
      <section id="features" className="relative bg-[var(--f1-dark)] py-24">
        <div className="max-w-6xl mx-auto px-6">
          <div className="text-center mb-16">
            <h2 className="text-3xl md:text-4xl font-black uppercase tracking-tight text-white">
              Why{" "}
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-[var(--f1-red)] to-[var(--f1-red-glow)]">
                OptiPit?
              </span>
            </h2>
            <p className="mt-4 text-white/40 max-w-lg mx-auto">
              Built on real Pirelli 2024 data and 2026 regulations, our engine delivers the precision required by professional analysts.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Feature 1 */}
            <div className="glass glass-hover rounded-xl p-8 racing-stripe">
              <div className="w-12 h-12 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center mb-5">
                <svg className="w-6 h-6 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M15.362 5.214A8.252 8.252 0 0112 21 8.25 8.25 0 016.038 7.048 8.287 8.287 0 009 9.6a8.983 8.983 0 013.361-6.867 8.21 8.21 0 003 2.48z" />
                </svg>
              </div>
              <h3 className="text-lg font-bold text-white mb-2">Physics-Based Engine</h3>
              <p className="text-sm text-white/40 leading-relaxed">
                Accounts for track temperature sensitivity, "the cliff" degradation, and track evolution (rubbering-in).
              </p>
            </div>

            {/* Feature 2 */}
            <div className="glass glass-hover rounded-xl p-8 racing-stripe">
              <div className="w-12 h-12 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center mb-5">
                <svg className="w-6 h-6 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607z" />
                </svg>
              </div>
              <h3 className="text-lg font-bold text-white mb-2">Exhaustive Search</h3>
              <p className="text-sm text-white/40 leading-relaxed">
                Unlike simple tools, we test EVERY viable compound sequence (Soft, Medium, Hard) to find the absolute fastest path.
              </p>
            </div>

            {/* Feature 3 */}
            <div className="glass glass-hover rounded-xl p-8 racing-stripe">
              <div className="w-12 h-12 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center mb-5">
                <svg className="w-6 h-6 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.5}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 13.5l10.5-11.25L12 10.5h8.25L9.75 21.75 12 13.5H3.75z" />
                </svg>
              </div>
              <h3 className="text-lg font-bold text-white mb-2">Fuel Weight Logic</h3>
              <p className="text-sm text-white/40 leading-relaxed">
                Dynamically adjusts lap times as fuel is consumed, making end-of-race stint calculations perfectly accurate.
              </p>
            </div>
          </div>
        </div>
      </section>

    </div>
  );
}
