"use client";

import { useEffect, useState, useCallback, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface DataPoint {
  lap: number;
  lapTime: number;
  compound: string;
}

export function useSimulation() {
  const [data, setData] = useState<DataPoint[]>([]);
  const [status, setStatus] = useState<'IDLE' | 'RUNNING' | 'FINISHED' | 'ERROR'>('IDLE');
  const clientRef = useRef<Client | null>(null);

  const connect = useCallback(() => {
    const socket = new SockJS('http://localhost:8080/ws-race');
    const client = new Client({
      webSocketFactory: () => socket,
      debug: (str) => console.log('STOMP: ' + str),
      onConnect: () => {
        console.log('Connected to WebSocket server');
        setStatus('IDLE');
        client.subscribe('/topic/race-updates', (message) => {
          const update = JSON.parse(message.body);
          if (update.status === 'FINISHED') {
            setStatus('FINISHED');
          } else if (update.status === 'ERROR') {
            console.error('Simulation error received:', update.message);
            setStatus('ERROR');
          } else {
            setData((prev) => [...prev, update]);
          }
        });
      },
      onStompError: (frame) => {
        console.error('Broker reported error: ' + frame.headers['message']);
        console.error('Additional details: ' + frame.body);
        setStatus('ERROR');
      },
      onWebSocketClose: () => {
        console.log('WebSocket connection closed');
      }
    });

    client.activate();
    clientRef.current = client;

    return () => {
      client.deactivate();
    };
  }, []);

  const startSimulation = (circuitId: number, compoundId: number) => {
    if (clientRef.current && clientRef.current.connected) {
      setData([]);
      setStatus('RUNNING');
      clientRef.current.publish({
        destination: '/app/start-simulation',
        body: JSON.stringify({ circuitId, compoundId }),
      });
    } else {
      console.error('Not connected to WebSocket');
    }
  };

  useEffect(() => {
    return connect();
  }, [connect]);

  return { data, status, startSimulation };
}
