"use client";

import React, { useState, useEffect, useRef } from "react";
import { Button } from "@/components/ui/button";
import { CIRCUITS, CircuitData } from "@/lib/circuitPaths";

interface CircuitLayoutProps {
  circuitId: string;
  highlightColor?: string;
  animateCar?: boolean;
  variant?: "default" | "minimal";
}

// ─── Animated dot along path ─────────────────────────────────────────────────
function AnimatedCar({ pathRef, color }: { pathRef: React.RefObject<SVGPathElement>, color: string }) {
  const [offset, setOffset] = useState(0);
  const animRef = useRef<number | null>(null);
  const startRef = useRef<number | null>(null);

  useEffect(() => {
    if (!pathRef.current) return;
    const total = pathRef.current.getTotalLength();

    const animate = (ts: number) => {
      if (!startRef.current) startRef.current = ts;
      const progress = ((ts - startRef.current) / 6000) % 1;
      setOffset(progress * total);
      animRef.current = requestAnimationFrame(animate);
    };

    animRef.current = requestAnimationFrame(animate);
    return () => {
      if (animRef.current) cancelAnimationFrame(animRef.current);
    };
  }, [pathRef]);

  if (!pathRef.current) return null;
  const pt = pathRef.current.getPointAtLength(offset);

  return (
    <g className="pointer-events-none">
      {/* Outer Glow */}
      <circle cx={pt.x} cy={pt.y} r={10} fill={color} opacity={0.5} className="animate-pulse" />
      {/* Main Dot */}
      <circle cx={pt.x} cy={pt.y} r={5} fill={color} stroke="white" strokeWidth={1.5} />
      {/* Center Detail */}
      <circle cx={pt.x} cy={pt.y} r={2} fill="white" />
    </g>
  );
}

// ─── Main Component ───────────────────────────────────────────────────────────
export default function CircuitLayout({
  circuitId = "spa",
  highlightColor = "#E8002D",
  animateCar: initialAnimateCar = true,
  variant = "default"
}: CircuitLayoutProps) {
  const [isAnimateEnabled, setIsAnimateEnabled] = useState(initialAnimateCar);
  const pathRef = useRef<SVGPathElement>(null);
  const [isMounted, setIsMounted] = useState(false);

  const circuit = CIRCUITS[circuitId] || CIRCUITS.spa;
  const drsColor = "#00D2BE";

  useEffect(() => {
    setIsMounted(true);
  }, []);

  const content = (
    <div className="relative bg-[#0d0d0d] rounded-xl overflow-hidden border border-white/5">
      {/* ── Controls ── */}
      <div className="absolute top-3 right-3 z-10 flex gap-1.5 scale-75 origin-right">
        <ToggleButton active={isAnimateEnabled} onClick={() => setIsAnimateEnabled(!isAnimateEnabled)} color={highlightColor} label="Live" />
      </div>

      {/* ── SVG Circuit ── */}
      <div className={`${variant === "minimal" ? "p-4" : "p-8"} flex items-center justify-center`}>
        <svg
          viewBox={circuit.viewBox}
          className="w-full h-auto max-h-[450px]"
          preserveAspectRatio="xMidYMid meet"
        >
          <defs>
            <filter id="glow">
              <feGaussianBlur stdDeviation="2" result="blur" />
              <feMerge>
                <feMergeNode in="blur" />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>
            <filter id="trackGlow">
              <feGaussianBlur stdDeviation="3" result="blur" />
              <feFlood floodColor="white" floodOpacity="0.15" result="flood" />
              <feComposite in="flood" in2="blur" operator="in" />
              <feMerge>
                <feMergeNode />
                <feMergeNode in="SourceGraphic" />
              </feMerge>
            </filter>
          </defs>

          {/* Track Shadow/Glow (Outer) */}
          <path
            d={circuit.path}
            fill="none"
            stroke="white"
            strokeWidth={16}
            strokeLinecap="round"
            strokeLinejoin="round"
            opacity={0.05}
            filter="url(#trackGlow)"
          />

          {/* Main Track Surface (White/Bright) */}
          <path
            d={circuit.path}
            fill="none"
            stroke="#ffffff"
            strokeWidth={8}
            strokeLinecap="round"
            strokeLinejoin="round"
            className="transition-all duration-500"
          />

          {/* Inner Detail Line */}
          <path
            d={circuit.path}
            fill="none"
            stroke="#e0e0e0"
            strokeWidth={0.5}
            strokeLinecap="round"
            strokeLinejoin="round"
            opacity={0.3}
          />

          {/* Racing Line / Highlight Line */}
          <path
            ref={pathRef}
            d={circuit.path}
            fill="none"
            stroke="transparent"
            strokeWidth={2}
            strokeLinecap="round"
            strokeLinejoin="round"
            opacity={0.8}
            filter="url(#glow)"
            className="transition-all duration-500"
          />

          {/* Animated Car */}
          {isAnimateEnabled && isMounted && pathRef.current && (
            <AnimatedCar pathRef={pathRef} color={highlightColor} />
          )}
        </svg>
      </div>

      {/* Legend - only show in full view */}
      {variant === "default" && (
        <div className="absolute bottom-6 left-6 flex flex-col gap-2 bg-[#111]/80 backdrop-blur-md p-3 rounded-xl border border-gray-800">
          <LegendItem color={highlightColor} label="Live Tracker" />
        </div>
      )}

      {/* Bottom Status */}
      <div className={`border-t border-gray-800 px-4 py-2 flex items-center justify-between bg-[#111]/50 ${variant === "minimal" ? "text-[8px]" : "text-[10px]"}`}>
        <div className="flex gap-4 text-gray-500 uppercase font-bold tracking-[0.2em]">
          <span>{variant === "minimal" ? circuitId : "Layout 2.0"}</span>
        </div>
        <div className="flex items-center gap-1.5">
          <span className="relative flex h-1.5 w-1.5">
            <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
            <span className="relative inline-flex rounded-full h-1.5 w-1.5 bg-green-500"></span>
          </span>
          <span className="text-green-500 font-bold uppercase tracking-wider">Live</span>
        </div>
      </div>
    </div>
  );

  return content;
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

function ToggleButton({ active, onClick, color, label }: { active: boolean, onClick: () => void, color: string, label: string }) {
  return (
    <Button
      variant="outline"
      size="sm"
      onClick={onClick}
      className={`h-6 px-2 text-[9px] font-black uppercase tracking-tighter transition-all border-gray-800 hover:bg-gray-800 ${
        active ? "" : "opacity-40"
      }`}
      style={{
        color: active ? color : "#666",
        borderColor: active ? `${color}44` : undefined,
        backgroundColor: active ? `${color}11` : undefined,
      }}
    >
      {label}
    </Button>
  );
}

function LegendItem({ color, label, circle, dashed }: { color: string, label: string, circle?: boolean, dashed?: boolean }) {
  return (
    <div className="flex items-center gap-3">
      {circle ? (
        <span
          className="flex items-center justify-center w-4 h-4 rounded-full border border-[#FFD700] text-[#FFD700] font-black"
          style={{ fontSize: 8 }}
        >
          1
        </span>
      ) : (
        <span
          className="w-5 h-0.5 rounded-full"
          style={{
            background: dashed ? "none" : color,
            border: dashed ? `1px dashed ${color}` : "none",
            boxShadow: !dashed ? `0 0 4px ${color}` : "none"
          }}
        />
      )}
      <span className="text-[9px] text-gray-400 uppercase font-bold tracking-wider">{label}</span>
    </div>
  );
}
