export function Footer() {
  return (
    <footer className="bg-[var(--f1-dark)] border-t border-white/10 py-10">
      <div className="max-w-6xl mx-auto px-6">
        <div className="flex flex-col md:flex-row items-center justify-between gap-6">
          {/* Logo */}
          <div className="text-xl font-black uppercase tracking-tight">
            <span className="text-white">Opti</span>
            <span className="text-[var(--f1-red)]">Pit</span>
          </div>

          {/* Copyright */}
          <div className="text-sm text-white/30 uppercase tracking-wider">
            © 2026 Strategy Engine — Not affiliated with Formula 1 or FIA
          </div>

          {/* Status */}
          <div className="flex items-center gap-2">
            <span className="w-2.5 h-2.5 rounded-full bg-green-500 animate-pulse" />
            <span className="text-sm text-white/30 uppercase tracking-wider">
              Operational
            </span>
          </div>
        </div>
      </div>
    </footer>
  );
}
