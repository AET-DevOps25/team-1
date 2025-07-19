import React from 'react';
import { useNavigate } from 'react-router-dom';
import { logout } from '../utils/auth';

interface SidebarProps {}

const Sidebar: React.FC<SidebarProps> = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <div className="sidebar">
      <div className="logo">Back</div>
      
      <div className="sidebar-section">
        <div className="sidebar-title">NAVIGATION</div>
        <div className="sidebar-item active">
          Dashboard
        </div>
      </div>

      <div style={{ marginTop: 'auto', paddingTop: '20px' }}>
        <button 
          style={{ 
            width: '100%',
            padding: '10px', 
            backgroundColor: '#e74c3c', 
            color: '#fff', 
            border: 'none', 
            borderRadius: 4, 
            cursor: 'pointer',
            fontSize: '14px'
          }}
          onClick={handleLogout}
          title="Logout"
        >
          Logout
        </button>
      </div>
    </div>
  );
};

export default Sidebar;