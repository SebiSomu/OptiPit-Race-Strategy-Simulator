"use client";

import React, { useState, useEffect, useCallback } from "react";
import { Navbar } from "@/components/Navbar";
import { StrategyComparison } from "@/components/StrategyComparison";
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";

const API_BASE = "http://localhost:8080/api";

interface Circuit {
  id: number;
  name: string;
  laps: number;
  pitStopLoss: number;
  baseLapTime: number;
}

interface Compound {
  id: number;
  name: string;
  degradationCoefficient: number;
  initialGrip: number;
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

export default function SimulationPage() {
  const [circuits, setCircuits] = useState<Circuit[]>([]);
  const [compounds, setCompounds] = useState<Compound[]>([]);
  const [selectedCircuit, setSelectedCircuit] = useState<Circuit | null>(null);
  const [strategies, setStrategies] = useState<StrategyData[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [selectedStrategyIdx, setSelectedStrategyIdx] = useState(0);

  // Fetch circuits and compounds on mount
  useEffect(() => {
    Promise.all([
      fetch(`${API_BASE}/circuits`).then((r) => r.json()),
      fetch(`${API_BASE}/compounds`).then((r) => r.json()),
    ])
      .then(([circs, comps]) => {
        setCircuits(circs);
        setCompounds(comps);
        if (circs.length > 0) setSelectedCircuit(circs[0]);
      })
      .catch((err) => setError("Failed to connect to backend. Make sure the server is running on port 8080."));
  }, []);

  const calculateStrategy = useCallback(async () => {
    if (!selectedCircuit) return;
    setLoading(true);
    setError(null);
    setStrategies([]);
    setSelectedStrategyIdx(0);

    try {
      const response = await fetch(
        `${API_BASE}/strategy/optimal?circuitId=${selectedCircuit.id}`
      );
      if (!response.ok) throw new Error("Strategy calculation failed");
      const data: StrategyData[] = await response.json();
      setStrategies(data);
    } catch (err) {
      setError("Failed to calculate strategies. Check if the backend is running.");
    } finally {
      setLoading(false);
    }
  }, [selectedCircuit]);

  // Build lap time chart data for selected strategy
  const chartData = strategies[selectedStrategyIdx]?.lapTimes || [];

  // Group lap times by compound for multi-line chart
  const uniqueCompounds = Array.from(new Set(chartData.map((d) => d.compound)));

  // Transform data: each lap has lap number + a field for each compound's time
  const transformedChartData = chartData.map((entry) => {
    const point: Record<string, number | null> = { lap: entry.lap };
    for (const comp of uniqueCompounds) {
      point[comp] = entry.compound === comp ? entry.lapTime : null;
    }
    return point;
  });

  return (
    <div className="min-h-screen bg-[var(--f1-dark)]">
      <Navbar />

      {/* Page Header */}
      <div className="pt-28 pb-8 px-6 max-w-7xl mx-auto">
        <div className="flex items-center gap-3 mb-2">
          <div className="w-1 h-8 bg-[var(--f1-red)] rounded-full" />
          <h1 className="text-3xl md:text-4xl font-black uppercase tracking-tight text-white">
            Strategy Simulator
          </h1>
        </div>
        <p className="text-white/40 text-sm ml-4 pl-1">
          Calculate optimal pit stop strategies for any circuit configuration
        </p>
      </div>

      <div className="max-w-7xl mx-auto px-6 pb-16">
        <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
          {/* ─── Control Panel ─────────────────────── */}
          <div className="lg:col-span-4 space-y-6">
            {/* Circuit Info Card */}
            <div className="glass rounded-xl p-6 racing-stripe">
              <h2 className="text-sm font-bold uppercase tracking-widest text-white/60 mb-4">
                Circuit
              </h2>

              {/* Circuit Selector */}
              <select
                className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-3 text-white text-sm font-medium focus:outline-none focus:border-[var(--f1-red)]/40 transition-colors cursor-pointer appearance-none"
                value={selectedCircuit?.id || ""}
                onChange={(e) => {
                  const c = circuits.find((c) => c.id === Number(e.target.value));
                  if (c) setSelectedCircuit(c);
                }}
              >
                {circuits.map((circuit) => (
                  <option key={circuit.id} value={circuit.id} className="bg-[#111]">
                    {circuit.name}
                  </option>
                ))}
              </select>

              {selectedCircuit && (
                <div className="mt-5 grid grid-cols-2 gap-3">
                  <InfoTile label="Race Laps" value={`${selectedCircuit.laps}`} />
                  <InfoTile label="Base Lap Time" value={`${selectedCircuit.baseLapTime}s`} />
                  <InfoTile label="Pit Stop Loss" value={`${selectedCircuit.pitStopLoss}s`} />
                  <InfoTile label="Track Length" value="5.412 km" />
                </div>
              )}
            </div>

            {/* Compounds Info */}
            <div className="glass rounded-xl p-6">
              <h2 className="text-sm font-bold uppercase tracking-widest text-white/60 mb-4">
                Available Compounds
              </h2>
              <div className="space-y-2">
                {compounds.map((compound) => (
                  <div
                    key={compound.id}
                    className="flex items-center gap-3 bg-white/[0.03] rounded-lg px-4 py-3"
                  >
                    <div
                      className={`w-4 h-4 rounded-full ${
                        compound.name === "Soft"
                          ? "bg-[var(--tyre-soft)]"
                          : compound.name === "Medium"
                          ? "bg-[var(--tyre-medium)]"
                          : "bg-[var(--tyre-hard)]"
                      }`}
                    />
                    <div className="flex-1">
                      <span className="text-sm font-semibold text-white">
                        {compound.name}
                      </span>
                    </div>
                    <div className="text-right">
                      <div className="text-[10px] text-white/30 uppercase tracking-wider">
                        Grip {compound.initialGrip.toFixed(2)}
                      </div>
                      <div className="text-[10px] text-white/30 uppercase tracking-wider">
                        Deg {compound.degradationCoefficient}
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Calculate Button */}
            <button
              className="btn-f1 w-full text-center text-sm disabled:opacity-50 disabled:cursor-not-allowed"
              onClick={calculateStrategy}
              disabled={loading || !selectedCircuit}
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="animate-spin w-4 h-4" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  Calculating...
                </span>
              ) : (
                "Calculate Optimal Strategy"
              )}
            </button>

            {error && (
              <div className="bg-red-500/10 border border-red-500/20 rounded-lg px-4 py-3 text-red-400 text-sm">
                {error}
              </div>
            )}
          </div>

          {/* ─── Results Panel ─────────────────────── */}
          <div className="lg:col-span-8 space-y-6">
            {strategies.length === 0 && !loading && (
              <div className="glass rounded-xl p-16 text-center">
                <div className="w-16 h-16 mx-auto mb-6 rounded-full bg-white/5 flex items-center justify-center">
                  <svg className="w-8 h-8 text-white/20" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1}>
                    <path strokeLinecap="round" strokeLinejoin="round" d="M3.75 3v11.25A2.25 2.25 0 006 16.5h2.25M3.75 3h-1.5m1.5 0h16.5m0 0h1.5m-1.5 0v11.25A2.25 2.25 0 0118 16.5h-2.25m-7.5 0h7.5m-7.5 0l-1 3m8.5-3l1 3m0 0l.5 1.5m-.5-1.5h-9.5m0 0l-.5 1.5m.75-9l3-3 2.148 2.148A12.061 12.061 0 0116.5 7.605" />
                  </svg>
                </div>
                <h3 className="text-lg font-bold text-white/60 mb-2">No Strategy Calculated</h3>
                <p className="text-sm text-white/30">
                  Select a circuit and click &quot;Calculate Optimal Strategy&quot; to see the results.
                </p>
              </div>
            )}

            {loading && (
              <div className="glass rounded-xl p-16 text-center">
                <div className="w-16 h-16 mx-auto mb-6 rounded-full bg-[var(--f1-red)]/10 flex items-center justify-center animate-pulse">
                  <svg className="w-8 h-8 text-[var(--f1-red)] animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                </div>
                <h3 className="text-lg font-bold text-white/60 mb-2">Calculating Strategies...</h3>
                <p className="text-sm text-white/30">
                  Evaluating thousands of compound and pit stop combinations.
                </p>
              </div>
            )}

            {strategies.length > 0 && (
              <>
                {/* Strategy Cards */}
                <div>
                  <h2 className="text-sm font-bold uppercase tracking-widest text-white/60 mb-4 flex items-center gap-2">
                    <div className="w-1 h-4 bg-[var(--f1-red)] rounded-full" />
                    Strategy Ranking
                  </h2>
                  <StrategyComparison
                    strategies={strategies}
                    totalLaps={selectedCircuit?.laps || 57}
                  />
                </div>

                {/* Lap Time Chart */}
                <div className="glass rounded-xl p-6">
                  <div className="flex items-center justify-between mb-4">
                    <h2 className="text-sm font-bold uppercase tracking-widest text-white/60 flex items-center gap-2">
                      <div className="w-1 h-4 bg-[var(--f1-red)] rounded-full" />
                      Predicted Lap Times
                    </h2>

                    {/* Strategy selector tabs */}
                    <div className="flex gap-1 bg-white/5 rounded-lg p-1">
                      {strategies.map((s, idx) => (
                        <button
                          key={idx}
                          onClick={() => setSelectedStrategyIdx(idx)}
                          className={`px-3 py-1.5 text-[11px] font-bold uppercase tracking-wider rounded-md transition-all ${
                            selectedStrategyIdx === idx
                              ? "bg-[var(--f1-red)] text-white"
                              : "text-white/40 hover:text-white/70"
                          }`}
                        >
                          {s.strategyType}
                        </button>
                      ))}
                    </div>
                  </div>

                  <div className="w-full h-[350px]">
                    <ResponsiveContainer width="100%" height="100%">
                      <LineChart
                        data={transformedChartData}
                        margin={{ top: 10, right: 30, left: 10, bottom: 10 }}
                      >
                        <CartesianGrid
                          strokeDasharray="3 3"
                          stroke="rgba(255,255,255,0.05)"
                          vertical={false}
                        />
                        <XAxis
                          dataKey="lap"
                          stroke="rgba(255,255,255,0.2)"
                          fontSize={11}
                          tickLine={false}
                          axisLine={false}
                          label={{
                            value: "Lap",
                            position: "insideBottom",
                            offset: -5,
                            fill: "rgba(255,255,255,0.3)",
                            fontSize: 11,
                          }}
                        />
                        <YAxis
                          stroke="rgba(255,255,255,0.2)"
                          fontSize={11}
                          tickLine={false}
                          axisLine={false}
                          domain={["auto", "auto"]}
                          label={{
                            value: "Time (s)",
                            angle: -90,
                            position: "insideLeft",
                            fill: "rgba(255,255,255,0.3)",
                            fontSize: 11,
                          }}
                        />
                        <Tooltip
                          contentStyle={{
                            backgroundColor: "rgba(17,17,17,0.95)",
                            border: "1px solid rgba(255,255,255,0.1)",
                            borderRadius: "8px",
                            color: "#fff",
                            fontSize: "12px",
                          }}
                          formatter={(value: number) => [
                            `${value.toFixed(3)}s`,
                            "Lap Time",
                          ]}
                          labelFormatter={(label) => `Lap ${label}`}
                        />
                        <Legend
                          wrapperStyle={{ paddingTop: "12px", fontSize: "12px" }}
                        />
                        {uniqueCompounds.map((comp) => (
                          <Line
                            key={comp}
                            type="monotone"
                            dataKey={comp}
                            stroke={compoundChartColors[comp] || "#666"}
                            name={comp}
                            dot={false}
                            strokeWidth={2.5}
                            connectNulls={false}
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
      <div className="text-[10px] uppercase tracking-widest text-white/30 mb-1">
        {label}
      </div>
      <div className="text-sm font-bold text-white">{value}</div>
    </div>
  );
}
