"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { useEffect, useState } from "react";
import Image from "next/image";

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
        {/* Logo / Brand */}
        <Link href="/" className="flex items-center gap-4 group">
          <div className="relative w-18 h-18 transition-transform duration-500 group-hover:scale-110">
             <Image 
                src="/assets/logo.png" 
                alt="OptiPit Logo" 
                fill
                className="object-contain"
             />
             <div className="absolute -inset-2 bg-[var(--f1-red)] rounded-full opacity-0 group-hover:opacity-10 blur-xl transition-opacity duration-500" />
          </div>
          <div className="flex flex-col">
            <span className="text-xl font-black tracking-tighter uppercase text-white group-hover:text-[var(--f1-red)] transition-colors">
              Opti<span className="text-white">Pit</span>
            </span>
            <span className="text-[9px] font-bold tracking-[0.3em] uppercase text-white/30 -mt-1 ml-0.5">
              F1 Strategy Engine
            </span>
          </div>
        </Link>

        {/* Nav Links */}
        <div className="flex items-center gap-1">
          <NavLink href="/" active={pathname === "/"}>
            Home
          </NavLink>
          <NavLink href="/simulation" active={pathname === "/simulation"}>
            Simulation
          </NavLink>
          <NavLink href="/about" active={pathname === "/about"}>
            About
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
