"use client";

import React from "react";
import { StintBar } from "./StintBar";
import { formatF1Time, formatF1Delta } from "@/utils/format";

interface StintData {
  compoundName: string;
  startLap: number;
  endLap: number;
  lapsDuration: number;
  stintTime: number;
}

interface StrategyData {
  strategyType: string;
  stints: StintData[];
  pitStopLaps: number[];
  totalTime: number;
  deltaToOptimal: number;
}

interface StrategyComparisonProps {
  strategies: StrategyData[];
  totalLaps: number;
}

export function StrategyComparison({ strategies, totalLaps }: StrategyComparisonProps) {
  if (!strategies || strategies.length === 0) return null;

  return (
    <div className="space-y-4">
      {strategies.map((strategy, idx) => {
        const isOptimal = strategy.deltaToOptimal === 0;

        return (
          <div
            key={idx}
            className={`strategy-card ${isOptimal ? "optimal" : ""}`}
          >
            {/* Header row */}
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-3">
                {/* Rank badge */}
                <div
                  className={`w-8 h-8 rounded-md flex items-center justify-center text-sm font-black ${
                    isOptimal
                      ? "bg-[var(--f1-red)] text-white"
                      : "bg-white/5 text-white/50"
                  }`}
                >
                  {idx + 1}
                </div>
                <div>
                  <h3 className="text-base font-bold text-white uppercase tracking-wider">
                    {strategy.strategyType}
                  </h3>
                  <p className="text-[11px] text-white/40">
                    {strategy.stints.map((s) => s.compoundName).join(" → ")}
                  </p>
                </div>
              </div>

              <div className="text-right">
                <div className="text-lg font-mono font-bold text-white">
                  {formatF1Time(strategy.totalTime)}
                </div>
                <div
                  className={`text-xs font-bold uppercase tracking-wider ${
                    isOptimal ? "text-[var(--f1-red)]" : "text-white/30"
                  }`}
                >
                  {formatF1Delta(strategy.deltaToOptimal)}
                </div>
              </div>
            </div>

            {/* Stint bar */}
            <StintBar
              stints={strategy.stints}
              totalLaps={totalLaps}
              pitStopLaps={strategy.pitStopLaps}
            />

            {/* Stint details table */}
            <div className="mt-4 grid grid-cols-1 sm:grid-cols-3 gap-2">
              {strategy.stints.map((stint, sIdx) => (
                <div
                  key={sIdx}
                  className="flex items-center gap-2 bg-white/[0.03] rounded-md px-3 py-2"
                >
                  <div
                    className={`w-2 h-2 rounded-full ${
                      {
                        Soft: "bg-[var(--tyre-soft)]",
                        Medium: "bg-[var(--tyre-medium)]",
                        Hard: "bg-[var(--tyre-hard)]",
                        Intermediate: "bg-[#43b02a]",
                        Wet: "bg-[#0067ff]",
                      }[stint.compoundName] || "bg-white/30"
                    }`}
                  />
                  <div className="flex-1">
                    <span className="text-xs font-semibold text-white/70">
                      {stint.compoundName}
                    </span>
                    <span className="text-[10px] text-white/30 ml-2">
                      {stint.lapsDuration} laps
                    </span>
                  </div>
                  <span className="text-xs font-mono text-white/40">
                    {formatF1Time(stint.stintTime)}
                  </span>
                </div>
              ))}
            </div>
          </div>
        );
      })}
    </div>
  );
}
