"use client";

import React from "react";

interface StintData {
  compoundName: string;
  startLap: number;
  endLap: number;
  lapsDuration: number;
  stintTime: number;
}

interface StintBarProps {
  stints: StintData[];
  totalLaps: number;
  pitStopLaps: number[];
}

const compoundColors: Record<string, { bg: string; text: string; border: string }> = {
  Soft: { bg: "linear-gradient(135deg, #ff1801, #cc1400)", text: "#fff", border: "#ff1801" },
  Medium: { bg: "linear-gradient(135deg, #ffc906, #dba800)", text: "#1a1a1a", border: "#ffc906" },
  Hard: { bg: "linear-gradient(135deg, #f0f0f0, #d0d0d0)", text: "#1a1a1a", border: "#f0f0f0" },
  Intermediate: { bg: "linear-gradient(135deg, #43b02a, #2d7a1c)", text: "#fff", border: "#43b02a" },
  Wet: { bg: "linear-gradient(135deg, #0067ff, #004cbf)", text: "#fff", border: "#0067ff" },
};

export function StintBar({ stints, totalLaps, pitStopLaps }: StintBarProps) {
  return (
    <div className="w-full">
      {/* Stint bar */}
      <div className="relative flex h-12 rounded-lg overflow-hidden border border-white/10">
        {stints.map((stint, idx) => {
          const widthPercent = (stint.lapsDuration / totalLaps) * 100;
          const colors = compoundColors[stint.compoundName] || compoundColors.Hard;

          return (
            <div
              key={idx}
              className="relative flex items-center justify-center transition-all duration-500"
              style={{
                width: `${widthPercent}%`,
                background: colors.bg,
                borderRight: idx < stints.length - 1 ? "2px solid rgba(0,0,0,0.3)" : "none",
              }}
            >
              <div className="flex flex-col items-center" style={{ color: colors.text }}>
                <span className="text-xs font-bold uppercase tracking-wider">
                  {stint.compoundName}
                </span>
                <span className="text-[10px] opacity-70">
                  L{stint.startLap}–L{stint.endLap}
                </span>
              </div>
            </div>
          );
        })}
      </div>

      {/* Pit stop markers */}
      <div className="relative h-6 mt-1">
        {pitStopLaps.map((lap, idx) => {
          const leftPercent = (lap / totalLaps) * 100;
          return (
            <div
              key={idx}
              className="absolute flex flex-col items-center"
              style={{ left: `${leftPercent}%`, transform: "translateX(-50%)" }}
            >
              <div className="w-[2px] h-3 bg-[var(--f1-red)]/60" />
              <span className="text-[9px] text-white/40 font-medium mt-0.5">
                PIT L{lap}
              </span>
            </div>
          );
        })}
      </div>
    </div>
  );
}
