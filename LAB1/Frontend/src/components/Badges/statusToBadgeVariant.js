export function statusToBadgeVariant(status) {
  const s = (status == null) ? '' : String(status).toLowerCase().trim();

  if (s === 'running') return 'running';
  if (s === 'stopped') return 'stopped';
  if (s === 'terminated') return 'terminated';

  return 'stopped';
}

export default statusToBadgeVariant;
