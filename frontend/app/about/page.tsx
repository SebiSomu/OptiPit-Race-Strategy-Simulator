"use client";

import Link from "next/link";
import { Navbar } from "@/components/Navbar";

export default function AboutPage() {
  return (
    <div className="min-h-screen flex flex-col bg-[var(--f1-dark)]">
      <Navbar />

      {/* Hero Section */}
      <section className="relative py-20 border-b border-white/5">
        <div className="max-w-4xl mx-auto px-6 text-center">
          <div className="animate-fade-up">
            <span className="inline-block px-4 py-1.5 text-[11px] font-bold uppercase tracking-[0.25em] text-[var(--f1-red)] border border-[var(--f1-red)]/20 rounded-full bg-[var(--f1-red)]/5 mb-6">
              Behind the Scenes
            </span>
            <h1 className="text-4xl sm:text-5xl md:text-6xl font-black uppercase tracking-tight text-white mb-4">
              The <span className="text-[var(--f1-red)]">Algorithm</span>
            </h1>
            <p className="text-lg text-white/50 max-w-2xl mx-auto leading-relaxed">
              How do we calculate optimal strategies? Here is what happens under the hood 
              when you hit the simulate button.
            </p>
          </div>
        </div>
      </section>

      {/* Content */}
      <main className="flex-1 py-16">
        <div className="max-w-4xl mx-auto px-6 space-y-16">

          {/* Section 1: Big Picture */}
          <section className="animate-fade-in">
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-3">
              <span className="w-8 h-8 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center text-[var(--f1-red)] text-sm">01</span>
              The Big Picture
            </h2>
            <div className="prose prose-invert max-w-none">
              <p className="text-white/60 leading-relaxed">
                Our algorithm uses an <strong className="text-white">intelligent exhaustive search</strong> — 
                systematically testing every possible combination of pit stops and tyre compounds, 
                then comparing them to find the fastest race strategy.
              </p>
              <p className="text-white/60 leading-relaxed">
                Think of it like computer chess: instead of guessing, the computer 
                &quot;plays&quot; every possible scenario and picks the best one.
              </p>
            </div>
          </section>

          {/* Section 2: The Strategy Types */}
          <section className="animate-fade-in animation-delay-100">
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-3">
              <span className="w-8 h-8 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center text-[var(--f1-red)] text-sm">02</span>
              Strategy Types We Analyze
            </h2>
            
            <div className="grid gap-4 mt-6">
              <div className="glass rounded-xl p-6 racing-stripe">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 rounded-lg bg-green-500/10 flex items-center justify-center shrink-0">
                    <span className="text-green-400 font-bold">1</span>
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-white mb-2">One-Stop Strategy</h3>
                    <p className="text-sm text-white/50 leading-relaxed">
                      We test all 2-compound combinations (Soft→Medium, Medium→Hard, etc.) 
                      and find the optimal pit window — typically between lap 3 and N-3. 
                      We skip strategies using the same compound twice (no point in changing 
                      to the same tyre).
                    </p>
                  </div>
                </div>
              </div>

              <div className="glass rounded-xl p-6 racing-stripe">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 rounded-lg bg-yellow-500/10 flex items-center justify-center shrink-0">
                    <span className="text-yellow-400 font-bold">2</span>
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-white mb-2">Two-Stop Strategy</h3>
                    <p className="text-sm text-white/50 leading-relaxed">
                      Here complexity increases: we test all 3-compound combinations 
                      (e.g., Soft→Medium→Soft) and find the best 2 pit stop moments. 
                      We enforce realistic rules: minimum 3 laps between stops, strategies with at least 
                      2 different compound types.
                    </p>
                  </div>
                </div>
              </div>

              <div className="glass rounded-xl p-6 racing-stripe">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 rounded-lg bg-red-500/10 flex items-center justify-center shrink-0">
                    <span className="text-red-400 font-bold">3</span>
                  </div>
                  <div>
                    <h3 className="text-lg font-bold text-white mb-2">Three-Stop Strategy</h3>
                    <p className="text-sm text-white/50 leading-relaxed">
                      The most aggressive strategy — 4 race segments with multiple pit stops. 
                      Here we use smart optimization: we search &quot;coarsely&quot; at first 
                      (every 3 laps), then &quot;refine&quot; around the best results found. 
                      We save computation time without losing accuracy.
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </section>

          {/* Section 3: The Physics */}
          <section className="animate-fade-in animation-delay-200">
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-3">
              <span className="w-8 h-8 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center text-[var(--f1-red)] text-sm">03</span>
              The Physics Model
            </h2>
            
            <div className="glass rounded-xl p-6">
              <p className="text-white/60 leading-relaxed mb-4">
                For every simulated lap, we calculate lap time using a complex physics 
                model that accounts for:
              </p>
              
              <ul className="space-y-3">
                <li className="flex items-start gap-3">
                  <span className="w-1.5 h-1.5 rounded-full bg-[var(--f1-red)] mt-2 shrink-0" />
                  <span className="text-white/50 text-sm">
                    <strong className="text-white/70">Tyre Degradation:</strong> Each compound loses grip 
                    with every lap — Soft degrades fast, Hard lasts longer
                  </span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="w-1.5 h-1.5 rounded-full bg-[var(--f1-red)] mt-2 shrink-0" />
                  <span className="text-white/50 text-sm">
                    <strong className="text-white/70">Track Evolution:</strong> As the race progresses, 
                    the track gains &quot;rubber&quot; from cars (rubbering-in), improving grip
                  </span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="w-1.5 h-1.5 rounded-full bg-[var(--f1-red)] mt-2 shrink-0" />
                  <span className="text-white/50 text-sm">
                    <strong className="text-white/70">Temperature:</strong> Tyres have an &quot;optimal window&quot; 
                    — too cold = no grip, too hot = accelerated degradation
                  </span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="w-1.5 h-1.5 rounded-full bg-[var(--f1-red)] mt-2 shrink-0" />
                  <span className="text-white/50 text-sm">
                    <strong className="text-white/70">Fuel Load:</strong> The car gets faster 
                    as fuel burns off (approximately 0.03s per lap of fuel consumed)
                  </span>
                </li>
                <li className="flex items-start gap-3">
                  <span className="w-1.5 h-1.5 rounded-full bg-[var(--f1-red)] mt-2 shrink-0" />
                  <span className="text-white/50 text-sm">
                    <strong className="text-white/70">Wind and Rain:</strong> Headwinds slow you down, 
                    rain completely changes dynamics (wet vs intermediate)
                  </span>
                </li>
              </ul>
            </div>
          </section>

          {/* Section 4: Precomputation */}
          <section className="animate-fade-in animation-delay-300">
            <h2 className="text-2xl font-bold text-white mb-4 flex items-center gap-3">
              <span className="w-8 h-8 rounded-lg bg-[var(--f1-red)]/10 flex items-center justify-center text-[var(--f1-red)] text-sm">04</span>
              Optimization: Precomputation
            </h2>
            
            <div className="glass rounded-xl p-6">
              <p className="text-white/60 leading-relaxed mb-4">
                To be fast, we use a technique called <strong className="text-white">&quot;precomputation&quot;</strong>:
              </p>
              
              <div className="bg-black/20 rounded-lg p-4 font-mono text-sm text-white/40 overflow-x-auto">
                <p className="text-green-400/60"># Before searching strategies...</p>
                <p>for each compound:</p>
                <p className="pl-4">for each possible start lap:</p>
                <p className="pl-8">for each possible stint length:</p>
                <p className="pl-12">calculate total time for that stint</p>
                <p className="text-yellow-400/60 mt-2"># Result: a massive table with all scenarios</p>
              </div>
              
              <p className="text-white/60 leading-relaxed mt-4">
                Now, instead of recalculating times for every lap in every strategy, 
                we simply <em>read</em> from the table. It is the difference between reading from a book 
                vs writing the book every time you need information.
              </p>
            </div>
          </section>

          {/* Section 5: Summary */}
          <section className="animate-fade-in animation-delay-400">
            <h2 className="text-xl font-bold text-white mb-6 text-center">
              Key Algorithm Characteristics
            </h2>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-[var(--f1-red)]/10 flex items-center justify-center shrink-0">
                  <svg className="w-4 h-4 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>
                <div>
                  <h4 className="text-sm font-bold text-white">Exhaustive</h4>
                  <p className="text-xs text-white/40">Tests all valid combinations, not just &quot;guesses&quot;</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-[var(--f1-red)]/10 flex items-center justify-center shrink-0">
                  <svg className="w-4 h-4 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                </div>
                <div>
                  <h4 className="text-sm font-bold text-white">Fast</h4>
                  <p className="text-xs text-white/40">Precomputation + optimizations = results under 100ms</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-[var(--f1-red)]/10 flex items-center justify-center shrink-0">
                  <svg className="w-4 h-4 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div>
                  <h4 className="text-sm font-bold text-white">Physically Accurate</h4>
                  <p className="text-xs text-white/40">Models degradation, temperature, fuel, wind</p>
                </div>
              </div>

              <div className="flex items-start gap-3">
                <div className="w-8 h-8 rounded-full bg-[var(--f1-red)]/10 flex items-center justify-center shrink-0">
                  <svg className="w-4 h-4 text-[var(--f1-red)]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
                  </svg>
                </div>
                <div>
                  <h4 className="text-sm font-bold text-white">Flexible</h4>
                  <p className="text-xs text-white/40">Adjustable for any circuit, temperature, conditions</p>
                </div>
              </div>
            </div>
          </section>

          {/* CTA */}
          <div className="text-center py-8 animate-fade-in animation-delay-500">
            <p className="text-white/40 mb-4">
              Want to see the algorithm in action?
            </p>
            <Link
              href="/simulation" 
              className="inline-flex items-center gap-2 btn-f1"
            >
              Start Simulation
              <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7l5 5m0 0l-5 5m5-5H6" />
              </svg>
            </Link>
          </div>
        </div>
      </main>

    </div>
  );
}
