import { getToken } from './auth.js';
import { apiBaseUrl } from './http-common.js';

const resolveBaseUrl = () => apiBaseUrl || 'http://localhost:8080';

const clampPercent = (value) => {
  const parsed = Number(value);
  if (!Number.isFinite(parsed)) return 0;
  return Math.max(0, Math.min(100, parsed));
};

export const parseDockerStats = (line) => {
  if (!line) return null;
  try {
    const statsJSON = JSON.parse(line);

    // Calcular CPU %
    let cpuPercent = 0.0;
    const cpuStats = statsJSON.cpu_stats;
    const preCpuStats = statsJSON.precpu_stats || statsJSON.precpu_stats;
    
    if (cpuStats && preCpuStats && cpuStats.cpu_usage && preCpuStats.cpu_usage) {
      const cpuDelta = cpuStats.cpu_usage.total_usage - preCpuStats.cpu_usage.total_usage;
      const systemDelta = cpuStats.system_cpu_usage - preCpuStats.system_cpu_usage;
      
      if (systemDelta > 0.0 && cpuDelta > 0.0) {
        let cpus = 1;
        if (cpuStats.cpu_usage.percpu_usage) {
          cpus = cpuStats.cpu_usage.percpu_usage.length;
        } else if (cpuStats.online_cpus) {
          cpus = cpuStats.online_cpus;
        }
        cpuPercent = (cpuDelta / systemDelta) * cpus * 100.0;
      }
    }

    // Calcular Memoria %
    let memPercent = 0.0;
    const memStats = statsJSON.memory_stats;
    if (memStats && memStats.limit && memStats.limit > 0) {
      memPercent = (memStats.usage / memStats.limit) * 100.0;
    }

    // Calcular Disco / Block IO
    let storagePercent = 0.0;
    const blkioStats = statsJSON.blkio_stats;
    if (blkioStats && blkioStats.io_service_bytes_recursive) {
      let readBytes = 0;
      let writeBytes = 0;
      for (const stat of blkioStats.io_service_bytes_recursive) {
        if (stat.op && stat.op.toLowerCase() === 'read') readBytes += stat.value;
        if (stat.op && stat.op.toLowerCase() === 'write') writeBytes += stat.value;
      }
      const total = readBytes + writeBytes;
      if (total > 0) {
        storagePercent = (writeBytes / total) * 100.0;
      }
    }

    return {
      cpu: parseFloat(clampPercent(cpuPercent).toFixed(2)),
      memory: parseFloat(clampPercent(memPercent).toFixed(2)),
      storage: parseFloat(clampPercent(storagePercent).toFixed(2))
    };
  } catch (e) {
    return null;
  }
};

export function openInstanceStatsStream(instanceId, handlers = {}) {
  const controller = new AbortController();
  const { onStats, onConnected, onError } = handlers;

  (async () => {
    const headers = {};
    const token = getToken();
    if (token) {
      headers.Authorization = `Bearer ${token}`;
    }

    const response = await fetch(`${resolveBaseUrl()}/api/instances/${instanceId}/stats`, {
      method: 'GET',
      headers,
      signal: controller.signal,
    });

    if (!response.ok) {
      throw new Error(`Status ${response.status}`);
    }

    if (!response.body) {
      throw new Error('Sin stream');
    }

    onConnected?.();

    const reader = response.body.getReader();
    const decoder = new TextDecoder('utf-8');
    let buffer = '';

    while (true) {
      const { value, done } = await reader.read();
      if (done) break;
      buffer += decoder.decode(value, { stream: true });
      const lines = buffer.split(/\r?\n/);
      buffer = lines.pop() || '';
      lines.forEach((line) => {
        const computedStats = parseDockerStats(line);
        if (computedStats) {
          onStats?.(computedStats);
        }
      });
    }
  })().catch((err) => {
    if (err?.name === 'AbortError') return;
    onError?.(err);
  });

  return controller;
}
