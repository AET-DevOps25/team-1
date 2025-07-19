import React from 'react';
import { useNavigate } from 'react-router-dom';
import type { Job } from '../types/dashboard';

interface JobsListProps {
  jobs: Job[];
  isLoading: boolean;
  onEditJob: (job: Job) => void;
  onToggleJobStatus: (job: Job) => void;
  onRefresh: () => void;
}

const JobsList: React.FC<JobsListProps> = ({
  jobs,
  isLoading,
  onEditJob,
  onToggleJobStatus,
  onRefresh
}) => {
  const navigate = useNavigate();

  const handleJobClick = (job: Job) => {
    navigate(`/job/${job.jobId}/applications`);
  };

  return (
    <div className="jobs-list">
      <div className="candidate-list-header">
        <div style={{ flexBasis: '35%', fontWeight: 'bold' }}>JOB TITLE</div>
        <div style={{ flexBasis: '12%', fontWeight: 'bold' }}>STATUS</div>
        <div style={{ flexBasis: '18%', fontWeight: 'bold' }}>CREATED</div>
        <div style={{ flexBasis: '15%', fontWeight: 'bold' }}>APPLICATIONS</div>
        <div style={{ flexBasis: '20%', fontWeight: 'bold' }}>ACTIONS</div>
        <div style={{ marginLeft:'auto' }}>
          <button 
            onClick={onRefresh} 
            style={{ 
              border:'none', 
              background:'transparent', 
              cursor:'pointer', 
              fontSize:16 
            }}
            title="Refresh jobs list"
          >
            Refresh
          </button>
        </div>
      </div>
      
      {isLoading && (
        <div style={{ textAlign:'center', padding:20 }}>
          Loading jobs…
        </div>
      )}
      
      {jobs.map(job => (
        <div key={job.jobId} className="candidate-item">
          <div style={{ flexBasis: '35%' }}>
            <button
              onClick={() => handleJobClick(job)}
              style={{
                background: 'none',
                border: 'none',
                padding: '0',
                color: '#3498db',
                textDecoration: 'underline',
                cursor: 'pointer',
                fontSize: '14px',
                textAlign: 'left',
                width: '100%'
              }}
              title="Click to view applications for this job"
            >
              {job.title}
            </button>
          </div>
          <div style={{ flexBasis: '12%' }}>
            <span 
              style={{ 
                backgroundColor: job.status==='OPEN' ? '#27ae60' : job.status==='CLOSED' ? '#e74c3c' : '#95a5a6', 
                color:'#fff', 
                padding:'4px 8px', 
                borderRadius:4, 
                fontSize:12 
              }}
            >
              {job.status}
            </span>
          </div>
          <div style={{ flexBasis: '18%' }}>
            {new Date(job.createdAt).toLocaleDateString()}
          </div>
          <div style={{ flexBasis: '15%' }}>
            <button
              onClick={() => handleJobClick(job)}
              style={{
                background: 'none',
                border: '1px solid #ddd',
                padding: '4px 8px',
                borderRadius: '4px',
                cursor: 'pointer',
                fontSize: '12px',
                color: '#666'
              }}
              title="View applications for this job"
            >
              View Apps
            </button>
          </div>
          <div style={{ flexBasis: '20%' }}>
            <button 
              style={{ 
                padding:'4px 8px', 
                marginRight:6, 
                border:'none', 
                borderRadius:4, 
                cursor:'pointer', 
                backgroundColor: '#8e44ad', 
                color:'#fff',
                fontSize: '12px'
              }}
              onClick={() => onEditJob(job)}
            >
              Edit
            </button>
            <button 
              style={{ 
                padding:'4px 8px', 
                border:'none', 
                borderRadius:4, 
                cursor:'pointer', 
                backgroundColor: '#3498db', 
                color:'#fff',
                fontSize: '12px'
              }}
              onClick={() => onToggleJobStatus(job)}
            >
              {job.status === 'OPEN' ? 'Close' : 'Open'}
            </button>
          </div>
        </div>
      ))}
      
      {jobs.length === 0 && !isLoading && (
        <div className="empty-state" style={{ 
          textAlign:'center', 
          padding:40, 
          color:'#7f8c8d' 
        }}>
          <p>No jobs found.</p>
        </div>
      )}
    </div>
  );
};

export default JobsList;