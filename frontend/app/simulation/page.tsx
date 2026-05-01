"use client";

import React, { useState, useEffect, useCallback } from "react";
import { Navbar } from "@/components/Navbar";
import { StrategyComparison } from "@/components/StrategyComparison";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer,
} from "recharts";

const API_BASE = "http://localhost:8080/api";

interface Circuit {
  id: number;
  name: string;
  laps: number;
  pitStopLoss: number;
  baseLapTime: number;
  trackTempNominal: number;
  trackEvolutionPerLap: number;
}

interface Compound {
  id: number;
  name: string;
  degradationCoefficient: number;
  initialGrip: number;
  tempSensitivity: number;
}

interface StintData {
  compoundName: string;
  startLap: number;
  endLap: number;
  lapsDuration: number;
  stintTime: number;
}

interface LapTimeEntry {
  lap: number;
  lapTime: number;
  compound: string;
}

interface StrategyData {
  strategyType: string;
  stints: StintData[];
  pitStopLaps: number[];
  totalTime: number;
  deltaToOptimal: number;
  lapTimes: LapTimeEntry[];
}

const compoundChartColors: Record<string, string> = {
  Soft: "#ff1801",
  Medium: "#ffc906",
  Hard: "#d0d0d0",
};

function tempLabel(t: number) {
  if (t < 30) return "Cool";
  if (t < 40) return "Mild";
  if (t < 50) return "Hot";
  return "Extreme";
}

export default function SimulationPage() {
  const [circuits, setCircuits] = useState<Circuit[]>([]);
  const [compounds, setCompounds] = useState<Compound[]>([]);
  const [selectedCircuit, setSelectedCircuit] = useState<Circuit | null>(null);
  const [trackTemp, setTrackTemp] = useState<number>(50);
  const [strategies, setStrategies] = useState<StrategyData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedStrategyIdx, setSelectedStrategyIdx] = useState(0);

  useEffect(() => {
    Promise.all([
      fetch(`${API_BASE}/circuits`).then((r) => r.json()),
      fetch(`${API_BASE}/compounds`).then((r) => r.json()),
    ])
      .then(([circs, comps]) => {
        setCircuits(circs);
        setCompounds(comps);
        if (circs.length > 0) {
          setSelectedCircuit(circs[0]);
          setTrackTemp(circs[0].trackTempNominal ?? 50);
        }
      })
      .catch(() => setError("Cannot connect to backend (port 8080). Start the Spring Boot server first."));
  }, []);

  const calculateStrategy = useCallback(async () => {
    if (!selectedCircuit) return;
    setLoading(true);
    setError(null);
    setStrategies([]);
    setSelectedStrategyIdx(0);
    try {
      const url = `${API_BASE}/strategy/optimal?circuitId=${selectedCircuit.id}&trackTemp=${trackTemp}`;
      const res = await fetch(url);
      if (!res.ok) throw new Error();
      setStrategies(await res.json());
    } catch {
      setError("Strategy calculation failed. Check backend logs.");
    } finally {
      setLoading(false);
    }
  }, [selectedCircuit, trackTemp]);

  // ── Chart data ──────────────────────────────────────────────────────────
  const chartLapTimes = strategies[selectedStrategyIdx]?.lapTimes || [];
  const uniqueCompounds = Array.from(new Set(chartLapTimes.map((d) => d.compound)));
  const chartData = chartLapTimes.map((entry) => {
    const pt: Record<string, number | null> = { lap: entry.lap };
    uniqueCompounds.forEach((c) => { pt[c] = entry.compound === c ? entry.lapTime : null; });
    return pt;
  });

  // Group strategies by stop type for section headers
  const stopTypes = Array.from(new Set(strategies.map((s) => s.strategyType)));

  return (
    <div className="min-h-screen bg-[var(--f1-dark)]">
      <Navbar />

      {/* ── Page header ─────────────────────────────────── */}
      <div className="pt-28 pb-6 px-6 max-w-7xl mx-auto">
        <div className="flex items-center gap-3 mb-1">
          <div className="w-1 h-8 bg-[var(--f1-red)] rounded-full" />
          <h1 className="text-3xl md:text-4xl font-black uppercase tracking-tight text-white">
            Strategy Simulator
          </h1>
        </div>
        <p className="text-white/40 text-sm ml-4 pl-1">
          Physics-based tyre model · temperature-aware degradation · all compound combinations
        </p>
      </div>

      <div className="max-w-7xl mx-auto px-6 pb-16">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">

          {/* ── Control panel ───────────────────────────── */}
          <div className="lg:col-span-4 space-y-5">

            {/* Circuit card */}
            <div className="glass rounded-xl p-6 racing-stripe">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-white/50 mb-4">
                Circuit
              </h2>
              <select
                className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-3 text-white text-sm font-medium focus:outline-none focus:border-[var(--f1-red)]/40 transition-colors cursor-pointer appearance-none"
                value={selectedCircuit?.id || ""}
                onChange={(e) => {
                  const c = circuits.find((c) => c.id === Number(e.target.value));
                  if (c) { setSelectedCircuit(c); setTrackTemp(c.trackTempNominal ?? 50); }
                }}
              >
                {circuits.map((c) => (
                  <option key={c.id} value={c.id} className="bg-[#111]">{c.name}</option>
                ))}
              </select>

              {selectedCircuit && (
                <div className="mt-4 grid grid-cols-2 gap-2">
                  <InfoTile label="Race Laps" value={`${selectedCircuit.laps}`} />
                  <InfoTile label="Pit Loss" value={`${selectedCircuit.pitStopLoss}s`} />
                  <InfoTile label="Base Lap" value={`${selectedCircuit.baseLapTime}s`} />
                  <InfoTile label="Nominal Temp" value={`${selectedCircuit.trackTempNominal}°C`} />
                </div>
              )}
            </div>

            {/* Track conditions card */}
            <div className="glass rounded-xl p-6">
              <div className="flex items-center justify-between mb-4">
                <h2 className="text-[11px] font-bold uppercase tracking-widest text-white/50">
                  Track Conditions
                </h2>
                <span className={`text-xs font-bold px-2 py-0.5 rounded-full ${
                  trackTemp >= 50 ? "bg-red-500/20 text-red-400" :
                  trackTemp >= 40 ? "bg-orange-500/20 text-orange-400" :
                  "bg-blue-500/20 text-blue-400"}`}>
                  {tempLabel(trackTemp)}
                </span>
              </div>

              {/* Temperature slider */}
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <label className="text-sm text-white/60">Track Temperature</label>
                  <span className="text-sm font-bold text-white font-mono">{trackTemp}°C</span>
                </div>
                <input
                  type="range" min={20} max={65} step={1} value={trackTemp}
                  onChange={(e) => setTrackTemp(Number(e.target.value))}
                  className="w-full h-1.5 rounded-full appearance-none cursor-pointer"
                  style={{
                    background: `linear-gradient(to right, #3b82f6 0%, #ef4444 ${((trackTemp - 20) / 45) * 100}%, rgba(255,255,255,0.1) ${((trackTemp - 20) / 45) * 100}%)`,
                  }}
                />
                <div className="flex justify-between text-[10px] text-white/25">
                  <span>20°C (Cool)</span>
                  <span>35°C (Std)</span>
                  <span>50°C (Hot)</span>
                  <span>65°C (Extreme)</span>
                </div>
                <p className="text-[10px] text-white/30 mt-1">
                  Higher temp → more Soft degradation → shorter stints → 2-stop more likely
                </p>
              </div>
            </div>

            {/* Compounds reference */}
            <div className="glass rounded-xl p-6">
              <h2 className="text-[11px] font-bold uppercase tracking-widest text-white/50 mb-3">
                Tyre Compounds
              </h2>
              <div className="space-y-2">
                {compounds.map((c) => {
                  const effDeg = c.degradationCoefficient + c.tempSensitivity * (trackTemp - 35) / 10;
                  return (
                    <div key={c.id} className="flex items-center gap-3 bg-white/[0.03] rounded-lg px-3 py-2.5">
                      <div className={`w-3 h-3 rounded-full flex-shrink-0 ${
                        c.name === "Soft" ? "bg-[var(--tyre-soft)]" :
                        c.name === "Medium" ? "bg-[var(--tyre-medium)]" : "bg-[var(--tyre-hard)]"
                      }`} />
                      <span className="text-sm font-semibold text-white flex-1">{c.name}</span>
                      <div className="text-right">
                        <div className="text-[10px] text-white/40">
                          {c.initialGrip >= 0 ? `+${c.initialGrip.toFixed(2)}` : c.initialGrip.toFixed(2)}s pace
                        </div>
                        <div className="text-[10px] text-white/40">
                          {effDeg.toFixed(3)}s/lap @ {trackTemp}°C
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>

            {/* Calculate button */}
            <button
              className="btn-f1 w-full text-center text-sm disabled:opacity-40 disabled:cursor-not-allowed"
              onClick={calculateStrategy}
              disabled={loading || !selectedCircuit}
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"/>
                  </svg>
                  Calculating…
                </span>
              ) : "Calculate Optimal Strategy"}
            </button>

            {error && (
              <div className="bg-red-500/10 border border-red-500/20 rounded-lg px-4 py-3 text-red-400 text-xs">
                {error}
              </div>
            )}
          </div>

          {/* ── Results panel ───────────────────────────── */}
          <div className="lg:col-span-8 space-y-6">

            {/* Empty state */}
            {strategies.length === 0 && !loading && (
              <div className="glass rounded-xl p-16 text-center">
                <div className="w-16 h-16 mx-auto mb-6 rounded-full bg-white/5 flex items-center justify-center">
                  <svg className="w-8 h-8 text-white/20" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 3v11.25A2.25 2.25 0 006 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0118 16.5h-2.25m-7.5 0h7.5m-7.5 0l-1 3m8.5-3l1 3m0 0l.5 1.5m-.5-1.5h-9.5m0 0l-.5 1.5" />
                  </svg>
                </div>
                <h3 className="text-base font-bold text-white/50 mb-2">No Strategy Calculated</h3>
                <p className="text-sm text-white/25 max-w-xs mx-auto">
                  Adjust temperature, then click &quot;Calculate Optimal Strategy&quot;
                </p>
              </div>
            )}

            {/* Loading */}
            {loading && (
              <div className="glass rounded-xl p-16 text-center">
                <div className="w-16 h-16 mx-auto mb-6 rounded-full bg-[var(--f1-red)]/10 flex items-center justify-center">
                  <svg className="w-8 h-8 text-[var(--f1-red)] animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"/>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
                  </svg>
                </div>
                <h3 className="text-base font-bold text-white/50 mb-1">Evaluating strategies…</h3>
                <p className="text-xs text-white/25">Testing all compound combinations at {trackTemp}°C</p>
              </div>
            )}

            {strategies.length > 0 && (
              <>
                {/* Summary banner */}
                <div className="glass rounded-xl px-5 py-4 flex flex-wrap items-center gap-4">
                  <div className="flex-1">
                    <p className="text-xs text-white/40 uppercase tracking-widest mb-0.5">Optimal Strategy</p>
                    <p className="text-sm font-bold text-white">
                      {strategies[0].strategyType} · {strategies[0].stints.map(s => s.compoundName).join(" → ")}
                    </p>
                  </div>
                  <div className="flex gap-6">
                    <div className="text-center">
                      <div className="text-[10px] text-white/30 uppercase tracking-wider">Strategies</div>
                      <div className="text-lg font-black text-white">{strategies.length}</div>
                    </div>
                    <div className="text-center">
                      <div className="text-[10px] text-white/30 uppercase tracking-wider">Track Temp</div>
                      <div className="text-lg font-black text-white">{trackTemp}°C</div>
                    </div>
                    <div className="text-center">
                      <div className="text-[10px] text-white/30 uppercase tracking-wider">Stop Types</div>
                      <div className="text-lg font-black text-white">{stopTypes.length}</div>
                    </div>
                  </div>
                </div>

                {/* All strategy cards */}
                <div>
                  <h2 className="text-[11px] font-bold uppercase tracking-widest text-white/50 mb-3 flex items-center gap-2">
                    <div className="w-1 h-4 bg-[var(--f1-red)] rounded-full" />
                    Full Strategy Ranking · All Compound Combinations
                  </h2>
                  <StrategyComparison
                    strategies={strategies}
                    totalLaps={selectedCircuit?.laps || 57}
                  />
                </div>

                {/* Lap time chart */}
                <div className="glass rounded-xl p-6">
                  <div className="flex flex-wrap items-center justify-between gap-3 mb-5">
                    <h2 className="text-[11px] font-bold uppercase tracking-widest text-white/50 flex items-center gap-2">
                      <div className="w-1 h-4 bg-[var(--f1-red)] rounded-full" />
                      Predicted Lap Times
                    </h2>
                    {/* Strategy picker */}
                    <div className="flex flex-wrap gap-1 bg-white/5 rounded-lg p-1">
                      {strategies.map((s, idx) => (
                        <button
                          key={idx}
                          onClick={() => setSelectedStrategyIdx(idx)}
                          className={`px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider rounded-md transition-all ${
                            selectedStrategyIdx === idx
                              ? "bg-[var(--f1-red)] text-white"
                              : "text-white/30 hover:text-white/60"
                          }`}
                        >
                          #{idx + 1} {s.strategyType}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="w-full h-[320px]">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart data={chartData} margin={{ top: 5, right: 20, left: 5, bottom: 10 }}>
                        <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.04)" vertical={false}/>
                        <XAxis dataKey="lap" stroke="rgba(255,255,255,0.15)" fontSize={11}
                          tickLine={false} axisLine={false}
                          label={{ value: "Lap", position: "insideBottom", offset: -5, fill: "rgba(255,255,255,0.25)", fontSize: 11 }}
                        />
                        <YAxis stroke="rgba(255,255,255,0.15)" fontSize={11}
                          tickLine={false} axisLine={false} domain={["auto", "auto"]}
                          label={{ value: "Time (s)", angle: -90, position: "insideLeft", fill: "rgba(255,255,255,0.25)", fontSize: 11 }}
                        />
                        <Tooltip
                          contentStyle={{ backgroundColor: "rgba(10,10,10,0.95)", border: "1px solid rgba(255,255,255,0.08)", borderRadius: "8px", color: "#fff", fontSize: "12px" }}
                          formatter={(v: number) => [`${v.toFixed(3)}s`, "Lap Time"]}
                          labelFormatter={(l) => `Lap ${l}`}
                        />
                        <Legend wrapperStyle={{ paddingTop: "10px", fontSize: "12px" }}/>
                        {uniqueCompounds.map((comp) => (
                          <Line key={comp} type="monotone" dataKey={comp}
                            stroke={compoundChartColors[comp] || "#666"}
                            name={comp} dot={false} strokeWidth={2.5} connectNulls={false}
                          />
                        ))}
                      </LineChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

function InfoTile({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-white/[0.03] rounded-lg px-3 py-2.5">
      <div className="text-[10px] uppercase tracking-widest text-white/30 mb-1">{label}</div>
      <div className="text-sm font-bold text-white">{value}</div>
    </div>
  );
}
