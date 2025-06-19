import React from 'react';

const AdminSettlementsPage = () => {
  return (
    <div>
      <h2>정산 관리</h2>
      <div style={{ marginBottom: 16 }}>
        <input type="text" placeholder="Seller No or ID" style={{ marginRight: 8 }} />
        <input type="date" style={{ marginRight: 8 }} />
        <button>Search</button>
      </div>
      <table style={{ width: '100%', background: '#eee' }}>
        <thead>
          <tr>
            <th>Seller No</th>
            <th>정산일</th>
            <th>금액</th>
            <th>상태</th>
            <th>상세</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>SETT001</td>
            <td>2024-06-10</td>
            <td>100,000</td>
            <td>Pending</td>
            <td><button>View</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  );
};

export default AdminSettlementsPage; 