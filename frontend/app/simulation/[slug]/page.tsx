import { use } from "react";
import { Navbar } from "@/components/Navbar";
import { SimulationDashboard } from "@/components/simulation/SimulationDashboard";

export default function DynamicSimulationPage({ params }: { params: Promise<{ slug: string }> }) {
  const { slug } = use(params);

  return (
    <div className="min-h-screen bg-[var(--f1-dark)]">
      <Navbar />
      
      <div className="pt-28 pb-6 px-6 max-w-7xl mx-auto">
        <div className="flex items-center gap-3 mb-1">
          <div className="w-1 h-8 bg-[var(--f1-red)] rounded-full" />
          <h1 className="text-3xl md:text-4xl font-black uppercase tracking-tight text-white">
            Opti<span className="text-[var(--f1-red)]">Pit</span> Simulator
          </h1>
        </div>
        <p className="text-white/40 text-sm ml-4 pl-1 uppercase tracking-widest font-bold">
           Mission Control · Dynamic Mode
        </p>
      </div>

      <SimulationDashboard initialSlug={slug} />
    </div>
  );
}
