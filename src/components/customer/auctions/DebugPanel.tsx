interface DebugPanelProps {
  page: number;
  hasNext: boolean;
  loading: boolean;
  total: number;
  category: string;
}

export default function DebugPanel({ page, hasNext, loading, total, category }: DebugPanelProps) {
  if (process.env.NODE_ENV !== 'development') return null;

  return (
    <div style={{
      position: 'fixed',
      bottom: 10,
      right: 10,
      backgroundColor: 'rgba(0,0,0,0.8)',
      color: 'white',
      padding: 10,
      borderRadius: 4,
      fontSize: 12,
      zIndex: 1000
    }}>
      <div>Page: {page}</div>
      <div>HasNext: {hasNext.toString()}</div>
      <div>Loading: {loading.toString()}</div>
      <div>Items: {total}</div>
      <div>Category: {category || 'All'}</div>
    </div>
  );
}
