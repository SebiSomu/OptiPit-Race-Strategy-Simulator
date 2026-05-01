"use client";

import React, { useState, useEffect, useCallback } from "react";
import { StrategyComparison } from "@/components/StrategyComparison";
import { useRouter } from "next/navigation";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid,
  Tooltip, Legend, ResponsiveContainer,
} from "recharts";
import { formatF1Time } from "@/utils/format";

const API_BASE = "http://localhost:8080/api";

interface Circuit {
  id: number;
  name: string;
  slug: string;
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
  Intermediate: "#43b02a",
  Wet: "#0067ff",
};

function tempLabel(t: number) {
  if (t < 30) return "Cool";
  if (t < 40) return "Mild";
  if (t < 50) return "Hot";
  return "Extreme";
}

export function SimulationDashboard({ initialSlug }: { initialSlug?: string }) {
  const router = useRouter();

  const [circuits, setCircuits] = useState<Circuit[]>([]);
  const [compounds, setCompounds] = useState<Compound[]>([]);
  const [selectedCircuit, setSelectedCircuit] = useState<Circuit | null>(null);
  const [trackTemp, setTrackTemp] = useState<number>(50);
  const [windSpeed, setWindSpeed] = useState<number>(0);
  const [windAngle, setWindAngle] = useState<number>(0);
  const [airTemp, setAirTemp] = useState<number>(25);
  const [rainIntensity, setRainIntensity] = useState<number>(0);
  const [strategies, setStrategies] = useState<StrategyData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedStrategyIdx, setSelectedStrategyIdx] = useState(0);

  // 1. Fetch initial data once
  useEffect(() => {
    Promise.all([
      fetch(`${API_BASE}/circuits`).then((r) => r.json()),
      fetch(`${API_BASE}/compounds`).then((r) => r.json()),
    ])
      .then(([circs, comps]) => {
        setCircuits(circs);
        setCompounds(comps);
      })
      .catch(() => setError("Backend connection error. Please restart the Spring Boot server."));
  }, []);

  // 2. Sync selection with initialSlug whenever circuits or slug change
  useEffect(() => {
    if (circuits.length > 0) {
      const found = circuits.find(c => c.slug === initialSlug);
      if (found) {
        setSelectedCircuit(found);
        setTrackTemp(found.trackTempNominal ?? 50);
      } else if (!initialSlug) {
        setSelectedCircuit(null);
      }
    }
  }, [initialSlug, circuits]);

  const calculateStrategy = useCallback(async () => {
    if (!selectedCircuit) return;
    setLoading(true);
    setError(null);
    setStrategies([]);
    setSelectedStrategyIdx(0);
    try {
      const params = new URLSearchParams({
        circuitId: String(selectedCircuit.id),
        trackTemp: String(trackTemp),
        windSpeed: String(windSpeed),
        windAngle: String(windAngle),
        airTemp: String(airTemp),
        rainIntensity: String(rainIntensity),
      });
      const res = await fetch(`${API_BASE}/strategy/optimal?${params}`);
      if (!res.ok) throw new Error();
      setStrategies(await res.json());
    } catch {
      setError("Strategy calculation failed. Check backend logs.");
    } finally {
      setLoading(false);
    }
  }, [selectedCircuit, trackTemp, windSpeed, windAngle, airTemp, rainIntensity]);

  const handleCircuitChange = (newId: string) => {
    const c = circuits.find((c) => c.id === Number(newId));
    if (c) {
      setSelectedCircuit(c);
      setTrackTemp(c.trackTempNominal ?? 50);
      setStrategies([]);
      // Ensure we have a valid slug before pushing
      if (c.slug) {
        router.push(`/simulation/${c.slug}`);
      } else {
        router.push(`/simulation`);
      }
    }
  };

  const chartLapTimes = strategies[selectedStrategyIdx]?.lapTimes || [];
  const uniqueCompounds = Array.from(new Set(chartLapTimes.map((d) => d.compound)));
  const chartData = chartLapTimes.map((entry) => {
    const pt: Record<string, number | null> = { lap: entry.lap };
    uniqueCompounds.forEach((c) => { pt[c] = entry.compound === c ? entry.lapTime : null; });
    return pt;
  });

  return (
    <div className="max-w-7xl mx-auto px-6 pb-16">
      <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
        
        {/* Left Column: Mission Parameters */}
        <div className="lg:col-span-4 space-y-5">
          
          {/* Circuit Selector Card */}
          <div className="glass rounded-xl p-6 racing-stripe relative overflow-hidden group">
            <div className="absolute top-0 right-0 w-32 h-32 bg-[var(--f1-red)]/5 blur-3xl rounded-full -mr-16 -mt-16" />
            <h2 className="text-[11px] font-black uppercase tracking-[0.2em] text-[var(--f1-red)] mb-4 flex items-center gap-2">
              <span className="w-1.5 h-1.5 rounded-full bg-[var(--f1-red)] animate-pulse" />
              Circuit Selection
            </h2>
            <select
              className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-3.5 text-white text-sm font-bold focus:outline-none focus:ring-2 focus:ring-[var(--f1-red)]/20 focus:border-[var(--f1-red)]/40 transition-all cursor-pointer appearance-none"
              value={selectedCircuit?.id || ""}
              onChange={(e) => handleCircuitChange(e.target.value)}
            >
              <option value="" disabled className="bg-[#111]">-- Choose a Grand Prix --</option>
              {circuits.map((c) => (
                <option key={c.id} value={c.id} className="bg-[#111]">{c.name}</option>
              ))}
            </select>

            {selectedCircuit && (
              <div className="mt-5 grid grid-cols-2 gap-2 animate-slide-up">
                <InfoTile label="Total Laps" value={`${selectedCircuit.laps}`} />
                <InfoTile label="Pit Loss" value={formatF1Time(selectedCircuit.pitStopLoss)} />
                <InfoTile label="Base Pace" value={formatF1Time(selectedCircuit.baseLapTime)} />
                <InfoTile label="Nominal" value={`${selectedCircuit.trackTempNominal}°C`} />
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

            <div className="space-y-4">
              {/* Track Temperature */}
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <label className="text-xs text-white/50">Track Temperature</label>
                  <span className="text-xs font-bold text-white font-mono">{trackTemp}°C</span>
                </div>
                <input
                  type="range" min={20} max={65} step={1} value={trackTemp}
                  onChange={(e) => setTrackTemp(Number(e.target.value))}
                  className="w-full h-1.5 rounded-full appearance-none cursor-pointer"
                  style={{
                    background: `linear-gradient(to right, #3b82f6 0%, #ef4444 ${((trackTemp - 20) / 45) * 100}%, rgba(255,255,255,0.1) ${((trackTemp - 20) / 45) * 100}%)`,
                  }}
                />
              </div>

              {/* Air Temperature */}
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <label className="text-xs text-white/50">Air Temperature</label>
                  <span className="text-xs font-bold text-white font-mono">{airTemp}°C</span>
                </div>
                <input
                  type="range" min={10} max={45} step={1} value={airTemp}
                  onChange={(e) => setAirTemp(Number(e.target.value))}
                  className="w-full h-1.5 rounded-full appearance-none cursor-pointer"
                  style={{
                    background: `linear-gradient(to right, #60a5fa 0%, #f97316 ${((airTemp - 10) / 35) * 100}%, rgba(255,255,255,0.1) ${((airTemp - 10) / 35) * 100}%)`,
                  }}
                />
              </div>

              <div className="border-t border-white/5 pt-3" />

              {/* Wind Speed */}
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <label className="text-xs text-white/50">Wind Speed</label>
                  <span className="text-xs font-bold text-white font-mono">{windSpeed} km/h</span>
                </div>
                <input
                  type="range" min={0} max={60} step={1} value={windSpeed}
                  onChange={(e) => setWindSpeed(Number(e.target.value))}
                  className="w-full h-1.5 rounded-full appearance-none cursor-pointer"
                  style={{
                    background: `linear-gradient(to right, #22c55e 0%, #eab308 ${(windSpeed / 60) * 100}%, rgba(255,255,255,0.1) ${(windSpeed / 60) * 100}%)`,
                  }}
                />
              </div>

              {/* Wind Direction */}
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <label className="text-xs text-white/50">Wind Direction</label>
                  <span className="text-xs font-bold text-white font-mono">
                    {windAngle === 0 ? "Headwind" : windAngle === 180 ? "Tailwind" : windAngle < 90 ? "Head-Cross" : "Tail-Cross"}
                  </span>
                </div>
                <input
                  type="range" min={0} max={180} step={15} value={windAngle}
                  onChange={(e) => setWindAngle(Number(e.target.value))}
                  className="w-full h-1.5 rounded-full appearance-none cursor-pointer bg-white/10"
                />
                <div className="flex justify-between text-[9px] text-white/20">
                  <span>Headwind (0°)</span>
                  <span>Crosswind (90°)</span>
                  <span>Tailwind (180°)</span>
                </div>
              </div>

              <div className="border-t border-white/5 pt-3" />

              {/* Rain Intensity */}
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <label className="text-xs text-white/50">Rain Intensity</label>
                  <span className={`text-xs font-bold font-mono ${
                    rainIntensity === 0 ? "text-green-400" :
                    rainIntensity <= 0.3 ? "text-blue-300" :
                    rainIntensity <= 0.6 ? "text-blue-400" :
                    "text-blue-500"
                  }`}>
                    {rainIntensity === 0 ? "DRY" : rainIntensity <= 0.3 ? "LIGHT" : rainIntensity <= 0.6 ? "MODERATE" : "HEAVY"}
                  </span>
                </div>
                <input
                  type="range" min={0} max={1} step={0.05} value={rainIntensity}
                  onChange={(e) => setRainIntensity(Number(e.target.value))}
                  className="w-full h-1.5 rounded-full appearance-none cursor-pointer"
                  style={{
                    background: `linear-gradient(to right, #22c55e 0%, #3b82f6 ${rainIntensity * 100}%, rgba(255,255,255,0.1) ${rainIntensity * 100}%)`,
                  }}
                />
                <p className="text-[9px] text-white/20">
                  Rain increases tyre degradation and reduces grip significantly
                </p>
              </div>
            </div>
          </div>

          {/* Action Trigger */}
          <button
            className="btn-f1 w-full text-center text-sm font-black uppercase tracking-widest py-4 disabled:opacity-40 disabled:grayscale transition-all duration-500 hover:scale-[1.02] active:scale-95 shadow-xl shadow-[var(--f1-red)]/10"
            onClick={calculateStrategy}
            disabled={loading || !selectedCircuit}
          >
            {loading ? (
              <span className="flex items-center justify-center gap-3">
                <svg className="animate-spin h-4 w-4" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" fill="none" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
                Processing...
              </span>
            ) : "Initialize Optimization"}
          </button>

          {error && (
            <div className="bg-red-500/5 border border-red-500/20 rounded-lg p-4 text-red-400 text-[11px] font-bold uppercase tracking-tight animate-pulse text-center">
              ⚠️ {error}
            </div>
          )}
        </div>

        {/* Right Column: Telemetry & Analysis */}
        <div className="lg:col-span-8 space-y-6">
          {!selectedCircuit ? (
            <div className="glass rounded-xl p-20 text-center border-dashed border-white/5">
              <div className="w-20 h-20 mx-auto mb-6 rounded-full bg-white/5 flex items-center justify-center border border-white/10">
                <svg className="w-10 h-10 text-white/10" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              </div>
              <h3 className="text-xl font-black text-white/30 uppercase tracking-widest">Awaiting Uplink</h3>
              <p className="text-sm text-white/10 max-w-xs mx-auto mt-2">Select a Grand Prix venue to initialize the strategy engine</p>
            </div>
          ) : strategies.length === 0 && !loading ? (
            <div className="glass rounded-xl p-20 text-center relative overflow-hidden">
               <div className="absolute inset-0 bg-gradient-to-br from-[var(--f1-red)]/5 to-transparent pointer-events-none" />
               <h3 className="text-xl font-black text-white uppercase tracking-widest relative z-10">
                  Ready for <span className="text-[var(--f1-red)]">{selectedCircuit.name}</span>
               </h3>
               <p className="text-sm text-white/40 mt-3 relative z-10">
                  Telemetry connection established. Ready to optimize {selectedCircuit.laps} laps.
               </p>
            </div>
          ) : null}

          {strategies.length > 0 && (
            <div className="animate-fade-in space-y-6">
              <div className="flex items-center gap-3 mb-2">
                <div className="w-2 h-2 rounded-full bg-[var(--f1-red)]" />
                <h2 className="text-xs font-black uppercase tracking-[0.3em] text-white/60">Optimized Strategy Sets</h2>
              </div>
              <StrategyComparison strategies={strategies} totalLaps={selectedCircuit?.laps || 0} selectedIdx={selectedStrategyIdx} onSelectStrategy={setSelectedStrategyIdx} />
              
              <div className="glass rounded-xl p-6 relative overflow-hidden">
                 <div className="flex items-center justify-between mb-6">
                    <div className="flex items-center gap-4">
                      <h2 className="text-[11px] font-black uppercase tracking-widest text-white/40">Pace Projection · {strategies[selectedStrategyIdx]?.strategyType}</h2>
                      {/* Strategy Selector */}
                      <div className="flex items-center gap-1 bg-white/5 rounded-lg p-1">
                        {strategies.map((strategy, idx) => (
                          <button
                            key={idx}
                            onClick={() => setSelectedStrategyIdx(idx)}
                            className={`px-2 py-1 text-[10px] font-bold rounded-md transition-all ${
                              selectedStrategyIdx === idx
                                ? "bg-[var(--f1-red)] text-white"
                                : "text-white/40 hover:text-white/60"
                            }`}
                            title={`${strategy.strategyType} - ${formatF1Time(strategy.totalTime)}`}
                          >
                            {idx + 1}
                          </button>
                        ))}
                      </div>
                    </div>
                    <div className="flex gap-4">
                       {uniqueCompounds.map(c => (
                         <div key={c} className="flex items-center gap-1.5">
                            <div className="w-2 h-2 rounded-full" style={{ backgroundColor: compoundChartColors[c] }} />
                            <span className="text-[10px] font-bold text-white/30">{c}</span>
                         </div>
                       ))}
                    </div>
                 </div>
                 <div className="w-full h-[320px]">
                  <ResponsiveContainer width="100%" height="100%">
                    <LineChart data={chartData}>
                      <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.03)" vertical={false}/>
                      <XAxis dataKey="lap" stroke="rgba(255,255,255,0.1)" fontSize={10} tickLine={false} axisLine={false} />
                      <YAxis stroke="rgba(255,255,255,0.1)" fontSize={10} domain={["auto", "auto"]} tickLine={false} axisLine={false} />
                      <Tooltip 
                        contentStyle={{ backgroundColor: "rgba(10,10,10,0.9)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "12px", backdropFilter: "blur(10px)" }} 
                        itemStyle={{ color: "#fff", fontSize: "11px", fontWeight: "bold" }}
                        formatter={(value: number) => [formatF1Time(value), "Lap Time"]}
                      />
                      {uniqueCompounds.map((comp) => (
                        <Line key={comp} type="monotone" dataKey={comp} stroke={compoundChartColors[comp]} dot={false} strokeWidth={3} animationDuration={1500} />
                      ))}
                    </LineChart>
                  </ResponsiveContainer>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function InfoTile({ label, value }: { label: string; value: string }) {
  return (
    <div className="bg-white/[0.03] border border-white/[0.05] rounded-xl px-4 py-3.5 hover:bg-white/[0.06] transition-colors group">
      <div className="flex items-center justify-between mb-1.5">
        <div className="text-[10px] font-black uppercase tracking-widest text-white/20">{label}</div>
      </div>
      <div className="text-base font-black text-white italic tracking-tight">{value}</div>
    </div>
  );
}
