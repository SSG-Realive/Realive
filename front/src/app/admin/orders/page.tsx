import React from 'react';

const orders = [
  { id: 1, product: '의자', customer: 'Hong Gil-dong', seller: 'Kim Young-hee', status: 'Shipping', amount: 50000, date: '2024-06-10', action: 'View' },
  { id: 2, product: '책상', customer: 'Park Dong-min', seller: 'Hong Gil-dong', status: 'Pending', amount: 120000, date: '2024-06-09', action: 'View' },
  { id: 3, product: '소파', customer: 'Kim Young-hee', seller: 'Park Dong-min', status: 'Delivered', amount: 300000, date: '2024-06-08', action: 'View' },
];

export default function AdminOrdersPage() {
  return (
    <div>
      <h2>주문 관리</h2>
      {/* 주문 목록, 검색, 상세 등 기능을 여기에 추가 */}
    </div>
  );
} 