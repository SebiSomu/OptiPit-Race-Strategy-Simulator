"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Slider } from "@/components/ui/slider"
import { Badge } from "@/components/ui/badge"
import { CircuitSelector } from "@/components/CircuitSelector"
import { StrategyChart } from "@/components/StrategyChart"
import { useSimulation } from "@/hooks/useSimulation"

export default function Dashboard() {
  const { data, status, startSimulation } = useSimulation();

  return (
    <div className="p-8 bg-background min-h-screen text-slate-100">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold tracking-tight text-red-600">F1 STRATEGY ENGINE</h1>
        <Badge variant="outline" className={`${status === 'RUNNING' ? 'text-green-500 border-green-500 animate-pulse' : 'text-slate-500 border-slate-500'}`}>
          {status === 'RUNNING' ? 'LIVE SIMULATION' : status === 'FINISHED' ? 'SIMULATION FINISHED' : 'READY'}
        </Badge>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Control Panel */}
        <Card className="col-span-1 shadow-lg border-slate-800 bg-slate-900/50">
          <CardHeader>
            <CardTitle>Race Parameters</CardTitle>
            <CardDescription>Adjust variables in real-time</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-2">
              <label className="text-sm font-medium">Circuit</label>
              <CircuitSelector />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Track Temperature (32°C)</label>
              <Slider defaultValue={[32]} max={60} step={1} />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium">Driver Aggression (Balanced)</label>
              <Slider defaultValue={[50]} max={100} step={1} />
            </div>
            <Button 
              className="w-full bg-red-600 hover:bg-red-700 font-bold"
              onClick={() => startSimulation(1, 1)}
              disabled={status === 'RUNNING'}
            >
              {status === 'RUNNING' ? 'Simulation in Progress...' : 'Start Live Simulation'}
            </Button>
          </CardContent>
        </Card>

        {/* Chart Zone */}
        <Card className="col-span-2 bg-slate-900/50 border-slate-800 flex flex-col">
          <CardHeader>
            <CardTitle>Live Race Trace</CardTitle>
            <CardDescription>Real-time lap time telemetry</CardDescription>
          </CardHeader>
          <CardContent className="flex-1 min-h-[400px]">
            <StrategyChart data={data} />
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
