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

  const hasChatHistory = (application: Application): boolean => {
    const statusesWithChat = ['AI_INTERVIEW', 'COMPLETED', 'SHORTLISTED', 'REJECTED', 'HIRED'];
    const statusesWithoutChat = ['SUBMITTED', 'AI_SCREENING'];
    
    if (statusesWithoutChat.includes(application.status)) {
      return false;
    }
    
    if (statusesWithChat.includes(application.status)) {
      return true;
    }
    
    const hasChatScore = application.assessment?.chatScore !== undefined || application.assessment?.chat_score !== undefined;
    return hasChatScore;
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
      case 'SHORTLISTED': return 'Shortlisted';
      case 'REJECTED': return 'Rejected';
      case 'HIRED': return 'Hired';
      default: return 'No Decision';
    }
  };

  const getHrDecisionColor = (decision?: string) => {
    switch (decision) {
      case 'SHORTLISTED': return '#f39c12';
      case 'REJECTED': return '#e74c3c';
      case 'HIRED': return '#27ae60';
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
          style={{ flexBasis: '20%' }}
        >
          CANDIDATE / JOB {getSortIcon('name')}
        </div>
        <div 
          className="jobs-header sortable"
          onClick={() => onSort('status')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('status')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '8%' }}
        >
          STATUS {getSortIcon('status')}
        </div>
        <div 
          className="resume-score-header sortable"
          onClick={() => onSort('resumeScore')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('resumeScore')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '8%' }}
        >
          RESUME {getSortIcon('resumeScore')}
        </div>
        <div 
          className="interview-score-header sortable"
          onClick={() => onSort('chatScore')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('chatScore')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '8%' }}
        >
          INTERVIEW {getSortIcon('chatScore')}
        </div>
        <div 
          className="final-score-header sortable"
          onClick={() => onSort('finalScore')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('finalScore')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '8%' }}
        >
          FINAL {getSortIcon('finalScore')}
        </div>
        <div 
          className="interview-comment-header"
          style={{ flexBasis: '12%' }}
        >
          RECOMMENDATION
        </div>
        <div 
          className="hr-decision-header sortable"
          onClick={() => onSort('hrDecision')}
          onKeyDown={(e) => e.key === 'Enter' && onSort('hrDecision')}
          role="button"
          tabIndex={0}
          style={{ flexBasis: '10%' }}
        >
          HR DECISION {getSortIcon('hrDecision')}
        </div>
        <div 
          className="details-header"
          style={{ flexBasis: '12%' }}
        >
          DETAILS
        </div>
        <div 
          className="chat-header"
          style={{ flexBasis: '14%' }}
        >
          CHAT
        </div>
      </div>
      
      {applications.map(application => (
        <div key={application.applicationId || application.application_id} className="candidate-item">
          <div className="name-company" style={{ flexBasis: '20%' }}>
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
          
          <div className="jobs" style={{ flexBasis: '8%' }}>
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
          
          <div className="resume-score" style={{ flexBasis: '8%' }}>
            <div style={{ fontSize: '12px', textAlign: 'center' }}>
              {(() => {
                const resumeScore = application.assessment?.resumeScore;
                return resumeScore !== undefined && resumeScore !== null ? (
                  <span style={{ fontWeight: 'bold' }}>{resumeScore}</span>
                ) : (
                  <span style={{ color: '#95a5a6' }}>-</span>
                );
              })()}
            </div>
          </div>
          
          <div className="interview-score" style={{ flexBasis: '8%' }}>
            <div style={{ fontSize: '12px', textAlign: 'center' }}>
              {(() => {
                // Check for both API formats
                const interviewScore = application.assessment?.interviewScore ?? application.assessment?.chatScore;
                // Check if interview was completed by looking at chat status or interview comment
                const hasInterview = application.chatStatus === 'COMPLETE' || 
                                   application.assessment?.interviewComment || 
                                   application.assessment?.aiChatSummary;
                
                if (hasInterview && interviewScore !== null && interviewScore !== undefined) {
                  return <span style={{ fontWeight: 'bold' }}>{interviewScore}</span>;
                } else if (hasInterview && (interviewScore === null || interviewScore === undefined)) {
                  return <span style={{ color: '#f39c12' }}>Pending Score</span>;
                } else {
                  return <span style={{ color: '#95a5a6' }}>No Interview</span>;
                }
              })()}
            </div>
          </div>
          
          <div className="final-score" style={{ flexBasis: '8%' }}>
            <div style={{ fontSize: '12px', textAlign: 'center' }}>
              {(() => {
                const finalScore = application.assessment?.finalScore;
                return finalScore !== undefined && finalScore !== null ? (
                  <span style={{ fontWeight: 'bold', color: '#2c3e50' }}>{finalScore}</span>
                ) : (
                  <span style={{ color: '#95a5a6' }}>-</span>
                );
              })()}
              {isHrUser() && (
                <Button
                  variant="text"
                  size="small"
                  startIcon={<AssessmentIcon />}
                  onClick={() => triggerManualScoring(application)}
                  disabled={scoringApplications.has(application.applicationId || application.application_id || '')}
                  sx={{ 
                    textTransform: 'none',
                    fontSize: '8px',
                    minWidth: 'auto',
                    mt: 0.5,
                    p: 0.3,
                    display: 'block'
                  }}
                >
                  {scoringApplications.has(application.applicationId || application.application_id || '') ? 'Scoring...' : 'Re-score'}
                </Button>
              )}
            </div>
          </div>
          
          <div className="ai-recommend" style={{ flexBasis: '12%' }}>
            <div style={{ fontSize: '11px' }}>
              {(() => {
                // Check for both API formats
                const recommendStatus = application.assessment?.recommendation ?? application.assessment?.aiRecommendStatus;
                
                // Debug logging
                if (application.candidate_name === 'cand cand') {
                  console.log('Debug - Candidate:', application.candidate_name);
                  console.log('Debug - Recommendation Status:', recommendStatus);
                  console.log('Debug - Full Assessment:', application.assessment);
                }
                
                return recommendStatus ? (
                  <span 
                    style={{ 
                      backgroundColor: getAiRecommendColor(recommendStatus),
                      color: 'white',
                      padding: '3px 6px',
                      borderRadius: '3px',
                      fontSize: '10px',
                      fontWeight: 'bold'
                    }}
                  >
                    {getAiRecommendDisplay(recommendStatus)}
                  </span>
                ) : (
                  <span style={{ color: '#95a5a6' }}>Pending</span>
                );
              })()}
              {(() => {
                // Check for both interview comment formats
                const interviewComment = application.assessment?.interviewComment ?? application.assessment?.aiChatSummary;
                return interviewComment && (
                  <div style={{ 
                    marginTop: '4px', 
                    fontSize: '10px', 
                    color: '#666',
                    overflow: 'hidden',
                    textOverflow: 'ellipsis',
                    display: '-webkit-box',
                    WebkitLineClamp: 2,
                    WebkitBoxOrient: 'vertical'
                  }}>
                    {interviewComment}
                  </div>
                );
              })()}
            </div>
          </div>
          
          <div className="hr-decision" style={{ flexBasis: '10%' }}>
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
          
          <div className="details" style={{ flexBasis: '12%' }}>
            <Button 
              variant="outlined"
              color="primary"
              size="small"
              startIcon={<VisibilityIcon />}
              onClick={() => onDetailsClick(application)}
              sx={{ 
                textTransform: 'none',
                fontSize: '11px'
              }}
            >
              Details
            </Button>
          </div>

          <div className="chat" style={{ flexBasis: '14%' }}>
            <Button 
              variant={isHrUser() ? 'outlined' : 'contained'}
              color={isHrUser() ? 'secondary' : 'primary'}
              size="small"
              startIcon={isHrUser() ? <VisibilityIcon /> : <ChatIcon />}
              disabled={isHrUser() && !hasChatHistory(application)}
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
                fontSize: '11px',
                bgcolor: isHrUser() ? 'transparent' : '#3498db',
                '&:hover': {
                  bgcolor: isHrUser() ? '#f3e5f5' : '#2980b9'
                },
                opacity: isHrUser() && !hasChatHistory(application) ? 0.6 : 1
              }}
              title={isHrUser() ? 
                (hasChatHistory(application) ? 'View chat history (read-only)' : 'No interview chat available yet') : 
                'Start/continue chat'
              }
            >
              {isHrUser() ? 
                (hasChatHistory(application) ? 'Chat' : 'No Interview') : 
                'Chat'
              }
            </Button>
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