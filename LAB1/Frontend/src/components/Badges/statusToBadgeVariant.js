// Utility to map instance state to Badge variant name.
export function statusToBadgeVariant(status) {
  const s = (status == null) ? '' : String(status).toLowerCase().trim();

  if (s === 'running') return 'running';
  if (s === 'stopped') return 'stopped';
  if (s === 'terminated') return 'terminated';

  return undefined;
}

export default statusToBadgeVariant;
