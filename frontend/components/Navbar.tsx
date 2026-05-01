"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";

export function Navbar() {
  const pathname = usePathname();
  const [scrolled, setScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener("scroll", handleScroll);
    return () => window.removeEventListener("scroll", handleScroll);
  }, []);

  return (
    <nav
      className={`fixed top-0 left-0 right-0 z-50 transition-all duration-500 ${
        scrolled
          ? "glass py-3 shadow-lg shadow-black/20"
          : "bg-transparent py-5"
      }`}
    >
      <div className="max-w-7xl mx-auto px-6 flex items-center justify-between">
        {/* Logo */}
        <Link href="/" className="flex items-center gap-3 group">
          <div className="relative">
            <div className="w-9 h-9 bg-[var(--f1-red)] rounded-sm flex items-center justify-center font-black text-white text-sm tracking-tighter rotate-[-2deg] group-hover:rotate-0 transition-transform duration-300">
              F1
            </div>
            <div className="absolute -inset-1 bg-[var(--f1-red)] rounded-sm opacity-0 group-hover:opacity-20 blur-md transition-opacity duration-300" />
          </div>
          <div className="flex flex-col">
            <span className="text-sm font-bold tracking-widest uppercase text-white/90">
              Strategy
            </span>
            <span className="text-[10px] font-medium tracking-[0.2em] uppercase text-white/40">
              Simulator
            </span>
          </div>
        </Link>

        {/* Nav Links */}
        <div className="flex items-center gap-1">
          <NavLink href="/" active={pathname === "/"}>
            Home
          </NavLink>
          <NavLink href="/simulation" active={pathname === "/simulation"}>
            Simulator
          </NavLink>
        </div>
      </div>
    </nav>
  );
}

function NavLink({
  href,
  active,
  children,
}: {
  href: string;
  active: boolean;
  children: React.ReactNode;
}) {
  return (
    <Link
      href={href}
      className={`relative px-4 py-2 text-sm font-semibold uppercase tracking-wider transition-all duration-300 rounded-md ${
        active
          ? "text-white bg-white/5"
          : "text-white/50 hover:text-white/90 hover:bg-white/5"
      }`}
    >
      {children}
      {active && (
        <span className="absolute bottom-0 left-1/2 -translate-x-1/2 w-6 h-[2px] bg-[var(--f1-red)] rounded-full" />
      )}
    </Link>
  );
}
