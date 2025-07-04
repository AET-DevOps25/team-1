import React from 'react';
import { Link } from 'react-router-dom';

const Register: React.FC = () => {
  return (
    <div style={{ textAlign: 'center', marginTop: '50px' }}>
      <h2>Register Page</h2>
      <Link to="/login">Login</Link>
    </div>
  );
};

export default Register; 