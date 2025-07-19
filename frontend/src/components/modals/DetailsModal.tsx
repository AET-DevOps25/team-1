import React from 'react';
import type { Application } from '../../types/dashboard';

interface DetailsModalProps {
  isOpen: boolean;
  selectedApplication: Application | null;
  onClose: () => void;
}

const DetailsModal: React.FC<DetailsModalProps> = ({
  isOpen,
  selectedApplication,
  onClose
}) => {
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

  const getHrDecisionDisplay = (decision?: string) => {
    switch (decision) {
      case 'SHORTLISTED': return 'Shortlisted';
      case 'REJECTED': return 'Rejected';
      case 'HIRED': return 'Hired';
      default: return 'No Decision';
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

  if (!isOpen || !selectedApplication) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-content" onClick={(e) => e.stopPropagation()} style={{ maxWidth: '700px' }}>
        <div className="modal-header">
          <h3>Candidate Details - {selectedApplication.candidate_name}</h3>
          <button className="modal-close" onClick={onClose}>×</button>
        </div>
        
        <div className="modal-body">
          <div className="candidate-details">
            <h4>{selectedApplication.candidate_name}</h4>
            <p>{selectedApplication.job_title}</p>
            <p>Email: {selectedApplication.email}</p>
            <p>Status: <span className="status-highlight">{getStatusDisplay(selectedApplication.status)}</span></p>
            <p>HR Decision: <span className="status-highlight">{getHrDecisionDisplay(selectedApplication.hr_decision)}</span></p>
            
            {selectedApplication.assessment && (
              <div style={{ marginTop: '20px' }}>
                <div style={{ display: 'flex', gap: '20px', marginBottom: '20px' }}>
                  <div>
                    <strong>Resume Score:</strong> {selectedApplication.assessment?.resumeScore}
                  </div>
                  <div>
                    <strong>Interview Score:</strong> {selectedApplication.assessment?.interviewScore ?? selectedApplication.assessment?.chatScore ?? 'N/A'}
                  </div>
                  <div>
                    <strong>Final Score:</strong> {selectedApplication.assessment?.finalScore ?? 'N/A'}
                  </div>
                  <div>
                    <strong>AI Recommendation:</strong> 
                    <span style={{ 
                      marginLeft: '8px',
                      backgroundColor: getAiRecommendColor(selectedApplication.assessment?.recommendation),
                      color: 'white',
                      padding: '2px 6px',
                      borderRadius: '4px',
                      fontSize: '12px'
                    }}>
                      {getAiRecommendDisplay(selectedApplication.assessment?.recommendation)}
                    </span>
                  </div>
                </div>
                
                <div style={{ marginBottom: '20px' }}>
                  <h5 style={{ color: '#2c3e50', marginBottom: '10px' }}>AI Resume Analysis:</h5>
                  <div style={{ 
                    padding: '12px', 
                    backgroundColor: '#f8f9fa', 
                    borderRadius: '4px',
                    lineHeight: '1.5',
                    fontSize: '14px',
                    border: '1px solid #e9ecef'
                  }}>
                    {selectedApplication.assessment?.resumeComment ?? selectedApplication.assessment?.aiResumeAnalysis ?? 'No analysis available'}
                  </div>
                </div>
                
                <div style={{ marginBottom: '20px' }}>
                  <h5 style={{ color: '#2c3e50', marginBottom: '10px' }}>AI Interview Summary:</h5>
                  <div style={{ 
                    padding: '12px', 
                    backgroundColor: '#f8f9fa', 
                    borderRadius: '4px',
                    lineHeight: '1.5',
                    fontSize: '14px',
                    border: '1px solid #e9ecef'
                  }}>
                    {selectedApplication.assessment?.interviewComment ?? selectedApplication.assessment?.aiChatSummary ?? 'No interview summary available'}
                  </div>
                </div>
                
              </div>
            )}
            
            {selectedApplication.hr_comment && (
              <div style={{ marginTop: '20px' }}>
                <h5 style={{ color: '#2c3e50', marginBottom: '10px' }}>HR Comment:</h5>
                <div style={{ 
                  padding: '12px', 
                  backgroundColor: '#fff3cd', 
                  borderRadius: '4px',
                  lineHeight: '1.5',
                  fontSize: '14px',
                  border: '1px solid #ffeaa7'
                }}>
                  {selectedApplication.hr_comment}
                </div>
              </div>
            )}
          </div>
        </div>
        
        <div className="modal-footer">
          <button 
            className="btn-cancel" 
            onClick={onClose}
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default DetailsModal;