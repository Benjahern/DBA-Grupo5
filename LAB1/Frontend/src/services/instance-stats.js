import { getToken } from './auth.js';
import { apiBaseUrl } from './http-common.js';

const resolveBaseUrl = () => apiBaseUrl || 'http://localhost:8080';

export function openInstanceStatsStream(instanceId, handlers = {}) {
  const controller = new AbortController();
  const { onLine, onConnected, onError } = handlers;

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
      lines.forEach((line) => onLine?.(line));
    }
  })().catch((err) => {
    if (err?.name === 'AbortError') return;
    onError?.(err);
  });

  return controller;
}
