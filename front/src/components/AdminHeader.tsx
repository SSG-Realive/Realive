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
      background: '#1f2937',
      color: '#e5e7eb',
      padding: '16px 24px',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      minHeight: 56,
      borderBottom: '1px solid #4b5563'
    }}>
      {/* 햄버거 버튼 (모바일에서만) */}
      <button
        className="md:hidden mr-2 text-2xl"
        aria-label="사이드바 열기"
        onClick={() => {
          document.getElementById('mobile-sidebar')?.classList.remove('-translate-x-full');
          document.getElementById('mobile-sidebar-overlay')?.classList.remove('hidden');
        }}
      >
        ≡
      </button>
      <h1 style={{ fontWeight: 'bold', fontSize: 20, color: '#e5e7eb' }}>{title}</h1>
      <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <AdminNotification />
        {isClient && (
          isLoggedIn ? (
            <button
              onClick={handleLogout}
              style={{
                padding: '8px 16px',
                background: '#14b8a6',
                color: '#fff',
                border: 'none',
                borderRadius: 6,
                fontWeight: 'bold',
                cursor: 'pointer',
                fontSize: 14
              }}
            >
              로그아웃
            </button>
          ) : (
            <button
              onClick={handleLogin}
              style={{
                padding: '8px 16px',
                background: '#14b8a6',
                color: '#fff',
                border: 'none',
                borderRadius: 6,
                fontWeight: 'bold',
                cursor: 'pointer',
                fontSize: 14
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