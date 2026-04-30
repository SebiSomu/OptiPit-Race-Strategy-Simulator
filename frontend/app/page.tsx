import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Slider } from "@/components/ui/slider"
import { Badge } from "@/components/ui/badge"

export default function Dashboard() {
  return (
    <div className="p-8 bg-background min-h-screen">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold tracking-tight text-red-600">F1 STRATEGY ENGINE</h1>
        <Badge variant="outline" className="text-green-500 border-green-500">LIVE CONNECTION</Badge>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Control Panel */}
        <Card className="col-span-1 shadow-lg border-primary/20">
          <CardHeader>
            <CardTitle>Race Parameters</CardTitle>
            <CardDescription>Adjust variables in real-time</CardDescription>
          </CardHeader>
          <CardContent className="space-y-6">
            <div className="space-y-2">
              <label className="text-sm font-medium">Track Temperature (32°C)</label>
              <Slider defaultValue={[32]} max={60} step={1} />
            </div>
            <Button className="w-full bg-red-600 hover:bg-red-700">Calculate Optimal Pit Window</Button>
          </CardContent>
        </Card>

        {/* Chart Zone (Placeholder) */}
        <Card className="col-span-2 bg-slate-900/50 border-slate-800">
          <CardContent className="h-[300px] flex items-center justify-center">
            <p className="text-muted-foreground italic">Tyre degradation chart will be displayed here (Recharts / D3)</p>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
