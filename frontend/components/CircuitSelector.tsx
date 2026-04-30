"use client";

import * as React from "react"
import {
  Select,
  SelectContent,
  SelectGroup,
  SelectItem,
  SelectLabel,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"

export function CircuitSelector() {
  return (
    <Select>
      <SelectTrigger className="w-[280px]">
        <SelectValue placeholder="Select a Circuit" />
      </SelectTrigger>
      <SelectContent>
        <SelectGroup>
          <SelectLabel>Circuits</SelectLabel>
          <SelectItem value="monaco">Circuit de Monaco</SelectItem>
          <SelectItem value="spa">Spa-Francorchamps</SelectItem>
          <SelectItem value="silverstone">Silverstone Circuit</SelectItem>
          <SelectItem value="monza">Autodromo Nazionale Monza</SelectItem>
          <SelectItem value="suzuka">Suzuka International Racing Course</SelectItem>
        </SelectGroup>
      </SelectContent>
    </Select>
  )
}
