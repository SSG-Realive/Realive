import React from 'react';
import Link from 'next/link';

const AdminReportsPage = () => {
  return (
    <div>
      <h1>Admin Reports</h1>
      <div>
        <h2>Report List</h2>
        <ul>
          <li>Report 1</li>
          <li>Report 2</li>
          <li>Report 3</li>
        </ul>
      </div>
      <Link href="/admin/dashboard">Back to Dashboard</Link>
    </div>
  );
};

export default AdminReportsPage; 