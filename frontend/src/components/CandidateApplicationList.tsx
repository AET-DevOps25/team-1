import React, { useState } from 'react';
import { Button } from '@mui/material';
import { 
  Chat as ChatIcon
} from '@mui/icons-material';
import type { Application, SortField, SortDirection } from '../types/dashboard';
import CandidateChatModal from './modals/CandidateChatModal';

interface CandidateApplicationListProps {
  applications: Application[];
  sortField: SortField;
  sortDirection: SortDirection;
  onSort: (field: SortField) => void;
  onRefreshApplications?: () => void;
}

const CandidateApplicationList: React.FC<CandidateApplicationListProps> = ({
  applications,
  sortField,
  sortDirection,
  onSort,
  onRefreshApplications
}) => {
  const [isCandidateChatModalOpen, setIsCandidateChatModalOpen] = useState(false);
  const [selectedChatApplication, setSelectedChatApplication] = useState<Application | null>(null);

  const getSortIcon = (field: SortField): string => {
    if (sortField !== field) return '';
    return sortDirection === 'asc' ? ' ↑' : ' ↓';
  };


  const getStatusDisplay = (status: string) => {
    switch (status) {
      case 'SUBMITTED': return 'New Application';
      case 'AI_SCREENING': return 'AI Screening';
      case 'AI_INTERVIEW': return 'AI Interview';
      case 'COMPLETED': return 'Assessment Complete';
      case 'SHORTLISTED': return 'Shortlisted';
      case 'REJECTED': return 'Rejected';
      case 'HIRED': return 'Hired';
      default: return status;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'SUBMITTED': return '#3498db';
      case 'AI_SCREENING': return '#f39c12';
      case 'AI_INTERVIEW': return '#e67e22';
      case 'COMPLETED': return '#9b59b6';
      case 'SHORTLISTED': return '#27ae60';
      case 'REJECTED': return '#e74c3c';
      case 'HIRED': return '#2ecc71';
      default: return '#95a5a6';
    }
  };

  if (applications.length === 0) {
    return (
      <div className="empty-state" style={{ 
        textAlign: 'center', 
        padding: '40px', 
        color: '#7f8c8d' 
      }}>
        <p>No applications found matching your criteria.</p>
      </div>
    );
  }

  return (
    <div className="candidate-list">
      <div className="candidate-list-header">
        <div 
          className="name-company-header sortable"
          onClick={() => onSort('name')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('name')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '35%' }}
        >
          JOB TITLE {getSortIcon('name')}
        </div>
        <div 
          className="jobs-header sortable"
          onClick={() => onSort('status')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('status')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '20%' }}
        >
          STATUS {getSortIcon('status')}
        </div>
        <div 
          className="chat-header"
          style={{ flexBasis: '25%' }}
        >
          INTERVIEW
        </div>
        <div 
          className="stage-header sortable"
          onClick={() => onSort('date')}
          style={{ flexBasis: '20%' }}
        >
          DATE & TIME {getSortIcon('date')}
        </div>
      </div>
      
      {applications.map(application => (
        <div key={application.applicationId || application.application_id} className="candidate-item">
          <div className="name-company" style={{ flexBasis: '35%' }}>
            <span className={`avatar-dot ${application.avatarColor}`}></span>
            <div className="candidate-info">
              <span className="candidate-name">
                {application.job_title || 'Job Title'}
              </span>
              <span className="candidate-company" style={{ fontSize: '12px', color: '#666' }}>
                Application ID: {(application.applicationId || application.application_id || '').substring(0, 8)}...
              </span>
            </div>
          </div>
          
          <div className="jobs" style={{ flexBasis: '20%' }}>
            <span 
              className="status-badge"
              style={{ 
                backgroundColor: getStatusColor(application.status),
                color: 'white',
                padding: '4px 8px',
                borderRadius: '4px',
                fontSize: '11px',
                fontWeight: 'bold'
              }}
            >
              {getStatusDisplay(application.status)}
            </span>
          </div>

          <div className="chat" style={{ flexBasis: '25%' }}>
            <Button 
              variant="contained"
              color="primary"
              size="small"
              startIcon={<ChatIcon />}
              onClick={() => {
                setSelectedChatApplication(application);
                setIsCandidateChatModalOpen(true);
              }}
              sx={{ 
                textTransform: 'none',
                fontSize: '12px',
                bgcolor: '#3498db',
                '&:hover': {
                  bgcolor: '#2980b9'
                }
              }}
              title="Start/continue interview chat"
            >
              Start Interview
            </Button>
          </div>
          
          <div className="stage" style={{ flexBasis: '20%' }}>
            <div style={{ fontSize: '12px' }}>
              {(() => {
                const possibleDates = [
                  application.submission_timestamp,
                  application.createdAt, 
                  application.updatedAt
                ];
                
                for (const dateValue of possibleDates) {
                  if (dateValue) {
                    try {
                      const date = new Date(dateValue);
                      if (!isNaN(date.getTime())) {
                        return date.toLocaleDateString('en-US', {
                          month: '2-digit',
                          day: '2-digit', 
                          year: 'numeric'
                        });
                      }
                    } catch (error) {
                      console.error('Date parsing error:', error);
                    }
                  }
                }
                
                return '--';
              })()}
            </div>
          </div>
        </div>
      ))}
      
      <CandidateChatModal
        isOpen={isCandidateChatModalOpen}
        application={selectedChatApplication}
        onClose={() => {
          setIsCandidateChatModalOpen(false);
          setSelectedChatApplication(null);
        }}
        onInterviewComplete={() => {
          setIsCandidateChatModalOpen(false);
          setSelectedChatApplication(null);
          if (onRefreshApplications) {
            onRefreshApplications();
          }
        }}
      />
    </div>
  );
};

export default CandidateApplicationList;