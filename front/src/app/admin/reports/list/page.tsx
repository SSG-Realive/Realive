import React from 'react';

const AdminReportListPage = () => {
  return (
    <div>
      <h2>신고된 목록</h2>
      <table style={{ width: '100%', background: '#eee', marginTop: 16 }}>
        <thead>
          <tr>
            <th>신고자</th>
            <th>피신고자</th>
            <th>사유</th>
            <th>일시</th>
            <th>상태</th>
            <th>상세</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>user1</td>
            <td>user2</td>
            <td>욕설</td>
            <td>2024-06-10</td>
            <td>처리중</td>
            <td><button>보기</button></td>
          </tr>
        </tbody>
      </table>
    </div>
  );
};

export default AdminReportListPage; 