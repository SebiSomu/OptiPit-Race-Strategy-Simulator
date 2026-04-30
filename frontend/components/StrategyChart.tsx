"use client";

import React from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';

interface DataPoint {
  lap: number;
  lapTime: number;
  compound: string;
}

export function StrategyChart({ data }: { data: DataPoint[] }) {
  return (
    <div className="w-full h-full min-h-[400px]">
      <ResponsiveContainer width="100%" height="100%">
        <LineChart
          data={data}
          margin={{ top: 20, right: 30, left: 20, bottom: 20 }}
        >
          <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
          <XAxis 
            dataKey="lap" 
            stroke="#94a3b8" 
            fontSize={12}
            tickLine={false}
            axisLine={false}
            label={{ value: 'Lap Number', position: 'insideBottom', offset: -10, fill: '#94a3b8', fontSize: 12 }} 
          />
          <YAxis 
            stroke="#94a3b8" 
            fontSize={12}
            tickLine={false}
            axisLine={false}
            label={{ value: 'Lap Time (s)', angle: -90, position: 'insideLeft', fill: '#94a3b8', fontSize: 12 }} 
            domain={['auto', 'auto']}
          />
          <Tooltip 
            contentStyle={{ backgroundColor: '#1e293b', border: '1px solid #334155', borderRadius: '8px', color: '#f8fafc' }}
            itemStyle={{ color: '#f8fafc' }}
          />
          <Legend wrapperStyle={{ paddingTop: '20px' }} />
          <Line 
            type="monotone" 
            dataKey="lapTime" 
            stroke="#ef4444" 
            name="Lap Time" 
            dot={false} 
            strokeWidth={3} 
            animationDuration={0}
          />
        </LineChart>
      </ResponsiveContainer>
    </div>
  );
}
