import React, { useState } from 'react';
import { Button } from '@mui/material';
import { 
  Visibility as VisibilityIcon, 
  Chat as ChatIcon,
  Assessment as AssessmentIcon
} from '@mui/icons-material';
import type { Application, SortField, SortDirection } from '../types/dashboard';
import { isHrUser, getValidToken } from '../utils/auth';
import ChatHistoryModal from './modals/ChatHistoryModal';
import CandidateChatModal from './modals/CandidateChatModal';
import apiConfig from '../utils/api';

interface ApplicationListProps {
  applications: Application[];
  sortField: SortField;
  sortDirection: SortDirection;
  onSort: (field: SortField) => void;
  onStatusClick: (application: Application) => void;
  onHrDecisionClick: (application: Application) => void;
  onDetailsClick: (application: Application) => void;
  onRefreshApplications?: () => void; // Optional callback to refresh applications
}

const ApplicationList: React.FC<ApplicationListProps> = ({
  applications,
  sortField,
  sortDirection,
  onSort,
  onStatusClick,
  onHrDecisionClick,
  onDetailsClick,
  onRefreshApplications
}) => {
  const [isChatHistoryModalOpen, setIsChatHistoryModalOpen] = useState(false);
  const [isCandidateChatModalOpen, setIsCandidateChatModalOpen] = useState(false);
  const [selectedChatApplication, setSelectedChatApplication] = useState<Application | null>(null);
  
  const [scoringApplications, setScoringApplications] = useState<Set<string>>(new Set());

  const triggerManualScoring = async (application: Application) => {
    const token = getValidToken();
    if (!token) {
      alert('Please login again to trigger scoring.');
      return;
    }

    const applicationId = application.applicationId || application.application_id;
    if (!applicationId) {
      alert('No application ID found');
      return;
    }
    setScoringApplications(prev => new Set(prev).add(applicationId));

    try {
      console.log('Triggering manual interview scoring for application:', applicationId);
      
      const response = await fetch(apiConfig.getFullURL(`/api/v1/applications/${applicationId}/assessment/interview/trigger`), {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        },
        credentials: 'include'
      });

      if (response.ok) {
        const data = await response.json();
        if (data.success) {
          alert('Interview scoring triggered successfully! Scores will be updated shortly.');
          if (onRefreshApplications) {
            setTimeout(() => {
              onRefreshApplications();
            }, 2000); // Give server time to process scoring
          }
        } else {
          alert(data.message || 'Failed to trigger scoring');
        }
      } else {
        const errorData = await response.json().catch(() => ({}));
        alert(errorData.message || `Failed to trigger scoring (${response.status})`);
      }
    } catch (error) {
      console.error('Error triggering manual scoring:', error);
      alert('Error triggering scoring. Please try again.');
    } finally {
      setScoringApplications(prev => {
        const newSet = new Set(prev);
        newSet.delete(applicationId);
        return newSet;
      });
    }
  };

  const getSortIcon = (field: SortField): string => {
    if (sortField !== field) return '';
    return sortDirection === 'asc' ? ' ↑' : ' ↓';
  };

  const copyEmailToClipboard = async (email?: string) => {
    try {
      if (email) {
        await navigator.clipboard.writeText(email);
      }
      alert('Email copied to clipboard!');
    } catch (err) {
      console.error('Failed to copy email:', err);
      alert('Failed to copy email');
    }
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

  const getAiRecommendDisplay = (status?: string) => {
    switch (status) {
      case 'RECOMMEND': return 'Recommend';
      case 'NOT_RECOMMEND': return 'Not Recommend';
      case 'CONDITIONAL': return 'Conditional';
      default: return 'Pending';
    }
  };

  const getAiRecommendColor = (status?: string) => {
    switch (status) {
      case 'RECOMMEND': return '#27ae60';
      case 'NOT_RECOMMEND': return '#e74c3c';
      case 'CONDITIONAL': return '#f39c12';
      default: return '#95a5a6';
    }
  };

  const getHrDecisionDisplay = (decision?: string) => {
    switch (decision) {
      case 'PENDING': return 'Pending Review';
      case 'APPROVE': return 'Approved';
      case 'REJECT': return 'Rejected';
      case 'INTERVIEW': return 'Schedule Interview';
      case 'FOLLOW_UP': return 'Follow Up';
      default: return 'Pending Review';
    }
  };

  const getHrDecisionColor = (decision?: string) => {
    switch (decision) {
      case 'PENDING': return '#95a5a6';
      case 'APPROVE': return '#27ae60';
      case 'REJECT': return '#e74c3c';
      case 'INTERVIEW': return '#3498db';
      case 'FOLLOW_UP': return '#f39c12';
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
          style={{ flexBasis: '25%' }}
        >
          CANDIDATE / JOB {getSortIcon('name')}
        </div>
        <div 
          className="jobs-header sortable"
          onClick={() => onSort('status')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('status')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '10%' }}
        >
          STATUS {getSortIcon('status')}
        </div>
        <div 
          className="assessment-header sortable"
          onClick={() => onSort('finalScore')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('finalScore')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '15%' }}
        >
          SCORES {getSortIcon('finalScore')}
        </div>
        <div 
          className="ai-recommend-header"
          style={{ flexBasis: '12%' }}
        >
          AI RECOMMEND
        </div>
        <div 
          className="hr-decision-header sortable"
          onClick={() => onSort('hrDecision')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('hrDecision')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '12%' }}
        >
          HR DECISION {getSortIcon('hrDecision')}
        </div>
        <div 
          className="details-header"
          style={{ flexBasis: '18%' }}
        >
          DETAILS
        </div>
        <div 
          className="chat-header"
          style={{ flexBasis: '18%' }}
        >
          CHAT
        </div>
        <div 
          className="stage-header sortable"
          onClick={() => onSort('date')}
          style={{ flexBasis: '8%' }}
        >
          DATE {getSortIcon('date')}
        </div>
      </div>
      
      {applications.map(application => (
        <div key={application.applicationId || application.application_id} className="candidate-item">
          <div className="name-company" style={{ flexBasis: '25%' }}>
            <span className={`avatar-dot ${application.avatarColor}`}></span>
            <div className="candidate-info">
              <span className="candidate-name">
                {application.candidate_name}
                <span 
                  style={{ 
                    marginLeft: '8px', 
                    cursor: 'pointer', 
                    color: '#3498db',
                    fontSize: '12px'
                  }}
                  onClick={() => copyEmailToClipboard(application.email)}
                  title={`Copy email: ${application.email}`}
                >
                  📧
                </span>
              </span>
              <span className="candidate-company">
                {application.job_title}
              </span>
            </div>
          </div>
          
          <div className="jobs" style={{ flexBasis: '10%' }}>
            <span 
              className="status-badge clickable"
              style={{ 
                backgroundColor: getStatusColor(application.status),
                color: 'white',
                padding: '4px 8px',
                borderRadius: '4px',
                fontSize: '11px',
                fontWeight: 'bold',
                cursor: 'pointer'
              }}
              onClick={() => onStatusClick(application)}
              title="Click to change status"
            >
              {getStatusDisplay(application.status)}
            </span>
          </div>
          
          <div className="assessment-scores" style={{ flexBasis: '15%' }}>
            {application.assessment ? (
              <div style={{ fontSize: '12px' }}>
                <div>Resume: {application.assessment?.resumeScore}</div>
                <div>Chat: {application.assessment?.chatScore}</div>
                <div style={{ fontWeight: 'bold' }}>Final: {application.assessment?.finalScore}</div>
                {isHrUser() && (
                  <Button
                    variant="text"
                    size="small"
                    startIcon={<AssessmentIcon />}
                    onClick={() => triggerManualScoring(application)}
                    disabled={scoringApplications.has(application.applicationId || application.application_id || '')}
                    sx={{ 
                      textTransform: 'none',
                      fontSize: '10px',
                      minWidth: 'auto',
                      mt: 0.5,
                      p: 0.5
                    }}
                  >
                    {scoringApplications.has(application.applicationId || application.application_id || '') ? 'Scoring...' : 'Re-score'}
                  </Button>
                )}
              </div>
            ) : (
              <div style={{ fontSize: '12px', color: '#95a5a6' }}>
                <div>Pending</div>
                {isHrUser() && (
                  <Button
                    variant="outlined"
                    size="small"
                    startIcon={<AssessmentIcon />}
                    onClick={() => triggerManualScoring(application)}
                    disabled={scoringApplications.has(application.applicationId || application.application_id || '')}
                    sx={{ 
                      textTransform: 'none',
                      fontSize: '10px',
                      mt: 0.5,
                      minWidth: 'auto'
                    }}
                  >
                    {scoringApplications.has(application.applicationId || application.application_id || '') ? 'Scoring...' : 'Score Now'}
                  </Button>
                )}
              </div>
            )}
          </div>
          
          <div className="ai-recommend" style={{ flexBasis: '12%' }}>
            {application.assessment ? (
              <div style={{ fontSize: '12px' }}>
                <span 
                  style={{ 
                    backgroundColor: getAiRecommendColor(application.assessment?.aiRecommendStatus),
                    color: 'white',
                    padding: '4px 8px',
                    borderRadius: '4px',
                    fontSize: '10px',
                    fontWeight: 'bold'
                  }}
                >
                  {getAiRecommendDisplay(application.assessment?.aiRecommendStatus)}
                </span>
              </div>
            ) : (
              <span style={{ color: '#95a5a6' }}>No recommendation</span>
            )}
          </div>
          
          <div className="hr-decision" style={{ flexBasis: '12%' }}>
            <span 
              className="status-badge clickable"
              style={{ 
                backgroundColor: getHrDecisionColor(application.hr_decision),
                color: 'white',
                padding: '4px 6px',
                borderRadius: '4px',
                fontSize: '10px',
                fontWeight: 'bold',
                cursor: 'pointer'
              }}
              onClick={() => onHrDecisionClick(application)}
              title="Click to change HR decision"
            >
              {getHrDecisionDisplay(application.hr_decision)}
            </span>
          </div>
          
          <div className="details" style={{ flexBasis: '18%' }}>
            <Button 
              variant="outlined"
              color="primary"
              size="small"
              startIcon={<VisibilityIcon />}
              onClick={() => onDetailsClick(application)}
              sx={{ 
                textTransform: 'none',
                fontSize: '12px'
              }}
            >
              View Details
            </Button>
          </div>

          <div className="chat" style={{ flexBasis: '18%' }}>
            <Button 
              variant={isHrUser() ? 'outlined' : 'contained'}
              color={isHrUser() ? 'secondary' : 'primary'}
              size="small"
              startIcon={isHrUser() ? <VisibilityIcon /> : <ChatIcon />}
              onClick={() => {
                setSelectedChatApplication(application);
                if (isHrUser()) {
                  setIsChatHistoryModalOpen(true);
                } else {
                  setIsCandidateChatModalOpen(true);
                }
              }}
              sx={{ 
                textTransform: 'none',
                fontSize: '12px',
                bgcolor: isHrUser() ? 'transparent' : '#3498db',
                '&:hover': {
                  bgcolor: isHrUser() ? '#f3e5f5' : '#2980b9'
                }
              }}
              title={isHrUser() ? 'View chat history (read-only)' : 'Start/continue chat'}
            >
              {isHrUser() ? 'View Chat' : 'Chat'}
            </Button>
          </div>
          
          <div className="stage" style={{ flexBasis: '8%' }}>
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
                    }
                  }
                }
                
                return '--';
              })()}
            </div>
          </div>
        </div>
      ))}
      
      <ChatHistoryModal
        isOpen={isChatHistoryModalOpen}
        application={selectedChatApplication}
        onClose={() => {
          setIsChatHistoryModalOpen(false);
          setSelectedChatApplication(null);
        }}
      />
      
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
        }}
      />
    </div>
  );
};

export default ApplicationList;