import React from 'react';
import { Link } from 'react-router-dom';

const Dashboard: React.FC = () => {
  return (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
      <h2>Dashboard</h2>
      <Link to="/login">Logout</Link>
    </div>
  );
};

export default Dashboard; 