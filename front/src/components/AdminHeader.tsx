import React, { useState, useEffect } from 'react';
import AdminNotification from './AdminNotification';

export default function AdminHeader({ title }: { title: string }) {
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isClient, setIsClient] = useState(false);

  useEffect(() => {
    setIsClient(true);
    const token = localStorage.getItem('adminToken');
    setIsLoggedIn(!!token);
  }, []);

  const handleLogout = () => {
    localStorage.removeItem('adminToken');
    window.location.href = '/admin/login';
  };

  const handleLogin = () => {
    window.location.href = '/admin/login';
  };

  return (
    <header style={{
      background: '#333',
      color: '#fff',
      padding: 16,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      minHeight: 56
    }}>
      <div style={{ fontWeight: 'bold', fontSize: 22, color: '#fff' }}>{title}</div>
      <div style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
        <AdminNotification />
        {isClient && (
          isLoggedIn ? (
            <button
              onClick={handleLogout}
              style={{
                marginLeft: 12,
                padding: '6px 18px',
                background: '#fff',
                color: '#333',
                border: 'none',
                borderRadius: 6,
                fontWeight: 'bold',
                cursor: 'pointer',
                fontSize: 15
              }}
            >
              로그아웃
            </button>
          ) : (
            <button
              onClick={handleLogin}
              style={{
                marginLeft: 12,
                padding: '6px 18px',
                background: '#fff',
                color: '#333',
                border: 'none',
                borderRadius: 6,
                fontWeight: 'bold',
                cursor: 'pointer',
                fontSize: 15
              }}
            >
              로그인
            </button>
          )
        )}
      </div>
    </header>
  );
} 