import React, { useState } from 'react';

const dummyNotifications = [
  { id: 1, message: '신규 주문이 접수되었습니다.', read: false },
  { id: 2, message: '신고가 접수되었습니다.', read: false },
  { id: 3, message: '정산 요청이 도착했습니다.', read: true },
];

export default function AdminNotification() {
  const [open, setOpen] = useState(false);
  const unreadCount = dummyNotifications.filter(n => !n.read).length;

  return (
    <div style={{ position: 'relative', display: 'inline-block', marginLeft: 16 }}>
      <button onClick={() => setOpen(v => !v)} style={{ background: 'none', border: 'none', cursor: 'pointer', position: 'relative' }}>
        <span style={{ fontSize: 22 }}>🔔</span>
        {unreadCount > 0 && (
          <span style={{ position: 'absolute', top: -6, right: -6, background: 'red', color: 'white', borderRadius: '50%', fontSize: 12, padding: '2px 6px' }}>{unreadCount}</span>
        )}
      </button>
      {open && (
        <div style={{ position: 'absolute', right: 0, top: 32, background: '#fff', border: '1px solid #ccc', borderRadius: 4, minWidth: 220, zIndex: 10 }}>
          <ul style={{ listStyle: 'none', margin: 0, padding: 8 }}>
            {dummyNotifications.map(n => (
              <li key={n.id} style={{ padding: 8, background: n.read ? '#f5f5f5' : '#ffeaea', borderBottom: '1px solid #eee' }}>{n.message}</li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
} 