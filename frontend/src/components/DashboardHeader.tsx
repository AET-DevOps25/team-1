import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { ViewMode } from '../types/dashboard';
import { logout } from '../utils/auth';

interface DashboardHeaderProps {
  selectedView: ViewMode;
  searchTerm: string;
  resultsCount: number;
  onViewChange: (view: ViewMode) => void;
  onSearchChange: (term: string) => void;
  onAddHrClick: () => void;
  onCreateJobClick: () => void;
}

const DashboardHeader: React.FC<DashboardHeaderProps> = ({
  selectedView,
  searchTerm,
  resultsCount,
  onViewChange,
  onSearchChange,
  onAddHrClick,
  onCreateJobClick
}) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    if (window.confirm('Are you sure you want to logout?')) {
      logout();
      navigate('/login', { replace: true });
    }
  };

  return (
    <header className="dashboard-header">
      <div className="search-bar-container" style={{ display: 'flex', alignItems: 'center', gap: 20 }}>
        <div>
          <button 
            style={{ 
              marginRight: 8, 
              padding: '6px 12px', 
              backgroundColor: selectedView==='applications' ? '#1a1a1a' : '#ecf0f1', 
              color: selectedView==='applications' ? '#fff' : '#1a1a1a', 
              border: 'none', 
              borderRadius: 4, 
              cursor: 'pointer' 
            }}
            onClick={() => onViewChange('applications')}
          >
            Applications
          </button>
          <button 
            style={{ 
              padding: '6px 12px', 
              backgroundColor: selectedView==='jobs' ? '#1a1a1a' : '#ecf0f1', 
              color: selectedView==='jobs' ? '#fff' : '#1a1a1a', 
              border: 'none', 
              borderRadius: 4, 
              cursor: 'pointer' 
            }}
            onClick={() => onViewChange('jobs')}
          >
            Jobs
          </button>
        </div>
        <input 
          type="text" 
          placeholder="Search candidates, jobs, emails..." 
          className="search-input"
          value={searchTerm}
          onChange={(e) => onSearchChange(e.target.value)}
        />
        <span className="results-count">{resultsCount} applications</span>
      </div>
      <div className="header-actions" style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
        <div className="user-moon">TEAM1 ADMIN</div>
        <button 
          style={{ 
            padding: '6px 12px', 
            backgroundColor: '#2ecc71', 
            color: '#fff', 
            border: 'none', 
            borderRadius: 4, 
            cursor: 'pointer' 
          }}
          onClick={onAddHrClick}
        >
          Add HR
        </button>
        {selectedView==='jobs' && (
        <button 
          style={{ 
            padding:'6px 12px', 
            backgroundColor:'#2ecc71', 
            color:'#fff', 
            border:'none', 
            borderRadius:4, 
            cursor:'pointer' 
          }}
          onClick={onCreateJobClick}
        >
          Create Job
        </button>)}
        <button 
          style={{ 
            padding: '6px 12px', 
            backgroundColor: '#e74c3c', 
            color: '#fff', 
            border: 'none', 
            borderRadius: 4, 
            cursor: 'pointer',
            marginLeft: '10px'
          }}
          onClick={handleLogout}
          title="Logout"
        >
          Logout
        </button>
      </div>
    </header>
  );
};

export default DashboardHeader;